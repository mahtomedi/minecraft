package net.minecraft.world.level.dimension;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;

public class DimHash {
    private static String lastPasshphrase = "";

    public static int getHash(String param0) {
        lastPasshphrase = param0;
        return Hashing.sha256().hashString(param0 + ":why_so_salty#LazyCrypto", StandardCharsets.UTF_8).asInt() & 2147483647;
    }

    public static String getLastPasshphrase() {
        return lastPasshphrase;
    }
}
