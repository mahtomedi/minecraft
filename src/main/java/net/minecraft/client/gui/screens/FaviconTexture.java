package net.minecraft.client.gui.screens;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FaviconTexture implements AutoCloseable {
    private static final ResourceLocation MISSING_LOCATION = new ResourceLocation("textures/misc/unknown_server.png");
    private static final int WIDTH = 64;
    private static final int HEIGHT = 64;
    private final TextureManager textureManager;
    private final ResourceLocation textureLocation;
    @Nullable
    private DynamicTexture texture;
    private boolean closed;

    private FaviconTexture(TextureManager param0, ResourceLocation param1) {
        this.textureManager = param0;
        this.textureLocation = param1;
    }

    public static FaviconTexture forWorld(TextureManager param0, String param1) {
        return new FaviconTexture(
            param0,
            new ResourceLocation(
                "minecraft", "worlds/" + Util.sanitizeName(param1, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(param1) + "/icon"
            )
        );
    }

    public static FaviconTexture forServer(TextureManager param0, String param1) {
        return new FaviconTexture(param0, new ResourceLocation("minecraft", "servers/" + Hashing.sha1().hashUnencodedChars(param1) + "/icon"));
    }

    public void upload(NativeImage param0) {
        if (param0.getWidth() == 64 && param0.getHeight() == 64) {
            try {
                this.checkOpen();
                if (this.texture == null) {
                    this.texture = new DynamicTexture(param0);
                } else {
                    this.texture.setPixels(param0);
                    this.texture.upload();
                }

                this.textureManager.register(this.textureLocation, this.texture);
            } catch (Throwable var3) {
                param0.close();
                this.clear();
                throw var3;
            }
        } else {
            param0.close();
            throw new IllegalArgumentException("Icon must be 64x64, but was " + param0.getWidth() + "x" + param0.getHeight());
        }
    }

    public void clear() {
        this.checkOpen();
        if (this.texture != null) {
            this.textureManager.release(this.textureLocation);
            this.texture.close();
            this.texture = null;
        }

    }

    public ResourceLocation textureLocation() {
        return this.texture != null ? this.textureLocation : MISSING_LOCATION;
    }

    @Override
    public void close() {
        this.clear();
        this.closed = true;
    }

    private void checkOpen() {
        if (this.closed) {
            throw new IllegalStateException("Icon already closed");
        }
    }
}
