package gg.aquatic.klocale

import gg.aquatic.klocale.message.Message

/**
 * Strategy for handling cases where a requested message key is not found.
 */
interface MissingKeyHandler<A : Message<A>> {

    /**
     * Handles a missing key and returns a fallback Message.
     * @param key The missing key path
     * @param language The language that was requested
     * @return A fallback message to be displayed/used instead
     */
    fun handle(key: String, language: String): A

    /**
     * Default implementation that throws an exception.
     */
    class Throwing<A : Message<A>> : MissingKeyHandler<A> {
        override fun handle(key: String, language: String): A {
            throw IllegalArgumentException("Message '$key' not found for language '$language'")
        }
    }
}
