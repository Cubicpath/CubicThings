////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.*;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

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

    public <T extends ModPacket> void registerCPacket(Class<T> messageType, Function<FriendlyByteBuf, T> decoder){
        this.registerPacket(messageType, T::encode, decoder, T::handle, NetworkDirection.PLAY_TO_SERVER);
    }

    public <T extends ModPacket> void registerSPacket(Class<T> messageType, Function<FriendlyByteBuf, T> decoder){
        this.registerPacket(messageType, T::encode, decoder, T::handle, NetworkDirection.PLAY_TO_CLIENT);
    }

    public <T> void registerPacket(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder,  Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler, NetworkDirection networkDirection){
        this.channel.registerMessage(this.packetIndex++, messageType, encoder, decoder, handler , Optional.ofNullable(networkDirection));
    }

    public void sendToPlayer(ServerPlayer player, ModPacket sPacket){
        this.channel.send(PacketDistributor.PLAYER.with(() -> player), sPacket);
    }

    public void sendToPlayers(ServerPlayer[] players, ModPacket sPacket){
        for (ServerPlayer player : players) this.channel.send(PacketDistributor.PLAYER.with(() -> player), sPacket);
    }

    public void sendToAllPlayers(ModPacket sPacket){
        this.channel.send(PacketDistributor.ALL.noArg(), sPacket);
    }

    public void sendToServer(ModPacket cPacket){
        this.channel.sendToServer(cPacket);
    }

}
