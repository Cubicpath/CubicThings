////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.event;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.enchantment.MultistepEnchantment;
import com.cubicpath.cubicthings.core.init.EnchantmentInit;
import com.cubicpath.cubicthings.core.init.NetworkInit;
import com.cubicpath.cubicthings.common.network.SStepHeightSyncPacket;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Objects;

/** Fires server-side only, communicates changes to client using {@link SStepHeightSyncPacket} */
@EventBusSubscriber(modid = CubicThings.MODID)
public final class MultistepEquipEvent {
    private MultistepEquipEvent() {
        throw new IllegalStateException();
    }

    private static boolean isValidItem(ItemStack item) {
        return EnchantmentInit.MULTISTEP.get().canApplyAtEnchantingTable(item) && item.isEnchanted();
    }

    @SubscribeEvent
    public static void multistepEquipEvent(final LivingEquipmentChangeEvent event){
        final Entity eventEntity = event.getEntityLiving();
        final ServerPlayerEntity eventEntityPlayer = Objects.requireNonNull(eventEntity.getCommandSenderWorld().getServer(), "Entity's world's server cannot be null.").getPlayerList().getPlayer(eventEntity.getUUID());
        final ItemStack oldEquip = event.getFrom();
        final ItemStack newEquip = event.getTo();
        final float oldStepHeight = eventEntity.maxUpStep;

        // Don't run if equipped in hands
        if (event.getSlot().getType() != EquipmentSlotType.Group.HAND){

            // Un-equip enchantment
            if (isValidItem(oldEquip)){
                int stepLvl = EnchantmentHelper.getItemEnchantmentLevel(EnchantmentInit.MULTISTEP.get(), oldEquip);
                if (stepLvl != 0){
                    eventEntity.maxUpStep -= stepLvl * MultistepEnchantment.STEP_FACTOR;
                }
            }
            // Equip enchantment
            if (isValidItem(newEquip)){
                int stepLvl = EnchantmentHelper.getItemEnchantmentLevel(EnchantmentInit.MULTISTEP.get(), newEquip);
                if (stepLvl != 0){
                    eventEntity.maxUpStep += stepLvl * MultistepEnchantment.STEP_FACTOR;
                }
            }
            // Sync the client if player
            if (eventEntityPlayer != null && eventEntity.maxUpStep != oldStepHeight){
                NetworkInit.PACKET_HANDLER.sendToPlayer(eventEntityPlayer, new SStepHeightSyncPacket(eventEntity.maxUpStep));
            }

        }
    }
}
