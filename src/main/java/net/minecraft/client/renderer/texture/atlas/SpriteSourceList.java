package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SpriteSourceList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter ATLAS_INFO_CONVERTER = new FileToIdConverter("atlases", ".json");
    private final List<SpriteSource> sources;

    private SpriteSourceList(List<SpriteSource> param0) {
        this.sources = param0;
    }

    public List<Function<SpriteResourceLoader, SpriteContents>> list(ResourceManager param0) {
        final Map<ResourceLocation, SpriteSource.SpriteSupplier> var0 = new HashMap<>();
        SpriteSource.Output var1 = new SpriteSource.Output() {
            @Override
            public void add(ResourceLocation param0, SpriteSource.SpriteSupplier param1) {
                SpriteSource.SpriteSupplier var0 = var0.put(param0, param1);
                if (var0 != null) {
                    var0.discard();
                }

            }

            @Override
            public void removeAll(Predicate<ResourceLocation> param0) {
                Iterator<Entry<ResourceLocation, SpriteSource.SpriteSupplier>> var0 = var0.entrySet().iterator();

                while(var0.hasNext()) {
                    Entry<ResourceLocation, SpriteSource.SpriteSupplier> var1 = var0.next();
                    if (param0.test(var1.getKey())) {
                        var1.getValue().discard();
                        var0.remove();
                    }
                }

            }
        };
        this.sources.forEach(param2 -> param2.run(param0, var1));
        Builder<Function<SpriteResourceLoader, SpriteContents>> var2 = ImmutableList.builder();
        var2.add(param0x -> MissingTextureAtlasSprite.create());
        var2.addAll(var0.values());
        return var2.build();
    }

    public static SpriteSourceList load(ResourceManager param0, ResourceLocation param1) {
        ResourceLocation var0 = ATLAS_INFO_CONVERTER.idToFile(param1);
        List<SpriteSource> var1 = new ArrayList<>();

        for(Resource var2 : param0.getResourceStack(var0)) {
            try (BufferedReader var3 = var2.openAsReader()) {
                Dynamic<JsonElement> var4 = new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseReader(var3));
                var1.addAll(SpriteSources.FILE_CODEC.parse(var4).getOrThrow(false, LOGGER::error));
            } catch (Exception var11) {
                LOGGER.warn("Failed to parse atlas definition {} in pack {}", var0, var2.sourcePackId(), var11);
            }
        }

        return new SpriteSourceList(var1);
    }
}
