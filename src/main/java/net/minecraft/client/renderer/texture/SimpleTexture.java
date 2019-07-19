package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SimpleTexture extends AbstractTexture {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final ResourceLocation location;

    public SimpleTexture(ResourceLocation param0) {
        this.location = param0;
    }

    @Override
    public void load(ResourceManager param0) throws IOException {
        try (SimpleTexture.TextureImage var0 = this.getTextureImage(param0)) {
            boolean var1 = false;
            boolean var2 = false;
            var0.throwIfError();
            TextureMetadataSection var3 = var0.getTextureMetadata();
            if (var3 != null) {
                var1 = var3.isBlur();
                var2 = var3.isClamp();
            }

            this.bind();
            TextureUtil.prepareImage(this.getId(), 0, var0.getImage().getWidth(), var0.getImage().getHeight());
            var0.getImage().upload(0, 0, 0, 0, 0, var0.getImage().getWidth(), var0.getImage().getHeight(), var1, var2, false);
        }

    }

    protected SimpleTexture.TextureImage getTextureImage(ResourceManager param0) {
        return SimpleTexture.TextureImage.load(param0, this.location);
    }

    @OnlyIn(Dist.CLIENT)
    public static class TextureImage implements Closeable {
        private final TextureMetadataSection metadata;
        private final NativeImage image;
        private final IOException exception;

        public TextureImage(IOException param0) {
            this.exception = param0;
            this.metadata = null;
            this.image = null;
        }

        public TextureImage(@Nullable TextureMetadataSection param0, NativeImage param1) {
            this.exception = null;
            this.metadata = param0;
            this.image = param1;
        }

        public static SimpleTexture.TextureImage load(ResourceManager param0, ResourceLocation param1) {
            try (Resource var0 = param0.getResource(param1)) {
                NativeImage var1 = NativeImage.read(var0.getInputStream());
                TextureMetadataSection var2 = null;

                try {
                    var2 = var0.getMetadata(TextureMetadataSection.SERIALIZER);
                } catch (RuntimeException var17) {
                    SimpleTexture.LOGGER.warn("Failed reading metadata of: {}", param1, var17);
                }

                return new SimpleTexture.TextureImage(var2, var1);
            } catch (IOException var20) {
                return new SimpleTexture.TextureImage(var20);
            }
        }

        @Nullable
        public TextureMetadataSection getTextureMetadata() {
            return this.metadata;
        }

        public NativeImage getImage() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            } else {
                return this.image;
            }
        }

        @Override
        public void close() {
            if (this.image != null) {
                this.image.close();
            }

        }

        public void throwIfError() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }
}
