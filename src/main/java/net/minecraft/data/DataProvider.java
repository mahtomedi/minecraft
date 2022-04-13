package net.minecraft.data;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.file.Path;

public interface DataProvider {
    HashFunction SHA1 = Hashing.sha1();

    void run(CachedOutput var1) throws IOException;

    String getName();

    static void save(Gson param0, CachedOutput param1, JsonElement param2, Path param3) throws IOException {
        String var0 = param0.toJson(param2);
        param1.writeIfNeeded(param3, var0);
    }
}
