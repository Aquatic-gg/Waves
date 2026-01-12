package gg.aquatic.blokk

import gg.aquatic.common.toBlockCardinal
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.util.Vector
import kotlin.math.floor

class AquaticMultiBlock(
    val shape: BlockShape
) {

    fun spawn(location: Location, vararg ignoredChars: Char): HashMap<Location, BlockData> {
        val locations = HashMap<Location,BlockData>()

        processLayerCells(location) { char, newLoc ->
            if (!ignoredChars.contains(char)) {
                val block = shape.blocks[char]
                if (block != null) {
                    block.place(newLoc)
                    locations += newLoc to block.blockData
                }
            }
        }

        return locations
    }

    fun processLayerCells(
        location: Location,
        operation: (Char, Location) -> Unit
    ) {
        val face = location.yaw.toBlockCardinal()
        for (layer in shape.layers) {
            val y = layer.key
            val l = layer.value
            for (s in l) {
                val z = s.key
                val chars = s.value.toCharArray()
                if (chars.size % 2 == 0) continue
                var x = -floor(chars.size / 2.0).toInt() - 1
                for (char in chars) {
                    x++
                    val vector = Vector(x, y, z)
                    vector.rotateAroundY(-Math.toRadians(face.ordinal * 90.0))
                    val newLoc = location.block.location.clone().add(vector)

                    operation.invoke(char, newLoc)
                }
            }
        }
    }
}