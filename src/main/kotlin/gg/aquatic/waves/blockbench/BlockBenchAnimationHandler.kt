package gg.aquatic.waves.blockbench

import gg.aquatic.waves.Waves
import gg.aquatic.waves.blockbench.handle.BBPart
import gg.aquatic.waves.blockbench.handle.BBTemplateHandle
import gg.aquatic.waves.blockbench.reader.BlockBenchReader
import gg.aquatic.waves.blockbench.template.BBTemplate
import gg.aquatic.waves.util.runAsyncTimer
import org.bukkit.Location

object BlockBenchAnimationHandler {

    val templates = HashMap<String, BBTemplate>()
    val spawned = HashSet<BBTemplateHandle>()

    fun initialize() {
        runAsyncTimer(1, 1) {
            for (handle in spawned) {
                handle.tick()
            }
        }
        loadTemplates()
    }

    fun spawn(location: Location, id: String, onPartTick: (BBPart) -> Unit, animation: String?): BBTemplateHandle? {
        val template = templates[id] ?: return null
        val handle = template.spawn(location, { }, onPartTick)
        animation?.let {
            handle.animationHandler.playAnimation(it, 1.0)
        }
        spawned.add(handle)
        return handle
    }

    fun loadTemplates() {
        templates.clear()
        val dataFolder = Waves.INSTANCE.dataFolder.resolve("bbtemplates")
        dataFolder.mkdirs()
        for (file in dataFolder.listFiles()) {
            val model = BlockBenchReader.read(file)
            if (model == null) {
                println("File " + file.name + " could not be loaded!")
                continue
            }
            templates[model.name] = model
            println("Loaded model " + model.name)
        }
    }
}