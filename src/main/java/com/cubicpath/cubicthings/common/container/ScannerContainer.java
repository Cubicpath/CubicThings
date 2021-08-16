////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.container;

import com.cubicpath.cubicthings.core.init.ContainerInit;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScannerContainer extends Container {
    public PlayerInventory inventory;
    public ItemStack sourceItemStack;

    public ScannerContainer(@Nullable ContainerType<?> type, int id) {
        super(type, id);
    }

    /**
     * Client-Side creation from {@link net.minecraftforge.fml.network.NetworkHooks#openGui NetworkHooks#openGui}
     *
     * @param windowId id of the window
     * @param inventory Player's inventory at time of creation.
     * @param data Extra data passed along by the server.
     */
    public ScannerContainer(int windowId, PlayerInventory inventory, @Nullable PacketBuffer data){
        this(ContainerInit.SCANNER.get(), windowId);
        this.inventory = inventory;
        if (data != null) sourceItemStack = data.readItem();
    }

    @Override
    public boolean stillValid(@Nonnull  PlayerEntity playerIn) {
        return true;
    }
}
