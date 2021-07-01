////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.network;

import com.cubicpath.cubicthings.CubicThings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class StepHeightSyncPacket {
    private static final NetworkDirection DIRECTION = NetworkDirection.PLAY_TO_CLIENT;
    private final float data;

    /** Internal packet creation. */
    private StepHeightSyncPacket(PacketBuffer buf) {
        this.data = buf.readFloat();
    }

    /** External packet creation. Null sets data to default {@linkplain ClientPlayerEntity#stepHeight} (0.6F) .*/
    public StepHeightSyncPacket(@Nullable Float data) {
        this.data = data != null ? data : 1.0F;
    }

    /** Called by {@link ModPacketHandler} to decode data on receiving side. */
    public static StepHeightSyncPacket decode(PacketBuffer buf){
        return new StepHeightSyncPacket(buf);
    }

    /** Called by {@link ModPacketHandler} to encode data for sending. */
    public void encode(PacketBuffer buf) {
        buf.writeFloat(this.data);
    }

    /** Handle logic using data from {@link #decode(PacketBuffer)}. */
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
