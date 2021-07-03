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

import com.cubicpath.cubicthings.CubicThings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SStepHeightSyncPacket {
    private static final NetworkDirection DIRECTION = NetworkDirection.PLAY_TO_CLIENT;
    private final float data;

    private SStepHeightSyncPacket(PacketBuffer buf) {
        this.data = buf.readFloat();
    }

    /** External packet creation. Null sets data to default {@linkplain ClientPlayerEntity#stepHeight} (0.6F) .*/
    public SStepHeightSyncPacket(@Nullable Float data) {
        this.data = data != null ? data : 1.0F;
    }

    /** Called by a {@link net.minecraftforge.fml.network.simple.SimpleChannel SimpleChannel} to decode data on receiving side. Do not use outside of packet-registration. */
    public static SStepHeightSyncPacket decode(PacketBuffer buf){
        return new SStepHeightSyncPacket(buf);
    }

    /** Called by a {@link net.minecraftforge.fml.network.simple.SimpleChannel SimpleChannel} to encode data for sending. Do not use outside of packet-registration. */
    public void encode(PacketBuffer buf) {
        buf.writeFloat(this.data);
    }

    /** Called by a {@link net.minecraftforge.fml.network.simple.SimpleChannel SimpleChannel} to handle logic using data from {@linkplain #decode}. Do not use outside of packet-registration. */
    public void handle(Supplier<NetworkEvent.Context> context) {
        CubicThings.LOGGER.debug("Handling Step-Height Sync Packet");
        context.get().enqueueWork(() -> {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            assert player != null;
            player.stepHeight = this.data - 0.4f;
        });
        context.get().setPacketHandled(true);
    }

    public static NetworkDirection getDirection(){
        return DIRECTION;
    }
}
