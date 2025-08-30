package gg.aquatic.waves.scenario

interface ScenarioProp {

    val scenario: Scenario

    fun tick()
    fun onEnd()
}