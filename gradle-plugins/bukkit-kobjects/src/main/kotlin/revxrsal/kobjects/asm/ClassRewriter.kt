package revxrsal.kobjects.asm

import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassWriter
import java.io.File
import kotlin.io.readBytes
import kotlin.io.writeBytes

private const val EXPAND_FRAMES = 8

/**
 * Applies the necessary bytecode transformations to Kotlin object class files
 */
object ClassRewriter {

    /**
     * Rewrites the given file
     */
    fun rewrite(inputFile: File) {
        val reader = ClassReader(inputFile.readBytes())
        val writer = ClassWriter(reader, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        reader.accept(
            ObjectRewriter(
                className = reader.className,
                writer = writer
            ), EXPAND_FRAMES
        )

        inputFile.writeBytes(writer.toByteArray())
    }
}
