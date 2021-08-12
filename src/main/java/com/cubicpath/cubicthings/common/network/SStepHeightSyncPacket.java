////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class SStepHeightSyncPacket extends ModPacket {
    private final float data;

    public SStepHeightSyncPacket(FriendlyByteBuf buf) {
        super(buf);
        this.data = buf.readFloat();
    }

    public SStepHeightSyncPacket(float data) {
        this.data = data;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(this.data);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            Objects.requireNonNull(player, "Client player cannot be null.").maxUpStep = this.data - 0.4f;
        });
        context.get().setPacketHandled(true);
    }

}
