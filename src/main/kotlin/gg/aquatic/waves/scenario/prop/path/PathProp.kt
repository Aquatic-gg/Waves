package gg.aquatic.waves.scenario.prop.path

import gg.aquatic.waves.scenario.prop.Moveable
import java.util.TreeMap

interface PathProp {

    val points: TreeMap<Int, PathPoint>
    val currentPoint: PathPoint


    val boundProps: MutableMap<Moveable, PathBoundProperties>

}