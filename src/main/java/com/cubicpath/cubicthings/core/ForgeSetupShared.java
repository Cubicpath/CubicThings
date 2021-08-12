////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.core.init.CommandInit;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.function.Consumer;

@EventBusSubscriber(modid = CubicThings.MODID)
public final class ForgeSetupShared {

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event){
        for (Consumer<CommandDispatcher<CommandSourceStack>> register: CommandInit.COMMAND_ENTRIES) register.accept(event.getDispatcher());
    }

}