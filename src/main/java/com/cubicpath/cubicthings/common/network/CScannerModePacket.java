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

public class CScannerModePacket extends ModPacket {
    private final ScannerItem.ScannerMode scannerMode;
    private final int slotIndex;

    public CScannerModePacket(PacketBuffer buf) {
        super(buf);
        this.scannerMode = buf.readEnumValue(ScannerItem.ScannerMode.class);
        this.slotIndex = buf.readInt();
    }

    /** External packet creation.*/
    public CScannerModePacket(ScannerItem.ScannerMode scannerMode, int slotIndex) {
        this.scannerMode = scannerMode;
        this.slotIndex = slotIndex;
    }

    public void encode(PacketBuffer buf) {
        buf.writeEnumValue(this.scannerMode);
        buf.writeInt(this.slotIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            ItemStack stack = Objects.requireNonNull(player, "Sender cannot be null.").inventory.getStackInSlot(this.slotIndex);
            if (stack.getItem() instanceof ScannerItem){
                ScannerItem.setScannerMode(stack, this.scannerMode);
            }
        });
        context.get().setPacketHandled(true);
    }

}