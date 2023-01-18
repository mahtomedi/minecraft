package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public class PacketBundleUnpacker extends MessageToMessageEncoder<Packet<?>> {
    private final PacketFlow flow;

    public PacketBundleUnpacker(PacketFlow param0) {
        this.flow = param0;
    }

    protected void encode(ChannelHandlerContext param0, Packet<?> param1, List<Object> param2) throws Exception {
        BundlerInfo.Provider var0 = param0.channel().attr(BundlerInfo.BUNDLER_PROVIDER).get();
        if (var0 == null) {
            throw new EncoderException("Bundler not configured: " + param1);
        } else {
            var0.getBundlerInfo(this.flow).unbundlePacket(param1, param2::add);
        }
    }
}
