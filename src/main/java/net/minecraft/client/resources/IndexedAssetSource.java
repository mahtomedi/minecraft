package net.minecraft.client.resources;

import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class IndexedAssetSource {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Splitter PATH_SPLITTER = Splitter.on('/');

    public static Path createIndexFs(Path param0, String param1) {
        Path var0 = param0.resolve("objects");
        LinkFileSystem.Builder var1 = LinkFileSystem.builder();
        Path var2 = param0.resolve("indexes/" + param1 + ".json");

        try (BufferedReader var3 = Files.newBufferedReader(var2, StandardCharsets.UTF_8)) {
            JsonObject var4 = GsonHelper.parse(var3);
            JsonObject var5 = GsonHelper.getAsJsonObject(var4, "objects", null);
            if (var5 != null) {
                for(Entry<String, JsonElement> var6 : var5.entrySet()) {
                    JsonObject var7 = (JsonObject)var6.getValue();
                    String var8 = var6.getKey();
                    List<String> var9 = PATH_SPLITTER.splitToList(var8);
                    String var10 = GsonHelper.getAsString(var7, "hash");
                    Path var11 = var0.resolve(var10.substring(0, 2) + "/" + var10);
                    var1.put(var9, var11);
                }
            }
        } catch (JsonParseException var17) {
            LOGGER.error("Unable to parse resource index file: {}", var2);
        } catch (IOException var18) {
            LOGGER.error("Can't open the resource index file: {}", var2);
        }

        return var1.build("index-" + param1).getPath("/");
    }
}
