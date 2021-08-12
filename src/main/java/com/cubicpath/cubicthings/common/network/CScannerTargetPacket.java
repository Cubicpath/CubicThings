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

public class CScannerTargetPacket extends ModPacket {
    private final String scanTarget;
    private final int slotIndex;
    private final byte opByte;

    public CScannerTargetPacket(FriendlyByteBuf buf) {
        super(buf);
        this.scanTarget = buf.readUtf(32767);
        this.slotIndex = buf.readInt();
        this.opByte = buf.readByte();
    }

    /** External packet creation. */
    public CScannerTargetPacket(String scanTarget, int slotIndex, byte opByte) {
        this.scanTarget = scanTarget;
        this.slotIndex = slotIndex;
        this.opByte = opByte;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.scanTarget);
        buf.writeInt(this.slotIndex);
        buf.writeByte(this.opByte);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            ItemStack stack = Objects.requireNonNull(player, "Sender cannot be null.").getInventory().getItem(this.slotIndex);
            ScannerItem.ScannerMode scannerMode = ScannerItem.getScannerMode(stack);
            if (stack.getItem() instanceof ScannerItem){
                switch (this.opByte) {
                    default -> throw new IllegalArgumentException("Unhandled byte-operator: " + this.opByte);
                    case (byte) 1 -> ScannerItem.addTarget(stack, scannerMode, this.scanTarget);
                    case (byte) 2 -> ScannerItem.removeTarget(stack, scannerMode, this.scanTarget);
                    case (byte) 3 -> scannerMode.getTargetList(stack).clear();
                }
            }
        });
        context.get().setPacketHandled(true);
    }

}