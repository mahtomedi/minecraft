package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PreloadedTexture extends SimpleTexture {
    @Nullable
    private CompletableFuture<SimpleTexture.TextureImage> future;

    public PreloadedTexture(ResourceManager param0, ResourceLocation param1, Executor param2) {
        super(param1);
        this.future = CompletableFuture.supplyAsync(() -> SimpleTexture.TextureImage.load(param0, param1), param2);
    }

    @Override
    protected SimpleTexture.TextureImage getTextureImage(ResourceManager param0) {
        if (this.future != null) {
            SimpleTexture.TextureImage var0 = this.future.join();
            this.future = null;
            return var0;
        } else {
            return SimpleTexture.TextureImage.load(param0, this.location);
        }
    }

    public CompletableFuture<Void> getFuture() {
        return this.future == null ? CompletableFuture.completedFuture(null) : this.future.thenApply(param0 -> null);
    }

    @Override
    public void reset(TextureManager param0, ResourceManager param1, ResourceLocation param2, Executor param3) {
        this.future = CompletableFuture.supplyAsync(() -> SimpleTexture.TextureImage.load(param1, this.location), Util.backgroundExecutor());
        this.future.thenRunAsync(() -> param0.register(this.location, this), executor(param3));
    }

    private static Executor executor(Executor param0) {
        return param1 -> param0.execute(() -> RenderSystem.recordRenderCall(param1::run));
    }
}
