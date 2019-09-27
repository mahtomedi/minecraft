package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DynamicTexture extends AbstractTexture implements AutoCloseable {
    private NativeImage pixels;

    public DynamicTexture(NativeImage param0) {
        this.pixels = param0;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
                this.upload();
            });
        } else {
            TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
            this.upload();
        }

    }

    public DynamicTexture(int param0, int param1, boolean param2) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        this.pixels = new NativeImage(param0, param1, param2);
        TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
    }

    @Override
    public void load(ResourceManager param0) throws IOException {
    }

    public void upload() {
        this.bind();
        this.pixels.upload(0, 0, 0, false);
    }

    @Nullable
    public NativeImage getPixels() {
        return this.pixels;
    }

    public void setPixels(NativeImage param0) throws Exception {
        this.pixels.close();
        this.pixels = param0;
    }

    @Override
    public void close() {
        this.pixels.close();
        this.releaseId();
        this.pixels = null;
    }
}
