////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.Entity;

import java.util.Collection;

public final class ExtinguishCommand {
    public static final String COMMAND_NAME = "extinguish";

    public static void register(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal(COMMAND_NAME).requires((context) -> {
            return context.hasPermission(2);
        }).executes((context) -> {
            // Extinguishes source Player if {targets} is not specified
            return removeFire(context, ImmutableList.of(context.getSource().getPlayerOrException()));
        }).then(Commands.argument("targets", EntityArgument.entities()).executes((context) -> {
            // Extinguishes {targets}
            return removeFire(context, EntityArgument.getEntities(context, "targets"));
        })));
    }

    private static int removeFire(CommandContext<CommandSource> context, Collection<? extends Entity> targets){
        int i = 0;
        for (Entity entity: targets){
            boolean wasOnFire = entity.isOnFire();
            entity.clearFire();
            i = i + (!entity.isOnFire() && wasOnFire ? 1 : 0);
        }

        // Send feedback to player
        if (i == 1) {
            context.getSource().sendSuccess(new TranslationTextComponent("commands.extinguish.success.single", targets.iterator().next().getDisplayName()), true);
        } else {
            context.getSource().sendSuccess(new TranslationTextComponent("commands.extinguish.success.multiple", i), true);
        }

        return targets.size();
    }
}
