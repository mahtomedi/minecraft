package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum GlyphProviderBuilderType {
    BITMAP("bitmap", BitmapProvider.Builder::fromJson),
    TTF("ttf", TrueTypeGlyphProviderBuilder::fromJson),
    LEGACY_UNICODE("legacy_unicode", LegacyUnicodeBitmapsProvider.Builder::fromJson);

    private static final Map<String, GlyphProviderBuilderType> BY_NAME = Util.make(Maps.newHashMap(), param0 -> {
        for(GlyphProviderBuilderType var0 : values()) {
            param0.put(var0.name, var0);
        }

    });
    private final String name;
    private final Function<JsonObject, GlyphProviderBuilder> factory;

    private GlyphProviderBuilderType(String param0, Function<JsonObject, GlyphProviderBuilder> param1) {
        this.name = param0;
        this.factory = param1;
    }

    public static GlyphProviderBuilderType byName(String param0) {
        GlyphProviderBuilderType var0 = BY_NAME.get(param0);
        if (var0 == null) {
            throw new IllegalArgumentException("Invalid type: " + param0);
        } else {
            return var0;
        }
    }

    public GlyphProviderBuilder create(JsonObject param0) {
        return this.factory.apply(param0);
    }
}
