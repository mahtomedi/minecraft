package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class CipherBase {
    private final Cipher cipher;
    private byte[] heapIn = new byte[0];
    private byte[] heapOut = new byte[0];

    protected CipherBase(Cipher param0) {
        this.cipher = param0;
    }

    private byte[] bufToByte(ByteBuf param0) {
        int var0 = param0.readableBytes();
        if (this.heapIn.length < var0) {
            this.heapIn = new byte[var0];
        }

        param0.readBytes(this.heapIn, 0, var0);
        return this.heapIn;
    }

    protected ByteBuf decipher(ChannelHandlerContext param0, ByteBuf param1) throws ShortBufferException {
        int var0 = param1.readableBytes();
        byte[] var1 = this.bufToByte(param1);
        ByteBuf var2 = param0.alloc().heapBuffer(this.cipher.getOutputSize(var0));
        var2.writerIndex(this.cipher.update(var1, 0, var0, var2.array(), var2.arrayOffset()));
        return var2;
    }

    protected void encipher(ByteBuf param0, ByteBuf param1) throws ShortBufferException {
        int var0 = param0.readableBytes();
        byte[] var1 = this.bufToByte(param0);
        int var2 = this.cipher.getOutputSize(var0);
        if (this.heapOut.length < var2) {
            this.heapOut = new byte[var2];
        }

        param1.writeBytes(this.heapOut, 0, this.cipher.update(var1, 0, var0, this.heapOut));
    }
}
