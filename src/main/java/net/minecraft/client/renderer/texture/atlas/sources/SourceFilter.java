package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ResourceLocationPattern;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SourceFilter implements SpriteSource {
    public static final Codec<SourceFilter> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(ResourceLocationPattern.CODEC.fieldOf("pattern").forGetter(param0x -> param0x.filter)).apply(param0, SourceFilter::new)
    );
    private final ResourceLocationPattern filter;

    public SourceFilter(ResourceLocationPattern param0) {
        this.filter = param0;
    }

    @Override
    public void run(ResourceManager param0, SpriteSource.Output param1) {
        param1.removeAll(this.filter.locationPredicate());
    }

    @Override
    public SpriteSourceType type() {
        return SpriteSources.FILTER;
    }
}
