package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.crypto.Cipher;

public class CipherDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final CipherBase cipher;

    public CipherDecoder(Cipher param0) {
        this.cipher = new CipherBase(param0);
    }

    protected void decode(ChannelHandlerContext param0, ByteBuf param1, List<Object> param2) throws Exception {
        param2.add(this.cipher.decipher(param0, param1));
    }
}
