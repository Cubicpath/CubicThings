////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;

public final class BurnCommand {
    public static final String COMMAND_NAME = "burn";

    public static void register(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal(COMMAND_NAME).requires((context) -> {
            return context.hasPermission(2);
        }).then(Commands.argument("targets", EntityArgument.entities()).executes(context -> {
            // Burns {targets} for 5 seconds if {time} is not specified
            return addFire(context, EntityArgument.getEntities(context, "targets"), 5, false);
        }).then(Commands.argument("time", IntegerArgumentType.integer()).executes((context -> {
            // Burns {targets} entities for {time} seconds if {preferTicks} is not specified
            return addFire(context, EntityArgument.getEntities(context, "targets"), IntegerArgumentType.getInteger(context, "time"), false);
        })).then(Commands.argument("preferTicks", BoolArgumentType.bool()).executes((context -> {
            // Burns {targets} entities for {time} ticks if {preferTicks} is true
            return addFire(context,  EntityArgument.getEntities(context, "targets"), IntegerArgumentType.getInteger(context, "time"), BoolArgumentType.getBool(context, "preferTicks"));
        }))))));
    }

    private static int addFire(CommandContext<CommandSource> context, Collection<? extends Entity> targets, int time, boolean preferTicks){
        for (Entity entity: targets){
            if (preferTicks) {
                entity.setRemainingFireTicks(time);
            } else {
                entity.setSecondsOnFire(time);
            }
        }

        // Send feedback to player
        String timeAmountFormat = (!preferTicks ? "second" : "tick") + (time != 1 ? "s" : "");
        if (targets.size() == 1) {
            context.getSource().sendSuccess(new TranslationTextComponent("commands.burn.success.single", targets.iterator().next().getDisplayName(), time, timeAmountFormat), true);
        } else {
            context.getSource().sendSuccess(new TranslationTextComponent("commands.burn.success.multiple", targets.size(), time, timeAmountFormat), true);
        }

        return targets.size();
    }

}
