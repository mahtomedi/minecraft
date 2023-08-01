package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import org.slf4j.Logger;

public class PacketFlowValidator extends MessageToMessageCodec<Packet<?>, Packet<?>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final AttributeKey<ConnectionProtocol.CodecData<?>> decoderKey;
    private final AttributeKey<ConnectionProtocol.CodecData<?>> encoderKey;

    public PacketFlowValidator(AttributeKey<ConnectionProtocol.CodecData<?>> param0, AttributeKey<ConnectionProtocol.CodecData<?>> param1) {
        this.decoderKey = param0;
        this.encoderKey = param1;
    }

    private static void validatePacket(
        ChannelHandlerContext param0, Packet<?> param1, List<Object> param2, AttributeKey<ConnectionProtocol.CodecData<?>> param3
    ) {
        Attribute<ConnectionProtocol.CodecData<?>> var0 = param0.channel().attr(param3);
        ConnectionProtocol.CodecData<?> var1 = var0.get();
        if (!var1.isValidPacketType(param1)) {
            LOGGER.error("Unrecognized packet in pipeline {}:{} - {}", var1.protocol().id(), var1.flow(), param1);
        }

        ReferenceCountUtil.retain(param1);
        param2.add(param1);
        ProtocolSwapHandler.swapProtocolIfNeeded(var0, param1);
    }

    protected void decode(ChannelHandlerContext param0, Packet<?> param1, List<Object> param2) throws Exception {
        validatePacket(param0, param1, param2, this.decoderKey);
    }

    protected void encode(ChannelHandlerContext param0, Packet<?> param1, List<Object> param2) throws Exception {
        validatePacket(param0, param1, param2, this.encoderKey);
    }
}
