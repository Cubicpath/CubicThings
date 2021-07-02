////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.event;

import com.cubicpath.cubicthings.core.init.EnchantmentInit;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MagmaVisionFogEvent {

    @SubscribeEvent
    public static void magmaVisionFogDensityEvent(EntityViewRenderEvent.FogDensity event){
        ClientPlayerEntity player = ((ClientPlayerEntity)event.getInfo().getRenderViewEntity());
        FluidState renderFluidState = event.getInfo().getFluidState();

        if (renderFluidState.isTagged(FluidTags.LAVA)){
            float density = player.isPotionActive(Effects.BLINDNESS) ? event.getDensity() * 5.0F : event.getDensity();
            int enchLvl = EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.MAGMA_VISION.get(), player.getItemStackFromSlot(EquipmentSlotType.HEAD));
            if (enchLvl >= 1){
                event.setCanceled(true); // Level effects are: 0.66x density (I), 0.33x density (II), 0.18x density (III)
                event.setDensity(density / ((enchLvl * ((float)enchLvl / 2)) + 1));
            }
        }
    }

    @SubscribeEvent
    public static void magmaVisionFogColorEvent(EntityViewRenderEvent.FogColors event) {
        ClientPlayerEntity player = ((ClientPlayerEntity)event.getInfo().getRenderViewEntity());
        FluidState renderFluidState = event.getInfo().getFluidState();

        if (renderFluidState.isTagged(FluidTags.LAVA)) {
            int enchLvl = EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.MAGMA_VISION.get(), player.getItemStackFromSlot(EquipmentSlotType.HEAD));
            if (enchLvl >= 1){
                if (player.isPotionActive(Effects.BLINDNESS)){
                    event.setRed(0.3F);
                    event.setGreen(0F);
                    event.setBlue(0.05F);
                }
            }
        }
    }

}
