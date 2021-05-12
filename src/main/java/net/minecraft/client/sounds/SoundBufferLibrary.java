package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundBufferLibrary {
    private final ResourceManager resourceManager;
    private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.newHashMap();

    public SoundBufferLibrary(ResourceManager param0) {
        this.resourceManager = param0;
    }

    public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation param0) {
        return this.cache.computeIfAbsent(param0, param0x -> CompletableFuture.supplyAsync(() -> {
                try {
                    SoundBuffer var6;
                    try (
                        Resource var0 = this.resourceManager.getResource(param0x);
                        InputStream var1x = var0.getInputStream();
                        OggAudioStream var2 = new OggAudioStream(var1x);
                    ) {
                        ByteBuffer var3 = var2.readAll();
                        var6 = new SoundBuffer(var3, var2.getFormat());
                    }

                    return var6;
                } catch (IOException var13) {
                    throw new CompletionException(var13);
                }
            }, Util.backgroundExecutor()));
    }

    public CompletableFuture<AudioStream> getStream(ResourceLocation param0, boolean param1) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Resource var2x = this.resourceManager.getResource(param0);
                InputStream var1x = var2x.getInputStream();
                return (AudioStream)(param1 ? new LoopingAudioStream(OggAudioStream::new, var1x) : new OggAudioStream(var1x));
            } catch (IOException var5) {
                throw new CompletionException(var5);
            }
        }, Util.backgroundExecutor());
    }

    public void clear() {
        this.cache.values().forEach(param0 -> param0.thenAccept(SoundBuffer::discardAlBuffer));
        this.cache.clear();
    }

    public CompletableFuture<?> preload(Collection<Sound> param0) {
        return CompletableFuture.allOf(
            param0.stream().map(param0x -> this.getCompleteBuffer(param0x.getPath())).toArray(param0x -> new CompletableFuture[param0x])
        );
    }
}
