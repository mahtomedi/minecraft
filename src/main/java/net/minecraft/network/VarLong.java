package net.minecraft.network;

import io.netty.buffer.ByteBuf;

public class VarLong {
    private static final int MAX_VARLONG_SIZE = 10;
    private static final int DATA_BITS_MASK = 127;
    private static final int CONTINUATION_BIT_MASK = 128;
    private static final int DATA_BITS_PER_BYTE = 7;

    public static int getByteSize(long param0) {
        for(int var0 = 1; var0 < 10; ++var0) {
            if ((param0 & -1L << var0 * 7) == 0L) {
                return var0;
            }
        }

        return 10;
    }

    public static boolean hasContinuationBit(byte param0) {
        return (param0 & 128) == 128;
    }

    public static long read(ByteBuf param0) {
        long var0 = 0L;
        int var1 = 0;

        byte var2;
        do {
            var2 = param0.readByte();
            var0 |= (long)(var2 & 127) << var1++ * 7;
            if (var1 > 10) {
                throw new RuntimeException("VarLong too big");
            }
        } while(hasContinuationBit(var2));

        return var0;
    }

    public static ByteBuf write(ByteBuf param0, long param1) {
        while((param1 & -128L) != 0L) {
            param0.writeByte((int)(param1 & 127L) | 128);
            param1 >>>= 7;
        }

        param0.writeByte((int)param1);
        return param0;
    }
}
