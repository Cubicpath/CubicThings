////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings;

import com.cubicpath.cubicthings.client.gui.screen.ConfigScreen;
import com.cubicpath.cubicthings.core.init.*;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The mod's {@link EventBusSubscriber} and main Class. Contains constants and setup methods.
 *
 * @see Mod
 */
@Mod(CubicThings.MODID)
@EventBusSubscriber(modid = CubicThings.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class CubicThings {
    public static class Config {
        private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.ConfigValue<String> stringValue;
        public static final ForgeConfigSpec.ConfigValue<Integer> integerValue;
        public static final ForgeConfigSpec.ConfigValue<Double> doubleValue;

        static {
            BUILDER.push("Config for " + CubicThings.MODNAME);

            stringValue = BUILDER.comment("This is a String value. It has quotation marks.").define("stringValue", "Value");
            integerValue = BUILDER.comment("This is an Integer value. It is a whole number.").define("integerValue", 0);
            doubleValue = BUILDER.comment("This is a double value. It has decimals.").define("doubleValue", 1.000D);

            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }

    public static final Logger LOGGER = LogManager.getLogger();

    /** Same as mods.toml modid. */
    public static final String MODID = "cubicthings";

    public static final String MODNAME = "Cubic Things";
    public static final String MODVER = "0.2.4";
    public static final String MCVER = "1.16.5";

    public CubicThings() {
        final ModLoadingContext modLoadingContext = ModLoadingContext.get();
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Event Listeners
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::processIMC);

        // Register Stuff
        BlockInit.BLOCKS.register(modEventBus); LOGGER.info("Blocks Registered");
        ItemInit.ITEMS.register(modEventBus); LOGGER.info("Items Registered");
        EnchantmentInit.ENCHANTMENTS.register(modEventBus); LOGGER.info("Enchantments Registered");
        //entities
        //tile entities
        ContainerInit.CONTAINER_TYPES.register(modEventBus); LOGGER.info("Container Types Registered");
        //biomes
        //dimensions
        CommandInit.registerCommands(); LOGGER.info("Commands Registered");
        NetworkInit.registerPackets(); LOGGER.info("Network logic Registered");

        modLoadingContext.registerConfig(ModConfig.Type.COMMON, Config.SPEC, MODID + "-common.toml");
        modLoadingContext.getActiveContainer().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (__, screen) -> new ConfigScreen(screen));
    }

    // FML Setup Events
    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("FML Setting Up...");
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        LOGGER.info("Enqueue IMC Event...");
    }

    private void processIMC(final InterModProcessEvent event) {
        LOGGER.info("Process IMC Event...");
    }

}
