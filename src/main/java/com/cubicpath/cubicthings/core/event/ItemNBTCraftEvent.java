////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.event;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.item.IDefaultNBTHolder;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = CubicThings.MODID)
public class ItemNBTCraftEvent {

    @SubscribeEvent
    public static void itemNBTCraftEvent(PlayerEvent.ItemCraftedEvent event) {
        ItemStack stack = event.getCrafting();

        if (stack.getItem() instanceof IDefaultNBTHolder) {
            ((IDefaultNBTHolder) stack.getItem()).setupNBT(stack);
        }
    }
}
