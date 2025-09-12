package gg.aquatic.waves.util.block

import gg.aquatic.waves.util.block.AquaticBlock


class BlockShape(
    val layers: MutableMap<Int, MutableMap<Int, String>>,
    val blocks: MutableMap<Char, AquaticBlock>
)