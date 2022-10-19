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
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundBufferLibrary {
    private final ResourceProvider resourceManager;
    private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.newHashMap();

    public SoundBufferLibrary(ResourceProvider param0) {
        this.resourceManager = param0;
    }

    public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation param0) {
        return this.cache.computeIfAbsent(param0, param0x -> CompletableFuture.supplyAsync(() -> {
                try {
                    SoundBuffer var5;
                    try (
                        InputStream var0 = this.resourceManager.open(param0x);
                        OggAudioStream var1x = new OggAudioStream(var0);
                    ) {
                        ByteBuffer var2 = var1x.readAll();
                        var5 = new SoundBuffer(var2, var1x.getFormat());
                    }

                    return var5;
                } catch (IOException var10) {
                    throw new CompletionException(var10);
                }
            }, Util.backgroundExecutor()));
    }

    public CompletableFuture<AudioStream> getStream(ResourceLocation param0, boolean param1) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InputStream var1x = this.resourceManager.open(param0);
                return (AudioStream)(param1 ? new LoopingAudioStream(OggAudioStream::new, var1x) : new OggAudioStream(var1x));
            } catch (IOException var4) {
                throw new CompletionException(var4);
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
