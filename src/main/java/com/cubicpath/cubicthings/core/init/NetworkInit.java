////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.core.init;

import com.cubicpath.cubicthings.CubicThings;
import com.cubicpath.cubicthings.common.network.*;

@SuppressWarnings("unused")
public final class NetworkInit {
    private NetworkInit(){
        throw new IllegalStateException();
    }

    public static final ModPacketHandler PACKET_HANDLER = new ModPacketHandler(CubicThings.MODID, "main", "1");

    public static void registerPackets() {
        // Client Packets
        PACKET_HANDLER.registerCPacket(CScannerModePacket.class, CScannerModePacket::new);
        PACKET_HANDLER.registerCPacket(CScannerTargetPacket.class, CScannerTargetPacket::new);

        // Server Packets
        PACKET_HANDLER.registerSPacket(SStepHeightSyncPacket.class, SStepHeightSyncPacket::new);
    }

}
