package gg.aquatic.execute.requirement

import gg.aquatic.execute.Condition
import java.util.concurrent.ConcurrentHashMap

val CONDITIONS = ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Condition<*>>>()