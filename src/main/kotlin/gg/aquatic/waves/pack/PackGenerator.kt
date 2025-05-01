package gg.aquatic.waves.pack

import gg.aquatic.waves.Waves
import gg.aquatic.waves.util.Config
import gg.aquatic.waves.util.getSectionList
import net.radstevee.packed.core.font.Font
import net.radstevee.packed.core.font.FontProvider
import net.radstevee.packed.core.key.Key
import org.bukkit.configuration.file.FileConfiguration
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO

object PackGenerator {

    fun load() {
        val session = PackSession()

        val fontsFolder = Waves.INSTANCE.dataFolder.resolve("fonts").apply { mkdirs() }
        val iconsFolder = Waves.INSTANCE.dataFolder.resolve("icons").apply { mkdirs() }
        loadGlyphs(iconsFolder, session)
        loadFonts(fontsFolder, session)

        session.iconFonts.values.forEach { PackHandler.pack.addFont(it) }
        PackHandler.pack.save(true)
        Waves.INSTANCE.dataFolder.resolve("tempassets").deleteRecursively()
    }

    private fun loadGlyphs(folder: File, session: PackSession) {
        if (folder.isDirectory) {
            for (f in folder.listFiles() ?: emptyArray()) {
                loadGlyphs(f, session)
            }
        } else {
            val cfg = Config(folder, Waves.INSTANCE)
            cfg.load()
            loadGlyphs(cfg.getConfiguration()!!, session)
        }
    }

    private fun loadFonts(folder: File, session: PackSession) {
        if (folder.isDirectory) {
            for (f in folder.listFiles() ?: emptyArray()) {
                loadFonts(f, session)
            }
        } else {
            val cfg = Config(folder, Waves.INSTANCE)
            cfg.load()
            loadFonts(cfg.getConfiguration()!!, session)
        }
    }

    private fun loadFonts(cfg: FileConfiguration, session: PackSession) {
        val fontsSection = cfg.getConfigurationSection("fonts")!!
        for (key in fontsSection.getKeys(false)) {
            val fontSection = fontsSection.getConfigurationSection(key) ?: continue
            val baseHeight = fontSection.getInt("base-height")
            val id = key
            val lines = fontSection.getInt("lines")
            val fontHeight = fontSection.getInt("height")
            val lettersSection = fontSection.getConfigurationSection("letters") ?: continue
            val namespace = fontSection.getString("namespace") ?: "wavesfonts"
            val fontAscent = fontSection.getInt("height-offset")
            //val letterPaths = HashMap<Char, String>()

            val font = Font(Key(namespace, id))

            val fontIcons = HashMap<Int, MutableMap<Char, MutableList<Pair<Int, Glyph>>>>()

            for (letter in lettersSection.getKeys(false)) {
                val frames = HashMap<Int, MutableList<Pair<Int, Glyph>>>()

                fun loadFrame(path: String, char: Char, stay: Int) {
                    val originalFile = Waves.INSTANCE.dataFolder.resolve("textures/$path.png")
                    val destinationFile = session.tempFolder.resolve("$namespace/textures/$path.png")

                    // Ensure the parent directories exist for the destination file
                    destinationFile.parentFile.mkdirs()

                    // Process the PNG file and modify it based on the `lines` value
                    val originalImage = try {
                        ImageIO.read(originalFile)
                    } catch (_: Exception) {
                        println("Failed to read the image for $char at path $originalFile")
                        null
                    }
                    if (originalImage == null) {
                        //println("Failed to read the image for $char at path $originalFile")
                        return
                    }

                    // Extract the width and height of the original image
                    val width = originalImage.width
                    val originalHeight = originalImage.height

                    val height = (if (lines > 1) {
                        val additionalSpace = (lines - 1) * (fontHeight + 2) // Additional height for empty spaces
                        originalHeight + additionalSpace + baseHeight
                    } else originalHeight + baseHeight) + if (fontAscent > 0) fontAscent else 0

                    val newImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

                    val graphics = newImage.createGraphics()
                    graphics.drawImage(originalImage, 0, 0, null)

                    graphics.dispose()

                    // Save the new extended image to the destination file
                    ImageIO.write(newImage, "png", destinationFile)

                    // Print or store the width and height for use
                    println("Image Dimensions for $char -> Width: $width, Height: $height")

                    for (i in 0 until lines) {
                        val packChar = formattedUnicode(session)
                        val newAscent = (if (i == 0) height else height - ((fontHeight * i) + (2 * i))) + if (fontAscent < 0) fontAscent else 0
                        /*
                        height = 5 + (2*(5+2)) = 5 + 14 = 19
                        0 = 19
                        1 = 19 - (5+2) = 12
                        2 = 19 - (10+2) = 7
                         */
                        font.addProvider(
                            FontProvider.Bitmap(
                                Key(namespace, "$path.png"),
                                height.toDouble(),
                                newAscent - 1.0,
                                listOf(packChar.toString())
                            )
                        )
                        val icon = Glyph(
                            char.toString(),
                            namespace,
                            id,
                            packChar,
                            width,
                            height
                        )
                        frames.getOrPut(i + 1) { kotlin.collections.ArrayList() } += stay to icon
                    }
                }

                val char = if (letter.lowercase() == "dot") '.' else letter.toCharArray().first()

                val letterSection = lettersSection.getConfigurationSection(letter) ?: continue
                if (letterSection.contains("frames")) {
                    for (frameSection in letterSection.getSectionList("frames")) {
                        val path = frameSection.getString("texture") ?: continue
                        val stay = frameSection.getInt("stay")

                        loadFrame(path, char, stay)
                    }
                } else {
                    val path = letterSection.getString("texture") ?: continue
                    loadFrame(path, char, 1)
                }

                for ((line, lineFrames) in frames) {
                    fontIcons.getOrPut(line) { kotlin.collections.HashMap() }[char] = lineFrames
                }

                //letterPaths[char] = path
            }
            val dialogueFont = Font(
                id,
                fontSection.getInt("char-spacing", 1),
                fontSection.getInt("base-height"),
                namespace
            )
            dialogueFont.glyphs += fontIcons
            Glyphs.fonts[id] = dialogueFont
            PackHandler.pack.addFont(font)
        }
    }

