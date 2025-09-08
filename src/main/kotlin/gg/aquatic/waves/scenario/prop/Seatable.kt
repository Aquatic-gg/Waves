package gg.aquatic.waves.scenario.prop

interface Seatable {

    val entityId: Int

    fun addPassenger(passenger: Passenger)
    fun removePassenger(passenger: Passenger)
}