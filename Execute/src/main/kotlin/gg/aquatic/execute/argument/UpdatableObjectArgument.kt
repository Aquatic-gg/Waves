package gg.aquatic.execute.argument

interface UpdatableObjectArgument {

    fun getUpdatedValue(updater: (String) -> String): Any?

}