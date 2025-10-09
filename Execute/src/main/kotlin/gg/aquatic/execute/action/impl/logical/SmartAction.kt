package gg.aquatic.execute.action.impl.logical

import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.Action

abstract class SmartAction<T: Any>(
    val clazz: Class<T>,
    val classTransforms: Collection<ClassTransform<T, *>>
): Action<T>