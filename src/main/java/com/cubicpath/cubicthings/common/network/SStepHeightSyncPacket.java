////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class SStepHeightSyncPacket extends ModPacket {
    private static final NetworkDirection DIRECTION = NetworkDirection.PLAY_TO_CLIENT;
    private final float data;

    public SStepHeightSyncPacket(PacketBuffer buf) {
        super(buf);
        this.data = buf.readFloat();
    }

    /** External packet creation. Null sets data to default {@linkplain ClientPlayerEntity#stepHeight} (0.6F) .*/
    public SStepHeightSyncPacket(@Nullable Float data) {
        this.data = data != null ? data : 1.0F;
    }

    public void encode(PacketBuffer buf) {
        buf.writeFloat(this.data);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            Objects.requireNonNull(player, "Client player cannot be null.").stepHeight = this.data - 0.4f;
        });
        context.get().setPacketHandled(true);
    }

    public static NetworkDirection getDirection(){
        return DIRECTION;
    }
}
