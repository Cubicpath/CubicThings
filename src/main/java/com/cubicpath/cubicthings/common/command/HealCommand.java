////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Collection;

public final class HealCommand {
    public static final String COMMAND_NAME = "heal";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(COMMAND_NAME).requires((context) -> {
            return context.hasPermission(2);
        }).executes((context) -> {
            // Heals source Player if {targets} is not specified
            return heal(context, ImmutableList.of(context.getSource().getPlayerOrException()), null);
        }).then(Commands.argument("targets", EntityArgument.entities()).executes((context) -> {
            // Heals {targets} to max health if {amount} is not specified
            return heal(context, EntityArgument.getEntities(context, "targets"), null);
        }).then(Commands.argument("amount", FloatArgumentType.floatArg()).executes(((context) -> {
            // Heals {targets} for {amount} each
            return heal(context, EntityArgument.getEntities(context, "targets"), FloatArgumentType.getFloat(context, "amount"));
        })))));
    }

    private static int heal(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets, @Nullable Float amount){
        for (Entity entity: targets){
            LivingEntity livingEntity = entity instanceof LivingEntity ? ((LivingEntity)entity) : null;
            if (livingEntity != null) {
                livingEntity.heal(amount != null ? amount : livingEntity.getMaxHealth());
                amount = amount != null ? amount : livingEntity.getMaxHealth();
            }
        }

        // Send feedback to player
        if (targets.size() == 1) {
            context.getSource().sendSuccess(new TranslatableComponent("commands.heal.success.single", targets.iterator().next().getDisplayName(), amount), true);
        } else {
            context.getSource().sendSuccess(new TranslatableComponent("commands.heal.success.multiple", targets.size(), amount), true);
        }

        return targets.size();
    }
}
