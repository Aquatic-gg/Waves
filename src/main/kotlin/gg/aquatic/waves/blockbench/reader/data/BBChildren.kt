package gg.aquatic.waves.blockbench.reader.data

import java.util.UUID

interface BBChildren {

    class BBBoneChildren(
        val uuid: UUID,
        val children: MutableList<BBChildren>
    ): BBChildren {
    }

    class BBElementChildren(
        val uuid: UUID
    ): BBChildren {
    }
}