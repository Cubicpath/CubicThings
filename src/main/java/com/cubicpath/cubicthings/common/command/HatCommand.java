////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

public final class HatCommand {
    private static final SimpleCommandExceptionType PERMISSION_EXCEPTION = new SimpleCommandExceptionType(new TranslatableComponent("commands.hat.failure.permission"));
    public static final String COMMAND_NAME = "hat";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(COMMAND_NAME).executes((context) -> {
            // Gives you a hat from your mainhand
            return placeHat(context, ImmutableList.of(context.getSource().getPlayerOrException()), null);
        }).then(Commands.argument("targets", EntityArgument.entities()).executes((context) -> {
            // Gets hat from mainhand and gives it to {targets}
            return placeHat(context, EntityArgument.getEntities(context, "targets"), null);
        }).then(Commands.argument("item", ItemArgument.item()).executes(((context) -> {
            // Gives {item} hat to {targets}
            return placeHat(context, EntityArgument.getEntities(context, "targets"), ItemArgument.getItem(context, "item").createItemStack(1,false));
        })))));
    }

    private static int placeHat(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets, @Nullable ItemStack stack) throws CommandSyntaxException {
        int i = 0;
        if (stack == null) stack = context.getSource().getPlayerOrException().getMainHandItem();
        for (Entity entity: targets){
            if (!entity.is(Objects.requireNonNull(context.getSource().getEntity()))) throw PERMISSION_EXCEPTION.create();
            if (entity instanceof LivingEntity && !stack.isEmpty()){
                if (!((LivingEntity) entity).getItemBySlot(EquipmentSlot.HEAD).isEmpty()){
                    entity.level.addFreshEntity(new ItemEntity(entity.level, entity.getX(), entity.getEyeY(), entity.getZ(), ((LivingEntity) entity).getItemBySlot(EquipmentSlot.HEAD)));
                    entity.level.playSound(entity instanceof Player ? (Player)entity : null, entity.getOnPos(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 1.0F, (entity.level.random.nextFloat() - entity.level.random.nextFloat()) * 0.4F + 1.0F);
                }
                entity.setItemSlot(EquipmentSlot.HEAD, stack);
                if (!context.getSource().hasPermission(2)) entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                i++;
            }
        }

        // Send feedback to player
        if (i == 1) {
            context.getSource().sendSuccess(new TranslatableComponent("commands.hat.success.single", stack.getDisplayName(), targets.iterator().next().getDisplayName()), true);
        } else {
            context.getSource().sendSuccess(new TranslatableComponent("commands.hat.success.multiple", stack.getDisplayName(), i), true);
        }

        return i;
    }
}
