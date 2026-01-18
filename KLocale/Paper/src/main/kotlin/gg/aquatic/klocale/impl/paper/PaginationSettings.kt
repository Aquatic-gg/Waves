package gg.aquatic.klocale.impl.paper

import net.kyori.adventure.text.Component

data class PaginationSettings(
    val pageSize: Int,
    val header: Component? = null,
    val footer: Component? = null
)