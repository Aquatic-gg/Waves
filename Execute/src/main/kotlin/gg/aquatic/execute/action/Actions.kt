package gg.aquatic.execute.action

import gg.aquatic.execute.Action
import gg.aquatic.execute.ExecutableObjectHandle
import java.util.concurrent.ConcurrentHashMap

val ACTIONS = ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Action<*>>>()

typealias Action<T> = ExecutableObjectHandle<T, Unit>