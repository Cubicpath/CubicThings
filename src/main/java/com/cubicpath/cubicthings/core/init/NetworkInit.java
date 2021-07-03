////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.init;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.network.*;

import java.util.Optional;

@SuppressWarnings("unused")
public final class NetworkInit {
    private NetworkInit(){
        throw new IllegalStateException();
    }

    public static final ModPacketHandler PACKET_HANDLER = new ModPacketHandler(CubicThings.MODID, "main", "1");

    public static void registerPackets() {
        PACKET_HANDLER.channel.registerMessage(PACKET_HANDLER.packetIndex++,
                SStepHeightSyncPacket.class, SStepHeightSyncPacket::encode, SStepHeightSyncPacket::decode, SStepHeightSyncPacket::handle,
                Optional.of(SStepHeightSyncPacket.getDirection()));

    }

}
