////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.event;

import com.cubicpath.cubicthings.core.init.EnchantmentInit;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MagmaVisionFogEvent {

    @SubscribeEvent
    public static void magmaVisionFogEvent(EntityViewRenderEvent.FogDensity event){
        ClientPlayerEntity player = ((ClientPlayerEntity)event.getInfo().getRenderViewEntity());
        FluidState renderFluidState = event.getInfo().getFluidState();

        if (renderFluidState.isTagged(FluidTags.LAVA)){
            int enchLvl = EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.MAGMA_VISION.get(), player.getItemStackFromSlot(EquipmentSlotType.HEAD));
            if (enchLvl >= 1){
                event.setCanceled(true); // Level effects are: 0.66x density (I), 0.33x density (II), 0.18x density (III)
                event.setDensity((event.getDensity() / ((enchLvl * ((float)enchLvl / 2)) + 1)));
            }
        }
    }
}
