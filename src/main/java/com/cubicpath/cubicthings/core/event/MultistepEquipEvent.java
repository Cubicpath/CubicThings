////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.event;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.enchantment.MultistepEnchantment;
import com.cubicpath.cubicthings.core.init.EnchantmentInit;
import com.cubicpath.cubicthings.core.init.NetworkInit;
import com.cubicpath.cubicthings.common.network.SStepHeightSyncPacket;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.EquipmentSlot;
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
        final var eventEntity = event.getEntityLiving();
        final var eventEntityPlayer = Objects.requireNonNull(eventEntity.getCommandSenderWorld().getServer(), "Entity's world's server cannot be null.").getPlayerList().getPlayer(eventEntity.getUUID());
        final var oldEquip = event.getFrom();
        final var newEquip = event.getTo();
        final var oldStepHeight = eventEntity.maxUpStep;

        // Don't run if equipped in hands
        if (event.getSlot().getType() != EquipmentSlot.Type.HAND){

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
