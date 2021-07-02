////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.Collection;

public final class HurtCommand {
    public static final String COMMAND_NAME = "hurt";
    public static final float DEFAULT_DAMAGE = 1.0F;

    public static void register(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal(COMMAND_NAME).requires((context) -> {
            return context.hasPermissionLevel(2);
        }).executes((context) -> {
            // Heals source Entity if {targets} is not specified
            return hurt(context, ImmutableList.of(context.getSource().assertIsEntity()), null);
        }).then(Commands.argument("targets", EntityArgument.entities()).executes((context -> {
            // Heals {targets} to max health if {amount} is not specified
            return hurt(context, EntityArgument.getEntities(context, "targets"), null);
        })).then(Commands.argument("amount", FloatArgumentType.floatArg()).executes((context -> {
            // Heals {targets} for {amount} each
            return hurt(context, EntityArgument.getEntities(context, "targets"), FloatArgumentType.getFloat(context, "amount"));
        })))));
    }

    private static int hurt(CommandContext<CommandSource> context, Collection<? extends Entity> targets, @Nullable Float amount) throws CommandSyntaxException {
        amount = amount != null ? amount : DEFAULT_DAMAGE; // Default amount
        for (Entity entity: targets){
            LivingEntity livingEntity = entity instanceof LivingEntity ? ((LivingEntity)entity) : null;
            if (livingEntity != null) livingEntity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(context.getSource().assertIsEntity(), livingEntity), amount);
        }

        // Send feedback to player
        if (targets.size() == 1) {
            context.getSource().sendFeedback(new TranslationTextComponent("commands.hurt.success.single", targets.iterator().next().getDisplayName(), amount), true);
        } else {
            context.getSource().sendFeedback(new TranslationTextComponent("commands.hurt.success.multiple", targets.size(), amount), true);
        }

        return targets.size();
    }
}