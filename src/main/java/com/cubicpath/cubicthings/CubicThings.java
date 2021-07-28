////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings;

import com.cubicpath.cubicthings.client.gui.screen.ConfigScreen;
import com.cubicpath.cubicthings.core.init.*;

import com.electronwill.nightconfig.core.AbstractConfig;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The mod's {@link EventBusSubscriber} and main Class. Contains constants and setup methods.
 *
 * @see Mod
 */
@Mod(CubicThings.MODID)
@EventBusSubscriber(modid = CubicThings.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class CubicThings {
    /**
     * Contains changeable values stored in a config file. Uses reflection to assign values.
     *
     * @see ForgeConfigSpec
     */
    @SuppressWarnings("unused")
    public static final class Config {
        public static final ForgeConfigSpec SPEC;
        public static ForgeConfigSpec.ConfigValue<Boolean> booleanValue;
        public static ForgeConfigSpec.ConfigValue<Integer> byteValue;
        public static ForgeConfigSpec.ConfigValue<Integer> integerValue;
        public static ForgeConfigSpec.ConfigValue<Double> doubleValue;
        public static ForgeConfigSpec.ConfigValue<String> stringValue;

        public static ForgeConfigSpec.ConfigValue<Boolean> bytesAsBinary;
        public static ForgeConfigSpec.ConfigValue<Integer> fail;
        public static ForgeConfigSpec.ConfigValue<Integer> good;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            buildValue(Config.class, builder, "booleanValue", true, "This is a Boolean value. It has 2 possible values.", Boolean.class);
            buildValue(Config.class, builder, "byteValue", (byte) 0, "This is an Integer value. It is a whole number.", Byte.class);
            buildValue(Config.class, builder, "integerValue", 0, "This is an Integer value. It is a whole number.", Integer.class);
            buildValue(Config.class, builder, "doubleValue", 1.000D, "This is a Double value. It has decimals.", Double.class);
            buildValue(Config.class, builder, "stringValue", "Value", "This is a String value. It has quotation marks.", String.class);
            builder.push("configMenu");
            buildValue(Config.class, builder, "bytesAsBinary", true, "Show byte config values as binary strings.", Boolean.class);
            builder.push("color");
            buildValue(Config.class, builder, "fail", 0xBB4444, null, Integer.class);
            buildValue(Config.class, builder, "good", 0x44BA44, "", Integer.class);
            builder.pop(2);
            SPEC = builder.build();
        }

        static <T> void buildValue(Class<?> configClazz, ForgeConfigSpec.Builder builder, String fieldName, T defaultValue, @Nullable String comment, Class<T> valueClazz){
            try {
                List<String> path = Lists.newArrayList(fieldName);
                Supplier<T> defaultSupplier = () -> defaultValue;
                Predicate<Object> validator = (o) -> o != null && valueClazz.isAssignableFrom(o.getClass());
                if (comment != null) builder.comment(comment);
                configClazz.getField(fieldName).set(null, builder.translation("config." + MODID + "." + fieldName).define(path, defaultSupplier, validator, valueClazz));
            } catch (NoSuchFieldException | IllegalAccessException exception){
                LOGGER.warn("Unable to instantiate configValue '" + fieldName + "' using reflection. Reason: " +  exception);
            }
        }

        public static boolean isValueSpecInConfigSpec(ForgeConfigSpec spec, String[] path){
            return isValueSpecInPath(spec.valueMap(), path, 0);
        }

        private static boolean isValueSpecInPath(Map<String, Object> valueMap, String[] path, final int depth){
            try {
                Object o = valueMap.get(path[depth]);
                AbstractConfig config = o instanceof AbstractConfig ? (AbstractConfig) o : null;
                return (o instanceof ForgeConfigSpec.ValueSpec && depth == path.length - 1) || (config != null && isValueSpecInPath(config.valueMap(), path, depth + 1));
            } catch (ArrayIndexOutOfBoundsException e){
                return false;
            }
        }
    }

    public static final Logger LOGGER = LogManager.getLogger();

    /** Same as mods.toml modid. */
    public static final String MODID = "cubicthings";

    public static final String MODNAME = "Cubic Things";
    public static final String MODVER = "0.2.6";

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
