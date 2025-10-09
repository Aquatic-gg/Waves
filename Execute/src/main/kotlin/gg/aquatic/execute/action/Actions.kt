package gg.aquatic.execute.action

import gg.aquatic.execute.Action
import java.util.concurrent.ConcurrentHashMap

val ACTIONS = ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Action<*>>>()