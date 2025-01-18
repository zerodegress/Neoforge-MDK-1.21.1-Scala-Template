package com.example.examplemod

import net.neoforged.fml.common.EventBusSubscriber
import net.minecraft.world.item.Item
import net.neoforged.neoforge.common.ModConfigSpec
import net.minecraft.core.registries.BuiltInRegistries
import net.neoforged.fml.event.config.ModConfigEvent
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import java.util.stream.Collectors

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = ExampleMod.MODID, bus = EventBusSubscriber.Bus.MOD)
class Config

object Config {
    var logDirtBlock: Option[Boolean] = None
    var magicNumber: Option[Int] = None
    var magicNumberIntroduction: Option[String] = None
    var items: Option[java.util.Set[Item]] = None

    private final val BUILDER = new ModConfigSpec.Builder()

    private final val LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true)

    private final val MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE)

    private final val MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ")

    // a list of strings that are treated as resource locations for items
    private final val ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", java.util.List.of("minecraft:iron_ingot"), Config.validateItemName)

    private[examplemod] final val SPEC = BUILDER.build()

    private def validateItemName(obj: Object) = 
        obj match {
                case itemName: String if BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName)) => 
                        true
                case _ => 
                        false
        }

    @SubscribeEvent
    private[examplemod] def onLoad(event: ModConfigEvent) = {
        logDirtBlock = Some(LOG_DIRT_BLOCK.get())
        magicNumber = Some(MAGIC_NUMBER.get())
        magicNumberIntroduction = Some(MAGIC_NUMBER_INTRODUCTION.get())

        // convert the list of strings into a set of items
        items = Some(ITEM_STRINGS.get()
                .stream()
                .map(itemName => BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet()))
    }
}