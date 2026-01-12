package gg.aquatic.common

import kotlin.collections.iterator

inline fun <A,B,C,D> Map<A,B>.map(transform: (A,B) -> Pair<C,D>): MutableMap<C,D> {
    val result = mutableMapOf<C,D>()
    for (entry in this) {
        val (c,d) = transform(entry.key, entry.value)
        result[c] = d
    }
    return result
}