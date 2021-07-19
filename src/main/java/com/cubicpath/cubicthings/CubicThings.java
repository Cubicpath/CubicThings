////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings;

import com.cubicpath.cubicthings.client.gui.screen.ConfigScreen;
import com.cubicpath.cubicthings.core.init.*;

import com.google.common.collect.Lists;
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
    /**
     * Contains changeable values stored in a config file.
     *
     * @see ForgeConfigSpec
     */
    public static final class Config {
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.ConfigValue<String> stringValue;
        public static final ForgeConfigSpec.ConfigValue<Integer> integerValue;
        public static final ForgeConfigSpec.ConfigValue<Double> doubleValue;
        public static final ForgeConfigSpec.ConfigValue<Boolean> booleanValue;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

            stringValue = builder.comment("This is a String value. It has quotation marks.").translation("config.cubicthings.stringValue").define(Lists.newArrayList("stringValue"), () -> "Value", o -> o != null && String.class.isAssignableFrom(o.getClass()), String.class);
            integerValue = builder.comment("This is an Integer value. It is a whole number.").translation("config.cubicthings.integerValue").define(Lists.newArrayList("integerValue"), () -> 0, o -> o != null && Integer.class.isAssignableFrom(o.getClass()), Integer.class);
            doubleValue = builder.comment("This is a double value. It has decimals.").translation("config.cubicthings.doubleValue").define(Lists.newArrayList("doubleValue"), () -> 1.000D, o -> o != null && Double.class.isAssignableFrom(o.getClass()), Double.class);
            booleanValue = builder.comment("This is a boolean value. It has 2 possible values.").translation("config.cubicthings.booleanValue").define(Lists.newArrayList("booleanValue"), () -> true, o -> o != null && Boolean.class.isAssignableFrom(o.getClass()), Boolean.class);

            SPEC = builder.build();
        }
    }

    public static final Logger LOGGER = LogManager.getLogger();

    /** Same as mods.toml modid. */
    public static final String MODID = "cubicthings";

    public static final String MODNAME = "Cubic Things";
    public static final String MODVER = "0.2.5-alpha";

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
