////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.network;

import com.cubicpath.cubicthings.common.item.ScannerItem;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class CScannerTargetPacket extends ModPacket {
    private static final NetworkDirection DIRECTION = NetworkDirection.PLAY_TO_SERVER;
    private final String scanTarget;
    private final int slotIndex;
    private final boolean remove;

    public CScannerTargetPacket(PacketBuffer buf) {
        super(buf);
        this.scanTarget = buf.readString();
        this.slotIndex = buf.readInt();
        this.remove = buf.readBoolean();
    }

    /** External packet creation. */
    public CScannerTargetPacket(String scanTarget, int slotIndex, boolean remove) {
        this.scanTarget = scanTarget;
        this.slotIndex = slotIndex;
        this.remove = remove;
    }

    public void encode(PacketBuffer buf) {
        buf.writeString(this.scanTarget);
        buf.writeInt(this.slotIndex);
        buf.writeBoolean(this.remove);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            ItemStack stack = Objects.requireNonNull(player, "Sender cannot be null.").inventory.getStackInSlot(this.slotIndex);
            if (stack.getItem() instanceof ScannerItem){
                if (this.remove) {
                    ScannerItem.removeTarget(stack, ScannerItem.getScannerMode(stack), this.scanTarget);
                } else {
                    ScannerItem.addTarget(stack, ScannerItem.getScannerMode(stack), this.scanTarget);
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static NetworkDirection getDirection(){
        return DIRECTION;
    }
}