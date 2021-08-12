////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.container;

import com.cubicpath.cubicthings.core.init.ContainerInit;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScannerContainer extends AbstractContainerMenu {
    public Inventory inventory;
    public ItemStack sourceItemStack;

    public ScannerContainer(@Nullable MenuType<?> type, int id) {
        super(type, id);
    }

    /**
     * Client-Side creation from {@link net.minecraftforge.fmllegacy.network.NetworkHooks#openGui NetworkHooks#openGui}
     *
     * @param windowId id of the window
     * @param inventory Player's inventory at time of creation.
     * @param data Extra data passed along by the server.
     */
    public ScannerContainer(int windowId, Inventory inventory, @Nullable FriendlyByteBuf data){
        this(ContainerInit.SCANNER.get(), windowId);
        this.inventory = inventory;
        if (data != null) sourceItemStack = data.readItem();
    }

    @Override
    public boolean stillValid(@Nonnull Player playerIn) {
        return true;
    }
}
