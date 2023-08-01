package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MissingTextureAtlasSprite {
    private static final int MISSING_IMAGE_WIDTH = 16;
    private static final int MISSING_IMAGE_HEIGHT = 16;
    private static final String MISSING_TEXTURE_NAME = "missingno";
    private static final ResourceLocation MISSING_TEXTURE_LOCATION = new ResourceLocation("missingno");
    private static final ResourceMetadata SPRITE_METADATA = new ResourceMetadata.Builder()
        .put(AnimationMetadataSection.SERIALIZER, new AnimationMetadataSection(ImmutableList.of(new AnimationFrame(0, -1)), 16, 16, 1, false))
        .build();
    @Nullable
    private static DynamicTexture missingTexture;

    private static NativeImage generateMissingImage(int param0, int param1) {
        NativeImage var0 = new NativeImage(param0, param1, false);
        int var1 = -16777216;
        int var2 = -524040;

        for(int var3 = 0; var3 < param1; ++var3) {
            for(int var4 = 0; var4 < param0; ++var4) {
                if (var3 < param1 / 2 ^ var4 < param0 / 2) {
                    var0.setPixelRGBA(var4, var3, -524040);
                } else {
                    var0.setPixelRGBA(var4, var3, -16777216);
                }
            }
        }

        return var0;
    }

    public static SpriteContents create() {
        NativeImage var0 = generateMissingImage(16, 16);
        return new SpriteContents(MISSING_TEXTURE_LOCATION, new FrameSize(16, 16), var0, SPRITE_METADATA);
    }

    public static ResourceLocation getLocation() {
        return MISSING_TEXTURE_LOCATION;
    }

    public static DynamicTexture getTexture() {
        if (missingTexture == null) {
            NativeImage var0 = generateMissingImage(16, 16);
            var0.untrack();
            missingTexture = new DynamicTexture(var0);
            Minecraft.getInstance().getTextureManager().register(MISSING_TEXTURE_LOCATION, missingTexture);
        }

        return missingTexture;
    }
}
