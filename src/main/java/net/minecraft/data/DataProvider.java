package net.minecraft.data;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public interface DataProvider {
    HashFunction SHA1 = Hashing.sha1();

    void run(HashCache var1) throws IOException;

    String getName();

    static void save(Gson param0, HashCache param1, JsonElement param2, Path param3) throws IOException {
        String var0 = param0.toJson(param2);
        String var1 = SHA1.hashUnencodedChars(var0).toString();
        if (!Objects.equals(param1.getHash(param3), var1) || !Files.exists(param3)) {
            Files.createDirectories(param3.getParent());

            try (BufferedWriter var2 = Files.newBufferedWriter(param3)) {
                var2.write(var0);
            }
        }

        param1.putNew(param3, var1);
    }
}
