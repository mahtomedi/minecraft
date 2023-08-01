package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.nio.charset.StandardCharsets;

public class Utf8String {
    public static String read(ByteBuf param0, int param1) {
        int var0 = ByteBufUtil.utf8MaxBytes(param1);
        int var1 = VarInt.read(param0);
        if (var1 > var0) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + var1 + " > " + var0 + ")");
        } else if (var1 < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            int var2 = param0.readableBytes();
            if (var1 > var2) {
                throw new DecoderException("Not enough bytes in buffer, expected " + var1 + ", but got " + var2);
            } else {
                String var3 = param0.toString(param0.readerIndex(), var1, StandardCharsets.UTF_8);
                param0.readerIndex(param0.readerIndex() + var1);
                if (var3.length() > param1) {
                    throw new DecoderException("The received string length is longer than maximum allowed (" + var3.length() + " > " + param1 + ")");
                } else {
                    return var3;
                }
            }
        }
    }

    public static void write(ByteBuf param0, CharSequence param1, int param2) {
        if (param1.length() > param2) {
            throw new EncoderException("String too big (was " + param1.length() + " characters, max " + param2 + ")");
        } else {
            int var0 = ByteBufUtil.utf8MaxBytes(param1);
            ByteBuf var1 = param0.alloc().buffer(var0);

            try {
                int var2 = ByteBufUtil.writeUtf8(var1, param1);
                int var3 = ByteBufUtil.utf8MaxBytes(param2);
                if (var2 > var3) {
                    throw new EncoderException("String too big (was " + var2 + " bytes encoded, max " + var3 + ")");
                }

                VarInt.write(param0, var2);
                param0.writeBytes(var1);
            } finally {
                var1.release();
            }

        }
    }
}
