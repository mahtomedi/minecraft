package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.client.renderer.texture.atlas.sources.SourceFilter;
import net.minecraft.client.renderer.texture.atlas.sources.Unstitcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpriteSources {
    private static final BiMap<ResourceLocation, SpriteSourceType> TYPES = HashBiMap.create();
    public static final SpriteSourceType SINGLE_FILE = register("single", SingleFile.CODEC);
    public static final SpriteSourceType DIRECTORY = register("directory", DirectoryLister.CODEC);
    public static final SpriteSourceType FILTER = register("filter", SourceFilter.CODEC);
    public static final SpriteSourceType UNSTITCHER = register("unstitch", Unstitcher.CODEC);
    public static Codec<SpriteSourceType> TYPE_CODEC = ResourceLocation.CODEC.flatXmap(param0 -> {
        SpriteSourceType var0 = (SpriteSourceType)TYPES.get(param0);
        return var0 != null ? DataResult.success(var0) : DataResult.error("Unknown type " + param0);
    }, param0 -> {
        ResourceLocation var0 = (ResourceLocation)TYPES.inverse().get(param0);
        return param0 != null ? DataResult.success(var0) : DataResult.error("Unknown type " + var0);
    });
    public static Codec<SpriteSource> CODEC = TYPE_CODEC.dispatch(SpriteSource::type, SpriteSourceType::codec);
    public static Codec<List<SpriteSource>> FILE_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(CODEC.listOf().fieldOf("sources").forGetter(param0x -> param0x)).apply(param0, param0x -> param0x)
    );

    private static SpriteSourceType register(String param0, Codec<? extends SpriteSource> param1) {
        SpriteSourceType var0 = new SpriteSourceType(param1);
        ResourceLocation var1 = new ResourceLocation(param0);
        SpriteSourceType var2 = (SpriteSourceType)TYPES.putIfAbsent(var1, var0);
        if (var2 != null) {
            throw new IllegalStateException("Duplicate registration " + var1);
        } else {
            return var0;
        }
    }
}
