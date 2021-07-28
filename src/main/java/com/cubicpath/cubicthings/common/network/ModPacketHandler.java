////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.*;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModPacketHandler {
    /** Increment by 1 for every new packet registered to this instance. */
    private int packetIndex = 0;
    public final String protocolVersion;
    public final SimpleChannel channel;

    public ModPacketHandler(String modid, String channelPath, String protocolVersion){
        this.protocolVersion = protocolVersion;
        this.channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(modid, channelPath),
                () -> this.protocolVersion, this.protocolVersion::equals, this.protocolVersion::equals);
    }

    public <T extends ModPacket> void registerCPacket(Class<T> messageType, Function<PacketBuffer, T> decoder){
        this.registerPacket(messageType, T::encode, decoder, T::handle, NetworkDirection.PLAY_TO_SERVER);
    }

    public <T extends ModPacket> void registerSPacket(Class<T> messageType, Function<PacketBuffer, T> decoder){
        this.registerPacket(messageType, T::encode, decoder, T::handle, NetworkDirection.PLAY_TO_CLIENT);
    }

    public <T extends ModPacket> void registerPacket(Class<T> messageType, BiConsumer<T, PacketBuffer> encoder,  Function<PacketBuffer, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler, NetworkDirection networkDirection){
        this.channel.registerMessage(this.packetIndex++, messageType, encoder, decoder, handler , Optional.ofNullable(networkDirection));
    }

    public void sendToPlayer(ServerPlayerEntity player, ModPacket sPacket){
        this.channel.send(PacketDistributor.PLAYER.with(() -> player), sPacket);
    }

    public void sendToPlayers(ServerPlayerEntity[] players, ModPacket sPacket){
        for (ServerPlayerEntity player : players) this.channel.send(PacketDistributor.PLAYER.with(() -> player), sPacket);
    }

    public void sendToAllPlayers(ModPacket sPacket){
        this.channel.send(PacketDistributor.ALL.noArg(), sPacket);
    }

    public void sendToServer(ModPacket cPacket){
        this.channel.sendToServer(cPacket);
    }

}
