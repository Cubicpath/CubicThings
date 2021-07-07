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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Objects;

/** Fires server-side only, communicates changes to client using {@link SStepHeightSyncPacket} */
@EventBusSubscriber(modid = CubicThings.MODID)
public class MultistepEquipEvent {

    private static boolean isValidItem(ItemStack item) {
        return EnchantmentInit.MULTISTEP.get().canApplyAtEnchantingTable(item) && item.isEnchanted();
    }

    @SubscribeEvent
    public static void multistepEquipEvent(final LivingEquipmentChangeEvent event){
        final LivingEntity eventEntity = event.getEntityLiving();
        final ServerPlayerEntity eventPlayer = Objects.requireNonNull(eventEntity.getEntityWorld().getServer(), "Entity's world's server cannot be null.").getPlayerList().getPlayerByUUID(eventEntity.getUniqueID());
        final ItemStack oldEquip = event.getFrom();
        final ItemStack newEquip = event.getTo();
        final float oldStepHeight = eventEntity.stepHeight;

        // Don't run if equipped in hands
        if (event.getSlot().getSlotType() != EquipmentSlotType.Group.HAND){

            // Un-equip enchantment
            if (isValidItem(oldEquip)){
                int stepLvl = EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.MULTISTEP.get(), oldEquip);
                if (stepLvl != 0){
                    eventEntity.stepHeight -= stepLvl * MultistepEnchantment.STEP_FACTOR;
                }
            }
            // Equip enchantment
            if (isValidItem(newEquip)){
                int stepLvl = EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.MULTISTEP.get(), newEquip);
                if (stepLvl != 0){
                    eventEntity.stepHeight += stepLvl * MultistepEnchantment.STEP_FACTOR;
                }
            }
            // Sync the client
            if (eventEntity.stepHeight != oldStepHeight && eventPlayer != null){
                NetworkInit.PACKET_HANDLER.sendToPlayer(eventPlayer, new SStepHeightSyncPacket(eventPlayer.stepHeight));
            }

        }
    }
}
