////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ModPacketHandler {
    /** Increment by 1 for every new packet registered to this instance. */
    public int packetIndex = 0;
    public final String protocolVersion;
    public final SimpleChannel channel;

    public ModPacketHandler(String modid, String channelPath, String protocolVersion){
        this.protocolVersion = protocolVersion;
        this.channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(modid, channelPath),
                () -> this.protocolVersion, this.protocolVersion::equals, this.protocolVersion::equals);
    }

    public void sendToPlayer(ServerPlayerEntity player, ModPacket packet){
        this.channel.send(PacketDistributor.PLAYER.with(()->player), packet);
    }

    public void sendToServer(ModPacket packet){
        this.channel.sendToServer(packet);
    }

}
