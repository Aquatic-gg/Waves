package gg.aquatic.execute.action.impl.logical

import gg.aquatic.execute.Action
import gg.aquatic.execute.Execute
import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey

typealias SmartActionFactory = (clazz: Class<*>) -> SmartAction<*>

abstract class SmartAction<T: Any>(
    override val binder: Class<out T>,
): Action<T> {

    companion object {
        typealias SmartActionRegistry = Registry<String, SmartActionFactory>
        val REGISTRY_KEY = RegistryKey.simple<String, SmartActionFactory>(
            RegistryId("aquatic", "smart-actions")
        )

        val REGISTRY: SmartActionRegistry
            get() = Execute.bootstrapHolder.get(REGISTRY_KEY)
    }
}