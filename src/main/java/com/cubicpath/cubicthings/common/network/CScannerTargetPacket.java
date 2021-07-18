////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.network;

import com.cubicpath.cubicthings.common.item.ScannerItem;

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
    private final byte flag;

    public CScannerTargetPacket(PacketBuffer buf) {
        super(buf);
        this.scanTarget = buf.readString(32767);
        this.slotIndex = buf.readInt();
        this.flag = buf.readByte();
    }

    /** External packet creation. */
    public CScannerTargetPacket(String scanTarget, int slotIndex, byte flag) {
        this.scanTarget = scanTarget;
        this.slotIndex = slotIndex;
        this.flag = flag;
    }

    public void encode(PacketBuffer buf) {
        buf.writeString(this.scanTarget);
        buf.writeInt(this.slotIndex);
        buf.writeByte(this.flag);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            ItemStack stack = Objects.requireNonNull(player, "Sender cannot be null.").inventory.getStackInSlot(this.slotIndex);
            ScannerItem.ScannerMode scannerMode = ScannerItem.getScannerMode(stack);
            if (stack.getItem() instanceof ScannerItem){
                switch(this.flag){
                    default: throw new IllegalArgumentException("Unhandled byte-value: " + this.flag);

                    case (byte) 1: {
                        ScannerItem.addTarget(stack, scannerMode, this.scanTarget);
                        break;
                    }
                    case (byte) 2: {
                        ScannerItem.removeTarget(stack, scannerMode, this.scanTarget);
                        break;
                    }
                    case (byte) 3: {
                        scannerMode.getTargetList(stack).clear();
                        break;
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static NetworkDirection getDirection(){
        return DIRECTION;
    }
}