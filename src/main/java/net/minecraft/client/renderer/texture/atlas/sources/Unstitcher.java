package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Unstitcher implements SpriteSource {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<Unstitcher> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ResourceLocation.CODEC.fieldOf("resource").forGetter(param0x -> param0x.resource),
                    ExtraCodecs.nonEmptyList(Unstitcher.Region.CODEC.listOf()).fieldOf("regions").forGetter(param0x -> param0x.regions),
                    Codec.DOUBLE.optionalFieldOf("divisor_x", Double.valueOf(1.0)).forGetter(param0x -> param0x.xDivisor),
                    Codec.DOUBLE.optionalFieldOf("divisor_y", Double.valueOf(1.0)).forGetter(param0x -> param0x.yDivisor)
                )
                .apply(param0, Unstitcher::new)
    );
    private final ResourceLocation resource;
    private final List<Unstitcher.Region> regions;
    private final double xDivisor;
    private final double yDivisor;

    public Unstitcher(ResourceLocation param0, List<Unstitcher.Region> param1, double param2, double param3) {
        this.resource = param0;
        this.regions = param1;
        this.xDivisor = param2;
        this.yDivisor = param3;
    }

    @Override
    public void run(ResourceManager param0, SpriteSource.Output param1) {
        ResourceLocation var0 = TEXTURE_ID_CONVERTER.idToFile(this.resource);
        Optional<Resource> var1 = param0.getResource(var0);
        if (var1.isPresent()) {
            LazyLoadedImage var2 = new LazyLoadedImage(var0, var1.get(), this.regions.size());

            for(Unstitcher.Region var3 : this.regions) {
                param1.add(var3.sprite, new Unstitcher.RegionInstance(var2, var3, this.xDivisor, this.yDivisor));
            }
        } else {
            LOGGER.warn("Missing sprite: {}", var0);
        }

    }

    @Override
    public SpriteSourceType type() {
        return SpriteSources.UNSTITCHER;
    }

    @OnlyIn(Dist.CLIENT)
    static record Region(ResourceLocation sprite, double x, double y, double width, double height) {
        public static final Codec<Unstitcher.Region> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ResourceLocation.CODEC.fieldOf("sprite").forGetter(Unstitcher.Region::sprite),
                        Codec.DOUBLE.fieldOf("x").forGetter(Unstitcher.Region::x),
                        Codec.DOUBLE.fieldOf("y").forGetter(Unstitcher.Region::y),
                        Codec.DOUBLE.fieldOf("width").forGetter(Unstitcher.Region::width),
                        Codec.DOUBLE.fieldOf("height").forGetter(Unstitcher.Region::height)
                    )
                    .apply(param0, Unstitcher.Region::new)
        );
    }

    @OnlyIn(Dist.CLIENT)
    static class RegionInstance implements SpriteSource.SpriteSupplier {
        private final LazyLoadedImage image;
        private final Unstitcher.Region region;
        private final double xDivisor;
        private final double yDivisor;

        RegionInstance(LazyLoadedImage param0, Unstitcher.Region param1, double param2, double param3) {
            this.image = param0;
            this.region = param1;
            this.xDivisor = param2;
            this.yDivisor = param3;
        }

        public SpriteContents get() {
            try {
                NativeImage var0 = this.image.get();
                double var1 = (double)var0.getWidth() / this.xDivisor;
                double var2 = (double)var0.getHeight() / this.yDivisor;
                int var3 = Mth.floor(this.region.x * var1);
                int var4 = Mth.floor(this.region.y * var2);
                int var5 = Mth.floor(this.region.width * var1);
                int var6 = Mth.floor(this.region.height * var2);
                NativeImage var7 = new NativeImage(NativeImage.Format.RGBA, var5, var6, false);
                var0.copyRect(var7, var3, var4, 0, 0, var5, var6, false, false);
                return new SpriteContents(this.region.sprite, new FrameSize(var5, var6), var7, AnimationMetadataSection.EMPTY);
            } catch (Exception var15) {
                Unstitcher.LOGGER.error("Failed to unstitch region {}", this.region.sprite, var15);
            } finally {
                this.image.release();
            }

            return MissingTextureAtlasSprite.create();
        }

        @Override
        public void discard() {
            this.image.release();
        }
    }
}
