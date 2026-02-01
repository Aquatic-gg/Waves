package gg.aquatic.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.*;

/**
 * The InternalResolver class provides utility methods for managing and resolving dependencies,
 * downloading tools, and handling secrets. It interacts with local files, remote repositories,
 * and environment variables to facilitate dependency resolution.
 */
public class InternalResolver {
    private final Path cacheDir;
    private final Map<String, String> localSecrets = new HashMap<>();
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    /**
     * Constructs an instance of InternalResolver with the specified cache directory.
     *
     * @param cacheDir the directory where cache data will be stored
     */
    public InternalResolver(Path cacheDir) {
        this.cacheDir = cacheDir;
    }

    /**
     * Loads secrets from a given environment file and stores them in a local secrets map.
     * Each line in the file should represent a key-value pair separated by an '='. Lines
     * that are empty or start with a '#' are ignored. If the file does not exist, the
     * method returns without performing any operation.
     *
     * @param envFile the path to the environment file containing secrets to be loaded
     */
    public void loadSecrets(Path envFile) {
        if (!Files.exists(envFile)) return;
        try (BufferedReader reader = Files.newBufferedReader(envFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    localSecrets.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException ignored) {}
    }

    /**
     * Downloads a specific version of a tool specified by its group, artifact, and version
     * from the Maven Central Repository. If the tool is already cached, it returns the cached
     * path. Otherwise, it downloads the tool and saves it to the local cache directory.
     *
     * @param group the group ID of the tool to be downloaded
     * @param artifact the artifact ID of the tool to be downloaded
     * @param version the version of the tool to be downloaded
     * @return the path to the downloaded tool file
     * @throws Exception if the tool cannot be downloaded or any error occurs during the process
     */
    public Path downloadTool(String group, String artifact, String version) throws Exception {
        Path toolDir = cacheDir.resolve("tools");
        if (!Files.exists(toolDir)) Files.createDirectories(toolDir);

        Path target = toolDir.resolve(artifact + "-" + version + ".jar");
        if (Files.exists(target)) return target;

        String url = String.format("https://repo1.maven.org/maven2/%s/%s/%s/%s-%s.jar",
                group.replace('.', '/'), artifact, version, artifact, version);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(target));

        if (response.statusCode() != 200) {
            Files.deleteIfExists(target);
            throw new RuntimeException("Failed to download tool: " + artifact);
        }
        return target;
    }

    private String resolveSecret(String value) {
        if (value == null || value.isEmpty()) return "";

        if (value.startsWith("${") && value.endsWith("}")) {
            String envKey = value.substring(2, value.length() - 1);

            String secret = localSecrets.get(envKey);

            if (secret == null) {
                secret = System.getenv(envKey);
            }

            if (secret == null || secret.isEmpty()) {
                System.err.println("[DependencyResolver] WARNING: Required secret '" + envKey + "' not found in .env or System Env!");
                return "";
            }
            return secret;
        }
        return value;
    }

    /**
     * Resolves the dependencies specified in the given manifest content by downloading them
     * from the listed repositories. The method verifies the downloaded files using provided
     * checksums and avoids re-downloading files that are already cached and verified.
     *
     * @param manifestContent the JSON content of the manifest containing dependencies and repositories
     * @return a list of paths to the resolved and cached dependency files
     * @throws Exception if any dependency cannot be resolved or if an error occurs during processing
     */
    public List<Path> resolve(String manifestContent) throws Exception {
        List<Path> resolved = new ArrayList<>();
        List<String> depsJson = extractList(manifestContent, "dependencies");
        List<String> reposJson = extractList(manifestContent, "repositories");

        for (String depJson : depsJson) {
            String group = extractValue(depJson, "group");
            String artifact = extractValue(depJson, "artifact");
            String version = extractValue(depJson, "version");
            String checksum = extractValue(depJson, "checksum");

            // Store downloads in a versioned subfolder to avoid conflicts
            Path target = cacheDir.resolve("downloads").resolve(group.replace('.', '/'))
                    .resolve(artifact + "-" + version + ".jar");
            Files.createDirectories(target.getParent());

            boolean needsDownload = true;
            if (Files.exists(target)) {
                if (checksum == null || checksum.isEmpty()) {
                    needsDownload = false;
                } else if (verify(target, checksum)) {
                    needsDownload = false;
                } else {
                    Files.deleteIfExists(target);
                }
            }

            if (needsDownload) {
                if (!tryDownloadFromRepos(reposJson, group, artifact, version, checksum, target)) {
                    throw new RuntimeException("Could not resolve " + artifact + " from any repository");
                }
            }
            resolved.add(target);
        }
        return resolved;
    }

    private boolean tryDownloadFromRepos(List<String> repos, String g, String a, String v, String checksum, Path target) throws Exception {
        for (String repoJson : repos) {
            if (download(extractValue(repoJson, "url"), g, a, v, target,
                    extractValue(repoJson, "user"), extractValue(repoJson, "pass"), checksum)) {
                return true;
            }
        }
        return false;
    }

    private boolean download(String repo, String g, String a, String v, Path target, String user, String pass, String checksum) throws Exception {
        String baseUrl = repo.endsWith("/") ? repo.substring(0, repo.length() - 1) : repo;
        String url = String.format("%s/%s/%s/%s/%s-%s.jar",
                baseUrl,
                g.replace('.', '/'), a, v, a, v);

        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).GET();

        String actualUser = resolveSecret(user);
        String actualPass = resolveSecret(pass);

        if (!actualUser.isEmpty()) {
            String auth = Base64.getEncoder().encodeToString((actualUser + ":" + actualPass).getBytes());
            builder.header("Authorization", "Basic " + auth);
        }

        Path tempFile = Files.createTempFile(target.getParent(), "download-", ".tmp");
        HttpResponse<Path> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofFile(tempFile));

        if (resp.statusCode() != 200) {
            Files.deleteIfExists(tempFile);
            return false;
        }

        if (checksum != null && !checksum.isEmpty() && !verify(tempFile, checksum)) {
            Files.deleteIfExists(tempFile);
            return false;
        }

        Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    private boolean verify(Path file, String expected) throws Exception {
        if (expected == null || expected.isEmpty()) return true;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(Files.readAllBytes(file));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString().equalsIgnoreCase(expected);
    }

    String extractValue(String json, String key) {
        int idx = json.indexOf("\"" + key + "\":");
        if (idx == -1) return "";

        int start = json.indexOf("\"", idx + key.length() + 2) + 1;
        int end = json.indexOf("\"", start);
        if (start <= 0 || end <= 0) return "";

        return json.substring(start, end);
    }

    List<String> extractList(String json, String key) {
        List<String> list = new ArrayList<>();
        int startIdx = json.indexOf("\"" + key + "\":[");
        if (startIdx == -1) return list;

        int endIdx = json.indexOf("]", startIdx);
        String content = json.substring(startIdx + key.length() + 4, endIdx);

        String[] parts = content.split("\\},\\{");
        for (String p : parts) {
            String sanitized = p.startsWith("{") ? p : "{" + p;
            sanitized = sanitized.endsWith("}") ? sanitized : sanitized + "}";
            list.add(sanitized);
        }
        return list;
    }
}
