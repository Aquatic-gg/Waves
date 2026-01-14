package gg.aquatic.execute.action.impl.logical

import gg.aquatic.execute.Action
import gg.aquatic.execute.ClassTransform
import gg.aquatic.kregistry.FrozenRegistry
import gg.aquatic.kregistry.Registry
import gg.aquatic.kregistry.RegistryId
import gg.aquatic.kregistry.RegistryKey

typealias SmartActionFactory = (clazz: Class<*>, classTransforms: Collection<ClassTransform<*, *>>) -> SmartAction<*>

abstract class SmartAction<T: Any>(
    val clazz: Class<T>,
    val classTransforms: Collection<ClassTransform<T, *>>
): Action<T> {

    companion object {
        val REGISTRY_KEY = RegistryKey<String, SmartActionFactory>(
            RegistryId("aquatic", "smart-actions")
        )

        val REGISTRY: FrozenRegistry<String, SmartActionFactory>
            get() = Registry.get(REGISTRY_KEY)
    }
}