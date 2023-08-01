package net.minecraft.client.renderer.texture.atlas.sources;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PalettedPermutations implements SpriteSource {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<PalettedPermutations> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.list(ResourceLocation.CODEC).fieldOf("textures").forGetter(param0x -> param0x.textures),
                    ResourceLocation.CODEC.fieldOf("palette_key").forGetter(param0x -> param0x.paletteKey),
                    Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).fieldOf("permutations").forGetter(param0x -> param0x.permutations)
                )
                .apply(param0, PalettedPermutations::new)
    );
    private final List<ResourceLocation> textures;
    private final Map<String, ResourceLocation> permutations;
    private final ResourceLocation paletteKey;

    private PalettedPermutations(List<ResourceLocation> param0, ResourceLocation param1, Map<String, ResourceLocation> param2) {
        this.textures = param0;
        this.permutations = param2;
        this.paletteKey = param1;
    }

    @Override
    public void run(ResourceManager param0, SpriteSource.Output param1) {
        Supplier<int[]> var0 = Suppliers.memoize(() -> loadPaletteEntryFromImage(param0, this.paletteKey));
        Map<String, Supplier<IntUnaryOperator>> var1 = new HashMap<>();
        this.permutations
            .forEach((param3, param4) -> var1.put(param3, Suppliers.memoize(() -> createPaletteMapping(var0.get(), loadPaletteEntryFromImage(param0, param4)))));

        for(ResourceLocation var2 : this.textures) {
            ResourceLocation var3 = TEXTURE_ID_CONVERTER.idToFile(var2);
            Optional<Resource> var4 = param0.getResource(var3);
            if (var4.isEmpty()) {
                LOGGER.warn("Unable to find texture {}", var3);
            } else {
                LazyLoadedImage var5 = new LazyLoadedImage(var3, var4.get(), var1.size());

                for(Entry<String, Supplier<IntUnaryOperator>> var6 : var1.entrySet()) {
                    ResourceLocation var7 = var2.withSuffix("_" + (String)var6.getKey());
                    param1.add(var7, new PalettedPermutations.PalettedSpriteSupplier(var5, var6.getValue(), var7));
                }
            }
        }

    }

    private static IntUnaryOperator createPaletteMapping(int[] param0, int[] param1) {
        if (param1.length != param0.length) {
            LOGGER.warn("Palette mapping has different sizes: {} and {}", param0.length, param1.length);
            throw new IllegalArgumentException();
        } else {
            Int2IntMap var0 = new Int2IntOpenHashMap(param1.length);

            for(int var1 = 0; var1 < param0.length; ++var1) {
                int var2 = param0[var1];
                if (FastColor.ABGR32.alpha(var2) != 0) {
                    var0.put(FastColor.ABGR32.transparent(var2), param1[var1]);
                }
            }

            return param1x -> {
                int var0x = FastColor.ABGR32.alpha(param1x);
                if (var0x == 0) {
                    return param1x;
                } else {
                    int var1x = FastColor.ABGR32.transparent(param1x);
                    int var2x = var0.getOrDefault(var1x, FastColor.ABGR32.opaque(var1x));
                    int var3x = FastColor.ABGR32.alpha(var2x);
                    return FastColor.ABGR32.color(var0x * var3x / 255, var2x);
                }
            };
        }
    }

    public static int[] loadPaletteEntryFromImage(ResourceManager param0, ResourceLocation param1) {
        Optional<Resource> var0 = param0.getResource(TEXTURE_ID_CONVERTER.idToFile(param1));
        if (var0.isEmpty()) {
            LOGGER.error("Failed to load palette image {}", param1);
            throw new IllegalArgumentException();
        } else {
            try {
                int[] var5;
                try (
                    InputStream var1 = var0.get().open();
                    NativeImage var2 = NativeImage.read(var1);
                ) {
                    var5 = var2.getPixelsRGBA();
                }

                return var5;
            } catch (Exception var11) {
                LOGGER.error("Couldn't load texture {}", param1, var11);
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public SpriteSourceType type() {
        return SpriteSources.PALETTED_PERMUTATIONS;
    }

    @OnlyIn(Dist.CLIENT)
    static record PalettedSpriteSupplier(LazyLoadedImage baseImage, Supplier<IntUnaryOperator> palette, ResourceLocation permutationLocation)
        implements SpriteSource.SpriteSupplier {
        @Nullable
        public SpriteContents apply(SpriteResourceLoader param0) {
            Object var3;
            try {
                NativeImage var0 = this.baseImage.get().mappedCopy(this.palette.get());
                return new SpriteContents(this.permutationLocation, new FrameSize(var0.getWidth(), var0.getHeight()), var0, ResourceMetadata.EMPTY);
            } catch (IllegalArgumentException | IOException var7) {
                PalettedPermutations.LOGGER.error("unable to apply palette to {}", this.permutationLocation, var7);
                var3 = null;
            } finally {
                this.baseImage.release();
            }

            return (SpriteContents)var3;
        }

        @Override
        public void discard() {
            this.baseImage.release();
        }
    }
}
