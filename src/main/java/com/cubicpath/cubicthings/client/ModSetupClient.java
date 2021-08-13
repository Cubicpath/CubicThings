////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.client.gui.screen.ConfigScreen;
import com.cubicpath.cubicthings.client.gui.screen.ScannerScreen;
import com.cubicpath.cubicthings.core.init.ContainerInit;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmlclient.ConfigGuiHandler;

@EventBusSubscriber(modid = CubicThings.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModSetupClient {

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        ModLoadingContext.get().getActiveContainer().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ConfigScreen(screen, CubicThings.MODNAME + " v" + CubicThings.MODVER, CubicThings.CONFIG)));

        event.enqueueWork(() -> {
            MenuScreens.register(ContainerInit.SCANNER.get(), ScannerScreen::new);
        });
    }
}
