package gg.aquatic.blokk


class BlockShape(
    val layers: MutableMap<Int, MutableMap<Int, String>>,
    val blocks: MutableMap<Char, AquaticBlock>
)