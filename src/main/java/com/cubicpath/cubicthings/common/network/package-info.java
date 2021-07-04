////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

/**
 * <p>Packet handlers hold {@link net.minecraftforge.fml.network.simple.SimpleChannel SimpleChannel}s that aid in the registering and sending of packets.</p>
 * <p>Packets can either be client-side or server-side (by origin). Client-side packets will be handled by the server, and vice versa.</p>
 * <p>Server packets will be denoted by an S, while client packets are denoted by a C. Ex: {@link com.cubicpath.cubicthings.common.network.SStepHeightSyncPacket}</p>
 *
 * @since 0.1.0
 * @author Cubicpath
 */
package com.cubicpath.cubicthings.common.network;