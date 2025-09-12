package gg.aquatic.waves.util.action.impl.logical

import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.generic.ClassTransform

abstract class SmartAction<T: Any>(
    val clazz: Class<T>,
    val classTransforms: Collection<ClassTransform<T,*>>
): Action<T>