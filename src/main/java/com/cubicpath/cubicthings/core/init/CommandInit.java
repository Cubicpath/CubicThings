////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.init;

import com.cubicpath.cubicthings.common.command.*;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public final class CommandInit {
    private CommandInit(){
        throw new IllegalStateException();
    }

    public static List<Consumer<CommandDispatcher<CommandSource>>> commandEntries = new LinkedList<>();

    public static void registerCommands() {
        commandEntries.add(BurnCommand::register);
        commandEntries.add(ExtinguishCommand::register);
        commandEntries.add(HatCommand::register);
        commandEntries.add(HealCommand::register);
        commandEntries.add(HurtCommand::register);
    }

}
