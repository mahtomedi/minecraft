package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LazyLoadedImage {
    private final ResourceLocation id;
    private final Resource resource;
    private final AtomicReference<NativeImage> image = new AtomicReference<>();
    private final AtomicInteger referenceCount;

    public LazyLoadedImage(ResourceLocation param0, Resource param1, int param2) {
        this.id = param0;
        this.resource = param1;
        this.referenceCount = new AtomicInteger(param2);
    }

    public NativeImage get() throws IOException {
        NativeImage var0 = this.image.get();
        if (var0 == null) {
            synchronized(this) {
                var0 = this.image.get();
                if (var0 == null) {
                    try (InputStream var1 = this.resource.open()) {
                        var0 = NativeImage.read(var1);
                        this.image.set(var0);
                    } catch (IOException var9) {
                        throw new IOException("Failed to load image " + this.id, var9);
                    }
                }
            }
        }

        return var0;
    }

    public void release() {
        int var0 = this.referenceCount.decrementAndGet();
        if (var0 <= 0) {
            NativeImage var1 = this.image.getAndSet(null);
            if (var1 != null) {
                var1.close();
            }
        }

    }
}
