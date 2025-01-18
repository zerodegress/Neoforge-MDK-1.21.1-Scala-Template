package com.example.examplemod

import com.mojang.logging.LogUtils
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.fml.common.Mod
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.item.Item
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTabs
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Blocks
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist


// The constructor for the mod class is the first code that is run when your mod is loaded.
// FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
@Mod(ExampleMod.MODID)
class ExampleMod(val modEventBus: IEventBus, val modContainer: ModContainer) {
    import com.example.examplemod.ExampleMod.LOGGER
    import com.example.examplemod.ExampleMod.BLOCKS
    import com.example.examplemod.ExampleMod.ITEMS
    import com.example.examplemod.ExampleMod.CREATIVE_MODE_TABS
    import com.example.examplemod.ExampleMod.EXAMPLE_BLOCK_ITEM

    // Register the commonSetup method for modloading
    modEventBus.addListener(this.commonSetup);

    // Register the Deferred Register to the mod event bus so blocks get registered
    BLOCKS.register(modEventBus);
    // Register the Deferred Register to the mod event bus so items get registered
    ITEMS.register(modEventBus);
    // Register the Deferred Register to the mod event bus so tabs get registered
    CREATIVE_MODE_TABS.register(modEventBus);

    // Register ourselves for server and other game events we are interested in.
    // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
    // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
    NeoForge.EVENT_BUS.register(this);

    // Register the item to a creative tab
    modEventBus.addListener(this.addCreative);

    // Register our mod's ModConfigSpec so that FML can create and load the config file for us
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    private def commonSetup(event: FMLCommonSetupEvent) = {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock.get)
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction.get + Config.magicNumber.get);

        Config.items.get.forEach((item) => LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add the example block item to the building blocks tab
    private def addCreative(event: BuildCreativeModeTabContentsEvent) = {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    def onServerStarting(event: ServerStartingEvent) = {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}

// The value here should match an entry in the META-INF/neoforge.mods.toml file
object ExampleMod {
    // Define mod id in a common place for everything to reference
    final val MODID = "examplemod"
    // Directly reference a slf4j logger
    final val LOGGER = LogUtils.getLogger()
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    final val BLOCKS = DeferredRegister.createBlocks(MODID)
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    final val ITEMS = DeferredRegister.createItems(MODID)
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    final val CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)
    
    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
    final val EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE))
    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
    final val EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK)
    
    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
    final val EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()))

    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    final val EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () => CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.examplemod")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() => EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) => {
                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build())

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Array(Dist.CLIENT))
    class ClientModEvents

    object ClientModEvents {
        @SubscribeEvent
        def onClientSetup(event: FMLClientSetupEvent) = {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}