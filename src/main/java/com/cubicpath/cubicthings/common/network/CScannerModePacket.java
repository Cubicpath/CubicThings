////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.network;

import com.cubicpath.cubicthings.common.item.ScannerItem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class CScannerModePacket extends ModPacket {
    private final ScannerItem.ScannerMode scannerMode;
    private final int slotIndex;

    public CScannerModePacket(FriendlyByteBuf buf) {
        super(buf);
        this.scannerMode = buf.readEnum(ScannerItem.ScannerMode.class);
        this.slotIndex = buf.readInt();
    }

    /** External packet creation.*/
    public CScannerModePacket(ScannerItem.ScannerMode scannerMode, int slotIndex) {
        this.scannerMode = scannerMode;
        this.slotIndex = slotIndex;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.scannerMode);
        buf.writeInt(this.slotIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            ItemStack stack = Objects.requireNonNull(player, "Sender cannot be null.").getInventory().getItem(this.slotIndex);
            if (stack.getItem() instanceof ScannerItem){
                ScannerItem.setScannerMode(stack, this.scannerMode);
            }
        });
        context.get().setPacketHandled(true);
    }

}