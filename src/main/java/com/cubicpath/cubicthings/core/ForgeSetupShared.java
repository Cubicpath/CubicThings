////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.core.init.CommandInit;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = CubicThings.MODID)
public class ForgeSetupShared {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event){
        for (Consumer<CommandDispatcher<CommandSource>> register: CommandInit.commandEntries) register.accept(event.getDispatcher());
    }

}