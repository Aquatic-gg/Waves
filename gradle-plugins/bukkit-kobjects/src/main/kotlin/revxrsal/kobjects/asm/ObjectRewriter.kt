package revxrsal.kobjects.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

/**
 * The singleton instance field name
 */
private const val SINGLETON_NAME = "INSTANCE"

/**
 * An ASM transformer that performs the following changes:
 * - Remove the `private` modifier from the constructor
 * - Remove the `final` modifier from the singleton
 * - Remove the initialization of the singleton on class load
 * - Assign INSTANCE to this in the constructor
 */
class ObjectRewriter(
    private val className: String,
    writer: ClassWriter,
) : ClassVisitor(ASM9, writer) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String?>?,
    ): MethodVisitor? {
        if (name == "<init>" && access != ACC_PUBLIC) {
            return object : MethodVisitor(ASM9, cv.visitMethod(ACC_PUBLIC, name, descriptor, signature, exceptions)) {
                private var injected = false
                override fun visitInsn(opcode: Int) {
                    super.visitInsn(opcode)
                }

                override fun visitMethodInsn(
                    opcode: Int,
                    owner: String?,
                    name: String?,
                    descriptor: String?,
                    isInterface: Boolean,
                ) {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    if (opcode == INVOKESPECIAL && !injected) {
                        injected = true
                        visitVarInsn(ALOAD, 0) // Load 'this' onto the stack
                        visitFieldInsn(
                            PUTSTATIC,
                            className,
                            SINGLETON_NAME,
                            "L${className};" // Type of INSTANCE field
                        )
                    }
                }
            }
        }
        if (name == "<clinit>") {
            return object : MethodVisitor(ASM9) {

                private var isCreatingInstance = false

                override fun visitTypeInsn(opcode: Int, type: String?) {
                    if (opcode == NEW && type == className) {
                        isCreatingInstance = true
                        return
                    }
                    super.visitTypeInsn(opcode, type)
                }

                override fun visitInsn(opcode: Int) {
                    // Skip the NEW and DUP instructions specifically for WorldCleanerPlugin
                    if (opcode == DUP && isCreatingInstance) {
                        return
                    }
                    super.visitInsn(opcode)
                }

                override fun visitMethodInsn(
                    opcode: Int,
                    owner: String?,
                    name: String?,
                    descriptor: String?,
                    isInterface: Boolean,
                ) {
                    if (opcode == INVOKESPECIAL && isCreatingInstance && owner == className) {
                        return
                    }
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }

                override fun visitFieldInsn(
                    opcode: Int,
                    owner: String?,
                    name: String?,
                    descriptor: String?,
                ) {

                    // Skip the PUTSTATIC that initializes the singleton INSTANCE field for the plugin instance
                    if (opcode == PUTSTATIC
                        && name == SINGLETON_NAME
                        && owner == className
                        && descriptor == "L${className};"
                        && isCreatingInstance
                    ) {
                        isCreatingInstance = false
                        return
                    }
                    super.visitFieldInsn(opcode, owner, name, descriptor)
                }
            }
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }

    // Remove the final modifier from the INSTANCE
    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?,
    ): FieldVisitor? {
        if (descriptor == className && access.isPublicStaticFinal && name == SINGLETON_NAME)
            return super.visitField(access and ACC_FINAL.inv(), name, descriptor, signature, value)
        return super.visitField(access, name, descriptor, signature, value)
    }
}

private val Int.isPublicStaticFinal: Boolean
    get() {
        return (this and ACC_PUBLIC != 0) && (this and ACC_STATIC != 0) && (this and ACC_FINAL != 0)
    }
