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
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;

public final class ExtinguishCommand {
    public static final String COMMAND_NAME = "extinguish";

    public static void register(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal(COMMAND_NAME).requires((context) -> {
            return context.hasPermissionLevel(2);
        }).executes((context) -> {
            // Extinguishes source Entity if {targets} is not specified
            return removeFire(context, ImmutableList.of(context.getSource().assertIsEntity()));
        }).then(Commands.argument("targets", EntityArgument.entities()).executes((context) -> {
            // Extinguishes {targets}
            return removeFire(context, EntityArgument.getEntities(context, "targets"));
        })));
    }

    private static int removeFire(CommandContext<CommandSource> context, Collection<? extends Entity> targets){
        int i = 0;
        for (Entity entity: targets){
            boolean wasOnFire = entity.isBurning();
            entity.extinguish();
            i = i + (!entity.isBurning() && wasOnFire ? 1 : 0);
        }

        // Send feedback to player
        if (i == 1) {
            context.getSource().sendFeedback(new TranslationTextComponent("commands.extinguish.success.single", targets.iterator().next().getDisplayName()), true);
        } else {
            context.getSource().sendFeedback(new TranslationTextComponent("commands.extinguish.success.multiple", i), true);
        }

        return targets.size();
    }
}
