package net.minecraft.server.rcon;

import java.nio.charset.StandardCharsets;

public class PktUtils {
    public static final char[] HEX_CHAR = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String stringFromByteArray(byte[] param0, int param1, int param2) {
        int var0 = param2 - 1;
        int var1 = param1 > var0 ? var0 : param1;

        while(0 != param0[var1] && var1 < var0) {
            ++var1;
        }

        return new String(param0, param1, var1 - param1, StandardCharsets.UTF_8);
    }

    public static int intFromByteArray(byte[] param0, int param1) {
        return intFromByteArray(param0, param1, param0.length);
    }

    public static int intFromByteArray(byte[] param0, int param1, int param2) {
        return 0 > param2 - param1 - 4
            ? 0
            : param0[param1 + 3] << 24 | (param0[param1 + 2] & 0xFF) << 16 | (param0[param1 + 1] & 0xFF) << 8 | param0[param1] & 0xFF;
    }

    public static int intFromNetworkByteArray(byte[] param0, int param1, int param2) {
        return 0 > param2 - param1 - 4
            ? 0
            : param0[param1] << 24 | (param0[param1 + 1] & 0xFF) << 16 | (param0[param1 + 2] & 0xFF) << 8 | param0[param1 + 3] & 0xFF;
    }

    public static String toHexString(byte param0) {
        return "" + HEX_CHAR[(param0 & 240) >>> 4] + HEX_CHAR[param0 & 15];
    }
}
