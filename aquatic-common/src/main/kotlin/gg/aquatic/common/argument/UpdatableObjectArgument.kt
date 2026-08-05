package gg.aquatic.common.argument

interface UpdatableObjectArgument {

    fun getUpdatedValue(updater: (String) -> String): Any?

}