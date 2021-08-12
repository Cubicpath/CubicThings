////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.event;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.item.IDefaultNBTHolder;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = CubicThings.MODID)
public final class ItemNBTCraftEvent {
    private ItemNBTCraftEvent() {
        throw new IllegalStateException();
    }

    @SubscribeEvent
    public static void itemNBTCraftEvent(final PlayerEvent.ItemCraftedEvent event) {
        final var stack = event.getCrafting();

        if (stack.getItem() instanceof IDefaultNBTHolder item) {
            item.setupNBT(stack);
        }
    }
}
