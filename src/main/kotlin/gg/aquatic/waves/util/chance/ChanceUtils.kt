package gg.aquatic.waves.util.chance

import gg.aquatic.waves.util.decimals

class ChanceUtils {

    companion object {

        fun <T : IChance> getRandomItemOld(items: Collection<T>): T? {
            val chances = items.map { it.chance }.toMutableList()
            val randomIndex = getRandomChanceIndex(chances)
            if (randomIndex < 0) return null
            return items.elementAtOrNull(randomIndex)
        }

        fun getRandomChanceIndex(chances: Collection<Double>): Int {
            if (chances.isEmpty()) return -1
            if (getTotalPercentage(chances) <= 0) {
                return -1
            }
            var totalWeight = 0.0
            for (chance in chances) {
                totalWeight += chance
            }

            var random: Double = Math.random() * totalWeight
            for ((i, chance) in chances.withIndex()) {
                random -= chance
                if (random <= 0.0) {
                    return i
                }
            }
            return -1
        }

        private fun getTotalPercentage(chances: Collection<Double>): Double {
            return chances.sum()
        }

        fun <T : IChance> getRandomItem(items: Collection<T>): T? {
            if (items.isEmpty()) return null

            val totalWeight = items.sumOf { it.chance }
            if (totalWeight <= 0) return null

            var random = Math.random() * totalWeight
            for (item in items) {
                random -= item.chance
                if (random <= 0.0) {
                    return item
                }
            }
            return null
        }
    }

}

fun <T: IChance> Collection<T>.randomItem(): T? {
    return ChanceUtils.getRandomItem(this)
}

fun Collection<IChance>.realChance(item: IChance): Double {
    var total = 0.0
    for (chance in this) {
        total += chance.chance
    }
    return if (total > 0) item.chance / total else 0.0
}

fun Collection<IChance>.realChanceFormatted(item: IChance): Double {
    val chance = realChance(item)
    return (chance*100.0).decimals(2).toDouble()
}

fun <T: IChance> Collection<T>.realChance(item: T): HashMap<T, Double> {
    var total = 0.0
    for (chance in this) {
        total += chance.chance
    }
    val realChance = HashMap<T, Double>()
    for (chance in this) {
        realChance[chance] = chance.chance / total
    }
    return realChance
}

fun <T: IChance> Collection<T>.realChanceFormatted(item: T): HashMap<T, Double> {
    var total = 0.0
    for (chance in this) {
        total += chance.chance
    }
    val realChance = HashMap<T, Double>()
    for (chance in this) {
        realChance[chance] = ((chance.chance / total)*100.0).decimals(2).toDouble()
    }
    return realChance
}