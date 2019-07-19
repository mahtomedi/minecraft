package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.Cipher;

public class CipherEncoder extends MessageToByteEncoder<ByteBuf> {
    private final CipherBase cipher;

    public CipherEncoder(Cipher param0) {
        this.cipher = new CipherBase(param0);
    }

    protected void encode(ChannelHandlerContext param0, ByteBuf param1, ByteBuf param2) throws Exception {
        this.cipher.encipher(param1, param2);
    }
}
