package gg.aquatic.waves.blockbench.reader

import com.google.gson.GsonBuilder
import gg.aquatic.waves.blockbench.reader.data.BBChildren
import gg.aquatic.waves.blockbench.reader.data.BBModel
import gg.aquatic.waves.blockbench.reader.serializer.ChildrenDeserializer
import gg.aquatic.waves.blockbench.reader.serializer.DoubleDeserializer
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

object BlockBenchParser {

    fun readModel(file: File): BBModel? {
        if (file.isDirectory) {
            return null
        }
        val name = file.name
        if (!name.endsWith(".bbmodel")) {
            return null
        }
        val gsonBuilder = GsonBuilder()

        gsonBuilder.registerTypeAdapter(Double::class.java, DoubleDeserializer())
        gsonBuilder.registerTypeAdapter(BBChildren::class.java, ChildrenDeserializer(gsonBuilder))
        val gson = gsonBuilder.create()
        val model: BBModel
        try {
            model = gson.fromJson(FileReader(file), BBModel::class.java)
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        }
        return model
    }

}