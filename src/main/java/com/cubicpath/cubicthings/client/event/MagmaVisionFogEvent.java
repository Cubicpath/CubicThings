////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.event;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.core.init.EnchantmentInit;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = CubicThings.MODID, value = Dist.CLIENT)
public final class MagmaVisionFogEvent {
    private MagmaVisionFogEvent() {
        throw new IllegalStateException();
    }

    //FIXME: Density not changing
    @SubscribeEvent
    public static void magmaVisionFogDensityEvent(final EntityViewRenderEvent.FogDensity event){
        final var player = (LocalPlayer)event.getInfo().getEntity();
        final var fluidState = event.getInfo().getBlockAtCamera().getFluidState();
        //CubicThings.LOGGER.info("Before | Is event cancelled:" + event.isCanceled());
        //CubicThings.LOGGER.info("Before | Fog Density:" + event.getDensity());

        if (fluidState.is(FluidTags.LAVA)){
            float density = player.hasEffect(MobEffects.BLINDNESS) ? event.getDensity() * 5.0F : event.getDensity();
            int enchLvl = EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.MAGMA_VISION.get(), player);
            if (enchLvl >= 1){
                event.setCanceled(true); // Level effects are: 0.66x density (I), 0.33x density (II), 0.18x density (III)
                event.setDensity(density / ((enchLvl * ((float)enchLvl / 2)) + 1));
                //CubicThings.LOGGER.info("After | Is event cancelled:" + event.isCanceled());
                //CubicThings.LOGGER.info("After | Fog Density:" + event.getDensity());
            }
        }
    }

    @SubscribeEvent
    public static void magmaVisionFogColorEvent(final EntityViewRenderEvent.FogColors event) {
        final var player = (LocalPlayer)event.getInfo().getEntity();
        final var fluidState = event.getInfo().getBlockAtCamera().getFluidState();

        if (fluidState.is(FluidTags.LAVA)) {
            int enchLvl = EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.MAGMA_VISION.get(), player);
            if (enchLvl >= 1){
                if (player.hasEffect(MobEffects.BLINDNESS)){
                    event.setRed(0.3F);
                    event.setGreen(0F);
                    event.setBlue(0.05F);
                }
            }
        }
    }

}
