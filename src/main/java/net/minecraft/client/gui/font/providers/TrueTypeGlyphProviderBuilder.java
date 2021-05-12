package net.minecraft.client.gui.font.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class TrueTypeGlyphProviderBuilder implements GlyphProviderBuilder {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceLocation location;
    private final float size;
    private final float oversample;
    private final float shiftX;
    private final float shiftY;
    private final String skip;

    public TrueTypeGlyphProviderBuilder(ResourceLocation param0, float param1, float param2, float param3, float param4, String param5) {
        this.location = param0;
        this.size = param1;
        this.oversample = param2;
        this.shiftX = param3;
        this.shiftY = param4;
        this.skip = param5;
    }

    public static GlyphProviderBuilder fromJson(JsonObject param0) {
        float var0 = 0.0F;
        float var1 = 0.0F;
        if (param0.has("shift")) {
            JsonArray var2 = param0.getAsJsonArray("shift");
            if (var2.size() != 2) {
                throw new JsonParseException("Expected 2 elements in 'shift', found " + var2.size());
            }

            var0 = GsonHelper.convertToFloat(var2.get(0), "shift[0]");
            var1 = GsonHelper.convertToFloat(var2.get(1), "shift[1]");
        }

        StringBuilder var3 = new StringBuilder();
        if (param0.has("skip")) {
            JsonElement var4 = param0.get("skip");
            if (var4.isJsonArray()) {
                JsonArray var5 = GsonHelper.convertToJsonArray(var4, "skip");

                for(int var6 = 0; var6 < var5.size(); ++var6) {
                    var3.append(GsonHelper.convertToString(var5.get(var6), "skip[" + var6 + "]"));
                }
            } else {
                var3.append(GsonHelper.convertToString(var4, "skip"));
            }
        }

        return new TrueTypeGlyphProviderBuilder(
            new ResourceLocation(GsonHelper.getAsString(param0, "file")),
            GsonHelper.getAsFloat(param0, "size", 11.0F),
            GsonHelper.getAsFloat(param0, "oversample", 1.0F),
            var0,
            var1,
            var3.toString()
        );
    }

    @Nullable
    @Override
    public GlyphProvider create(ResourceManager param0) {
        STBTTFontinfo var0 = null;
        ByteBuffer var1 = null;

        try {
            TrueTypeGlyphProvider var5;
            try (Resource var2 = param0.getResource(new ResourceLocation(this.location.getNamespace(), "font/" + this.location.getPath()))) {
                LOGGER.debug("Loading font {}", this.location);
                var0 = STBTTFontinfo.malloc();
                var1 = TextureUtil.readResource(var2.getInputStream());
                var1.flip();
                LOGGER.debug("Reading font {}", this.location);
                if (!STBTruetype.stbtt_InitFont(var0, var1)) {
                    throw new IOException("Invalid ttf");
                }

                var5 = new TrueTypeGlyphProvider(var1, var0, this.size, this.oversample, this.shiftX, this.shiftY, this.skip);
            }

            return var5;
        } catch (Exception var9) {
            LOGGER.error("Couldn't load truetype font {}", this.location, var9);
            if (var0 != null) {
                var0.free();
            }

            MemoryUtil.memFree(var1);
            return null;
        }
    }
}
