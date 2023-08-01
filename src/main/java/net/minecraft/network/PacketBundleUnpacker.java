package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.AttributeKey;
import java.util.List;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;

public class PacketBundleUnpacker extends MessageToMessageEncoder<Packet<?>> {
    private final AttributeKey<? extends BundlerInfo.Provider> bundlerAttributeKey;

    public PacketBundleUnpacker(AttributeKey<? extends BundlerInfo.Provider> param0) {
        this.bundlerAttributeKey = param0;
    }

    protected void encode(ChannelHandlerContext param0, Packet<?> param1, List<Object> param2) throws Exception {
        BundlerInfo.Provider var0 = param0.channel().attr(this.bundlerAttributeKey).get();
        if (var0 == null) {
            throw new EncoderException("Bundler not configured: " + param1);
        } else {
            var0.bundlerInfo().unbundlePacket(param1, param2::add);
        }
    }
}
