package gg.aquatic.blokk

import gg.aquatic.blokk.factory.BlockFactory
import gg.aquatic.blokk.factory.VanillaFactory
import gg.aquatic.blokk.impl.VanillaBlock
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

object AquaticBlockSerializer {

    fun loadMultiBlock(section: ConfigurationSection): AquaticMultiBlock {
        val layersSection = section.getConfigurationSection("layers")!!
        val ingredientsSection = section.getConfigurationSection("blocks")!!

        val ingredients = HashMap<Char, AquaticBlock>()

        for (key in ingredientsSection.getKeys(false)) {
            val block = load(ingredientsSection.getConfigurationSection(key)!!)
            ingredients[key.toCharArray().first()] = block
        }
        val layers = HashMap<Int, MutableMap<Int, String>>()
        for (key in layersSection.getKeys(false)) {
            val layer = layersSection.getConfigurationSection(key)!!
            val layerBlocks = HashMap<Int, String>()
            for (layerKey in layer.getKeys(false)) {
                layerBlocks[layerKey.toInt()] = layer.getString(layerKey)!!
            }
            layers[key.toInt()] = layerBlocks
        }
        return AquaticMultiBlock(BlockShape(layers, ingredients))
    }

    fun load(section: ConfigurationSection): AquaticBlock {
        val material = section.getString("material", "STONE")!!.uppercase()

        for ((id, factory) in BlockFactory.REGISTRY.getAll()) {
            if (material.startsWith("$id:")) {
                val block = factory.load(section, material.substringAfter("$id:"))
                if (block == null) {
                    Bukkit.getLogger().warning("Failed to load block $material")
                    return VanillaBlock(Material.STONE.createBlockData())
                }
                return block
            }
        }
        return VanillaFactory.load(section, material)
    }

}