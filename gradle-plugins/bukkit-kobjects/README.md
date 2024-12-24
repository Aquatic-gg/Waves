# Bukkit KObjects
A Gradle plugin that allows using Kotlin `object`s for Bukkit's `JavaPlugin`.

Traditionally, if you wanted to create a singleton for your Bukkit plugin, you would have to do something like:

🤮
```kt
class MyPlugin : JavaPlugin() {

    companion object {
        private lateinit var INSTANCE: MyPlugin

        fun getInstance() = INSTANCE
    }

    init {
        INSTANCE = this
    }

    // ...
}

// somewhere else:
Bukkit.getScheduler().runTaskLater(MyPlugin.getInstance(), { ... }, 10)
```

Then, to access your instance, you would have to do `MyPlugin.getInstance()` every time.

Kotlin has a very neat feature, called `object`s, which are basically singletons that are initialized once. It also provides nice syntactic sugar to make your code look neater. 

However, you cannot use `object`s with JavaPlugins, because objects have private constructors and are initialized beforehand. This does not go along well with the Bukkit plugin lifecycle.

This Gradle plugin solves this problem, and allows you to write:

🥰🌟
```kt
object MyPlugin : JavaPlugin() {
    // ...
}

// somewhere else:
Bukkit.getScheduler().runTaskLater(MyPlugin, { ... }, 10)
```

Which is a lot more Kotlin-ish!

# Usage
Usage is very simple: Simply add the Gradle plugin:
```groovy
plugins {
  id("io.github.revxrsal.bukkitkobjects") version "0.0.3"
}
```

Then, add your object class:
```groovy
bukkitKObjects {
    classes.add("com.example.plugin.MyPlugin")
}
```

That's it! You're ready to use the plugin and enjoy idiomatic Kotlin syntax.