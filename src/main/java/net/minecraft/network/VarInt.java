package net.minecraft.network;

import io.netty.buffer.ByteBuf;

public class VarInt {
    private static final int MAX_VARINT_SIZE = 5;
    private static final int DATA_BITS_MASK = 127;
    private static final int CONTINUATION_BIT_MASK = 128;
    private static final int DATA_BITS_PER_BYTE = 7;

    public static int getByteSize(int param0) {
        for(int var0 = 1; var0 < 5; ++var0) {
            if ((param0 & -1 << var0 * 7) == 0) {
                return var0;
            }
        }

        return 5;
    }

    public static boolean hasContinuationBit(byte param0) {
        return (param0 & 128) == 128;
    }

    public static int read(ByteBuf param0) {
        int var0 = 0;
        int var1 = 0;

        byte var2;
        do {
            var2 = param0.readByte();
            var0 |= (var2 & 127) << var1++ * 7;
            if (var1 > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while(hasContinuationBit(var2));

        return var0;
    }

    public static ByteBuf write(ByteBuf param0, int param1) {
        while((param1 & -128) != 0) {
            param0.writeByte(param1 & 127 | 128);
            param1 >>>= 7;
        }

        param0.writeByte(param1);
        return param0;
    }
}
