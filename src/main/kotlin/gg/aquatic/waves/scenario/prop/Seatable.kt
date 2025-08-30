package gg.aquatic.waves.scenario.prop

import gg.aquatic.waves.scenario.ScenarioProp

interface Seatable {

    fun addPassenger(entityAnimationProp: ScenarioProp)
    fun removePassenger(entityAnimationProp: ScenarioProp)
}