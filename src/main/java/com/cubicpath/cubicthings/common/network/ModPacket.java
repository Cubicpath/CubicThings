////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class ModPacket {
    protected ModPacket(){
    }

    /** Called by a {@link net.minecraftforge.fml.network.simple.SimpleChannel SimpleChannel} to decode data on receiving side. Do not use outside of packet-registration. */
    public ModPacket(PacketBuffer buf) {
    }

    /** Called by a {@link net.minecraftforge.fml.network.simple.SimpleChannel SimpleChannel} to encode data for sending. Do not use outside of packet-registration. */
    public abstract void encode(PacketBuffer buf);

    /** Called by a {@link net.minecraftforge.fml.network.simple.SimpleChannel SimpleChannel} to handle logic using data from {@linkplain #ModPacket(PacketBuffer)}. Do not use outside of packet-registration. */
    public abstract void handle(Supplier<NetworkEvent.Context> context);

}
