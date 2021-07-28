////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.Collection;

public class HatCommand {
    private static final SimpleCommandExceptionType PERMISSION_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.hat.failure.permission"));
    public static final String COMMAND_NAME = "hat";

    public static void register(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal(COMMAND_NAME).executes((context) -> {
            // Gives you a hat from your mainhand
            return placeHat(context, ImmutableList.of(context.getSource().assertIsEntity()), null);
        }).then(Commands.argument("targets", EntityArgument.entities()).executes((context) -> {
            // Gets hat from mainhand and gives it to {targets}
            return placeHat(context, EntityArgument.getEntities(context, "targets"), null);
        }).then(Commands.argument("item", ItemArgument.item()).executes(((context) -> {
            // Gives {item} hat to {targets}
            return placeHat(context, EntityArgument.getEntities(context, "targets"), ItemArgument.getItem(context, "item").createStack(1,false));
        })))));
    }

    private static int placeHat(CommandContext<CommandSource> context, Collection<? extends Entity> targets, @Nullable ItemStack stack) throws CommandSyntaxException {
        int i = 0;
        if (stack == null) stack = context.getSource().asPlayer().getHeldItemMainhand();
        for (Entity entity: targets){
            if (!entity.isEntityEqual(context.getSource().assertIsEntity())) throw PERMISSION_EXCEPTION.create();
            if (entity instanceof LivingEntity && !stack.isEmpty()){
                if (!((LivingEntity) entity).getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()){
                    entity.world.addEntity(new ItemEntity(entity.world, entity.getPosX(), entity.getPosYEye(), entity.getPosZ(), ((LivingEntity) entity).getItemStackFromSlot(EquipmentSlotType.HEAD)));
                    entity.world.playSound(entity instanceof PlayerEntity ? (PlayerEntity)entity : null, entity.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 1.0F, (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.4F + 1.0F);
                }
                entity.setItemStackToSlot(EquipmentSlotType.HEAD, stack);
                if (!context.getSource().hasPermissionLevel(2)) entity.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
                i++;
            }
        }

        // Send feedback to player
        if (i == 1) {
            context.getSource().sendFeedback(new TranslationTextComponent("commands.hat.success.single", stack.getDisplayName(), targets.iterator().next().getDisplayName()), true);
        } else {
            context.getSource().sendFeedback(new TranslationTextComponent("commands.hat.success.multiple", stack.getDisplayName(), i), true);
        }

        return i;
    }
}
