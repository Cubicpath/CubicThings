////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings;

import com.cubicpath.cubicthings.core.init.*;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
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
    public static final Logger LOGGER = LogManager.getLogger();

    /** Same as mods.toml modid. */
    public static final String MODID = "cubicthings";

    public CubicThings() {
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
        //containers
        //biomes
        //dimensions
        CommandInit.registerCommands(); LOGGER.info("Commands Registered");
        NetworkInit.registerPackets(); LOGGER.info("Network logic Registered");

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
