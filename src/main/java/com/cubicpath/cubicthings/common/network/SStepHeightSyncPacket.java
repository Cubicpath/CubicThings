////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class SStepHeightSyncPacket extends ModPacket {
    private final float data;

    public SStepHeightSyncPacket(PacketBuffer buf) {
        super(buf);
        this.data = buf.readFloat();
    }

    public SStepHeightSyncPacket(float data) {
        this.data = data;
    }

    public void encode(PacketBuffer buf) {
        buf.writeFloat(this.data);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            PlayerEntity player = Minecraft.getInstance().player;
            Objects.requireNonNull(player, "Client player cannot be null.").maxUpStep = this.data - 0.4f;
        });
        context.get().setPacketHandled(true);
    }

}
