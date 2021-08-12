////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.init;

import com.cubicpath.cubicthings.common.command.*;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public final class CommandInit {
    private CommandInit(){
        throw new IllegalStateException();
    }

    public static final List<Consumer<CommandDispatcher<CommandSourceStack>>> COMMAND_ENTRIES = new LinkedList<>();

    public static void registerCommands() {
        COMMAND_ENTRIES.add(BurnCommand::register);
        COMMAND_ENTRIES.add(ExtinguishCommand::register);
        COMMAND_ENTRIES.add(HatCommand::register);
        COMMAND_ENTRIES.add(HealCommand::register);
        COMMAND_ENTRIES.add(HurtCommand::register);
        COMMAND_ENTRIES.add(ListRegistryCommand::register);
    }

}
