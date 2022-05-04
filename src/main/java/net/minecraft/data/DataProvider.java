package net.minecraft.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;

public interface DataProvider {
    ToIntFunction<String> FIXED_ORDER_FIELDS = Util.make(new Object2IntOpenHashMap<>(), param0 -> {
        param0.put("type", 0);
        param0.put("parent", 1);
        param0.defaultReturnValue(2);
    });
    Comparator<String> KEY_COMPARATOR = Comparator.comparingInt(FIXED_ORDER_FIELDS).thenComparing(param0 -> param0);

    void run(CachedOutput var1) throws IOException;

    String getName();

    static void saveStable(CachedOutput param0, JsonElement param1, Path param2) throws IOException {
        ByteArrayOutputStream var0 = new ByteArrayOutputStream();
        HashingOutputStream var1 = new HashingOutputStream(Hashing.sha1(), var0);
        Writer var2 = new OutputStreamWriter(var1, StandardCharsets.UTF_8);
        JsonWriter var3 = new JsonWriter(var2);
        var3.setSerializeNulls(false);
        var3.setIndent("  ");
        GsonHelper.writeValue(var3, param1, KEY_COMPARATOR);
        var3.close();
        param0.writeIfNeeded(param2, var0.toByteArray(), var1.hash());
    }
}
