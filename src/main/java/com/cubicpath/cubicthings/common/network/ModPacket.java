////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class ModPacket {
    protected ModPacket(){
    }

    /** Called by a {@link net.minecraftforge.fmllegacy.network.simple.SimpleChannel SimpleChannel} to decode data on receiving side. Do not use outside packet-registration. */
    public ModPacket(FriendlyByteBuf buf) {
    }

    /** Called by a {@link net.minecraftforge.fmllegacy.network.simple.SimpleChannel SimpleChannel} to encode data for sending. Do not use outside packet-registration. */
    public abstract void encode(FriendlyByteBuf buf);

    /** Called by a {@link net.minecraftforge.fmllegacy.network.simple.SimpleChannel SimpleChannel} to handle logic using data from {@linkplain #ModPacket(FriendlyByteBuf buf)}. Do not use outside packet-registration. */
    public abstract void handle(Supplier<NetworkEvent.Context> context);

}
