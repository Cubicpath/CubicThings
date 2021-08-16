////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.client.gui.screen.ConfigScreen;
import com.cubicpath.cubicthings.client.gui.screen.ScannerScreen;
import com.cubicpath.cubicthings.core.config.Configs;
import com.cubicpath.cubicthings.core.init.ContainerInit;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = CubicThings.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModSetupClient {

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        ModLoadingContext.get().getActiveContainer().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                () -> (minecraft, screen) -> new ConfigScreen(screen, CubicThings.MODNAME + " v" + CubicThings.MODVER, Configs.CUBICTHINGS_CLIENT, Configs.CUBICTHINGS_COMMON, Configs.CUBICTHINGS_CLIENT));

        event.enqueueWork(() -> {
            ScreenManager.register(ContainerInit.SCANNER.get(), ScannerScreen::new);
        });
    }
}
