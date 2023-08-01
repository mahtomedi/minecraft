package net.minecraft.client.renderer.texture.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface SpriteResourceLoader {
    Logger LOGGER = LogUtils.getLogger();

    static SpriteResourceLoader create(Collection<MetadataSectionSerializer<?>> param0) {
        return (param1, param2) -> {
            ResourceMetadata var0;
            try {
                var0 = param2.metadata().copySections(param0);
            } catch (Exception var91) {
                LOGGER.error("Unable to parse metadata from {}", param1, var91);
                return null;
            }

            NativeImage var4;
            try (InputStream var3 = param2.open()) {
                var4 = NativeImage.read(var3);
            } catch (IOException var11) {
                LOGGER.error("Using missing texture, unable to load {}", param1, var11);
                return null;
            }

            AnimationMetadataSection var8 = var0.getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
            FrameSize var9 = var8.calculateFrameSize(var4.getWidth(), var4.getHeight());
            if (Mth.isMultipleOf(var4.getWidth(), var9.width()) && Mth.isMultipleOf(var4.getHeight(), var9.height())) {
                return new SpriteContents(param1, var9, var4, var0);
            } else {
                LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", param1, var4.getWidth(), var4.getHeight(), var9.width(), var9.height());
                var4.close();
                return null;
            }
        };
    }

    @Nullable
    SpriteContents loadSprite(ResourceLocation var1, Resource var2);
}
