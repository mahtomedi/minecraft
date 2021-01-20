package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MissingTextureAtlasSprite extends TextureAtlasSprite {
    private static final ResourceLocation MISSING_TEXTURE_LOCATION = new ResourceLocation("missingno");
    @Nullable
    private static DynamicTexture missingTexture;
    private static final LazyLoadedValue<NativeImage> MISSING_IMAGE_DATA = new LazyLoadedValue<>(() -> {
        NativeImage var0 = new NativeImage(16, 16, false);
        int var1 = -16777216;
        int var2 = -524040;

        for(int var3 = 0; var3 < 16; ++var3) {
            for(int var4 = 0; var4 < 16; ++var4) {
                if (var3 < 8 ^ var4 < 8) {
                    var0.setPixelRGBA(var4, var3, -524040);
                } else {
                    var0.setPixelRGBA(var4, var3, -16777216);
                }
            }
        }

        var0.untrack();
        return var0;
    });
    private static final TextureAtlasSprite.Info INFO = new TextureAtlasSprite.Info(
        MISSING_TEXTURE_LOCATION, 16, 16, new AnimationMetadataSection(ImmutableList.of(new AnimationFrame(0, -1)), 16, 16, 1, false)
    );

    private MissingTextureAtlasSprite(TextureAtlas param0, int param1, int param2, int param3, int param4, int param5) {
        super(param0, INFO, param1, param2, param3, param4, param5, MISSING_IMAGE_DATA.get());
    }

    public static MissingTextureAtlasSprite newInstance(TextureAtlas param0, int param1, int param2, int param3, int param4, int param5) {
        return new MissingTextureAtlasSprite(param0, param1, param2, param3, param4, param5);
    }

    public static ResourceLocation getLocation() {
        return MISSING_TEXTURE_LOCATION;
    }

    public static TextureAtlasSprite.Info info() {
        return INFO;
    }

    @Override
    public void close() {
        for(int var0 = 1; var0 < this.mainImage.length; ++var0) {
            this.mainImage[var0].close();
        }

    }

    public static DynamicTexture getTexture() {
        if (missingTexture == null) {
            missingTexture = new DynamicTexture(MISSING_IMAGE_DATA.get());
            Minecraft.getInstance().getTextureManager().register(MISSING_TEXTURE_LOCATION, missingTexture);
        }

        return missingTexture;
    }
}