    private fun loadGlyphs(cfg: FileConfiguration, session: PackSession) {
        val iconsSection = cfg.getConfigurationSection("icons")!!
        for (key in iconsSection.getKeys(false)) {
            val iconSection = iconsSection.getConfigurationSection(key) ?: continue
            val id = key
            val path = iconSection.getString("path") ?: continue

            val font = iconSection.getString("font") ?: "icons"
            val namespace = iconSection.getString("namespace") ?: "wavesglyphs"

            val originalFile = Waves.INSTANCE.dataFolder.resolve("textures/$path.png")
            val destinationFile = session.tempFolder.resolve("$namespace/textures/$path.png")

            // Ensure the parent directories exist for the destination file
            destinationFile.parentFile.mkdirs()

            val originalImage = try {
                ImageIO.read(originalFile)
            } catch (e: Exception) {
                println("Failed to read the image for $id at path $originalFile")
                null
            }
            if (originalImage == null) {
                continue
            }
            val packChar = formattedUnicode(session)
            val height = originalImage.height
            val ascent = iconSection.getInt("ascent", height - 1)

            Files.copy(originalFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

            val fontKey = Key(namespace, font)
            val sessionFont = session.iconFonts.getOrPut(fontKey) { Font(fontKey) }

            sessionFont.addProvider(
                FontProvider.Bitmap(
                    Key(namespace, "$path.png"),
                    height.toDouble(),
                    ascent.toDouble(),
                    listOf(packChar.toString())
                )
            )

            val icon = Glyph(
                id,
                namespace,
                font,
                packChar,
                originalImage.width,
                height
            )
            Glyphs.glyphs[id] = icon
            println("Generated icon $id (Font: icons, Char: $packChar, Width: ${originalImage.width}, Height: $height)")
        }
    }

    private fun formattedUnicode(session: PackSession): Char {
        return Char(session.freeUnicode++)
    }

    class PackSession() {
        var freeUnicode = "e000".toInt(16)
        val tempFolder = Waves.INSTANCE.dataFolder.resolve("tempassets/assets/")
            .apply { mkdirs() }
        val iconFonts = HashMap<Key, Font>()
    }

}