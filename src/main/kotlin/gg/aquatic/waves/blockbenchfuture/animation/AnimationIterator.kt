package gg.aquatic.waves.blockbenchfuture.animation

import gg.aquatic.waves.blockbenchfuture.interpolation.Timed

interface AnimationIterator<T: Timed>: Iterator<T> {

    fun clear()

    val type: Type

    enum class Type {

        PLAY_ONCE {
            override fun <T : Timed> create(list: List<T>): AnimationIterator<T> {
                return PlayOnce(list)
            }

        },
        LOOP {
            override fun <T : Timed> create(list: List<T>): AnimationIterator<T> {
                return Loop(list)
            }
        },
        HOLD_ON_LAST {
            override fun <T : Timed> create(list: List<T>): AnimationIterator<T> {
                return HoldOnLast(list)
            }
        };

        abstract fun <T: Timed> create(list: List<T>): AnimationIterator<T>
    }

    class PlayOnce<T: Timed>(
        val keyFrame: List<T>,
    ): AnimationIterator<T> {
        private var index = 0

        override fun clear() {
            index = Integer.MAX_VALUE
        }

        override val type: Type = Type.PLAY_ONCE

        override fun hasNext(): Boolean {
            return index < keyFrame.size
        }
        override fun next(): T {
            return keyFrame[index++]
        }
    }
    class HoldOnLast<T: Timed>(
        val keyFrame: List<T>,
    ): AnimationIterator<T> {
        private var index = 0

        override fun clear() {
            index = 0
        }

        override val type: Type = Type.HOLD_ON_LAST

        override fun next(): T {
            if (index >= keyFrame.size) return keyFrame.last()
            return keyFrame[index++]
        }

        override fun hasNext(): Boolean {
            return true
        }
    }

    class Loop<T: Timed>(
        val keyFrame: List<T>,
    ): AnimationIterator<T> {
        private var index = 0

        override fun clear() {
            index = 0
        }

        override fun hasNext(): Boolean {
            return true
        }

        override fun next(): T {
            if (index >= keyFrame.size) index = 0
            return keyFrame[index++]
        }
        override val type: Type = Type.LOOP
    }
}