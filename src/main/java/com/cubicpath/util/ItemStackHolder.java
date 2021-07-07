////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.util;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

/**
 * Holds an {@link ItemStack}. Primarily for writing to a {@link PacketBuffer}.
 *
 * @since 1.2
 * @author Cubicpath
 */
public class ItemStackHolder {
    public final ItemStack stack;

    public ItemStackHolder(ItemStack stack) {
        this.stack = stack;
    }

    public void writeToBuffer(PacketBuffer buf){
        buf.writeItemStack(this.stack);
    }
}
