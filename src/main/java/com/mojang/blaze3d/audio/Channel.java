package com.mojang.blaze3d.audio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL10;

@OnlyIn(Dist.CLIENT)
public class Channel {
    private static final Logger LOGGER = LogManager.getLogger();
    private final int source;
    private AtomicBoolean initialized = new AtomicBoolean(true);
    private int streamingBufferSize = 16384;
    @Nullable
    private AudioStream stream;

    @Nullable
    static Channel create() {
        int[] var0 = new int[1];
        AL10.alGenSources(var0);
        return OpenAlUtil.checkALError("Allocate new source") ? null : new Channel(var0[0]);
    }

    private Channel(int param0) {
        this.source = param0;
    }

    public void destroy() {
        if (this.initialized.compareAndSet(true, false)) {
            AL10.alSourceStop(this.source);
            OpenAlUtil.checkALError("Stop");
            if (this.stream != null) {
                try {
                    this.stream.close();
                } catch (IOException var2) {
                    LOGGER.error("Failed to close audio stream", (Throwable)var2);
                }

                this.removeProcessedBuffers();
                this.stream = null;
            }

            AL10.alDeleteSources(new int[]{this.source});
            OpenAlUtil.checkALError("Cleanup");
        }

    }

    public void play() {
        AL10.alSourcePlay(this.source);
    }

    private int getState() {
        return !this.initialized.get() ? 4116 : AL10.alGetSourcei(this.source, 4112);
    }

    public void pause() {
        if (this.getState() == 4114) {
            AL10.alSourcePause(this.source);
        }

    }

    public void unpause() {
        if (this.getState() == 4115) {
            AL10.alSourcePlay(this.source);
        }

    }

    public void stop() {
        if (this.initialized.get()) {
            AL10.alSourceStop(this.source);
            OpenAlUtil.checkALError("Stop");
        }

    }

    public boolean stopped() {
        return this.getState() == 4116;
    }

    public void setSelfPosition(Vec3 param0) {
        AL10.alSourcefv(this.source, 4100, new float[]{(float)param0.x, (float)param0.y, (float)param0.z});
    }

    public void setPitch(float param0) {
        AL10.alSourcef(this.source, 4099, param0);
    }

    public void setLooping(boolean param0) {
        AL10.alSourcei(this.source, 4103, param0 ? 1 : 0);
    }

    public void setVolume(float param0) {
        AL10.alSourcef(this.source, 4106, param0);
    }

    public void disableAttenuation() {
        AL10.alSourcei(this.source, 53248, 0);
    }

    public void linearAttenuation(float param0) {
        AL10.alSourcei(this.source, 53248, 53251);
        AL10.alSourcef(this.source, 4131, param0);
        AL10.alSourcef(this.source, 4129, 1.0F);
        AL10.alSourcef(this.source, 4128, 0.0F);
    }

    public void setRelative(boolean param0) {
        AL10.alSourcei(this.source, 514, param0 ? 1 : 0);
    }

    public void attachStaticBuffer(SoundBuffer param0) {
        param0.getAlBuffer().ifPresent(param0x -> AL10.alSourcei(this.source, 4105, param0x));
    }

    public void attachBufferStream(AudioStream param0) {
        this.stream = param0;
        AudioFormat var0 = param0.getFormat();
        this.streamingBufferSize = calculateBufferSize(var0, 1);
        this.pumpBuffers(4);
    }

    private static int calculateBufferSize(AudioFormat param0, int param1) {
        return (int)((float)(param1 * param0.getSampleSizeInBits()) / 8.0F * (float)param0.getChannels() * param0.getSampleRate());
    }

    private void pumpBuffers(int param0) {
        if (this.stream != null) {
            try {
                for(int var0 = 0; var0 < param0; ++var0) {
                    ByteBuffer var1 = this.stream.read(this.streamingBufferSize);
                    if (var1 != null) {
                        new SoundBuffer(var1, this.stream.getFormat())
                            .releaseAlBuffer()
                            .ifPresent(param0x -> AL10.alSourceQueueBuffers(this.source, new int[]{param0x}));
                    }
                }
            } catch (IOException var4) {
                LOGGER.error("Failed to read from audio stream", (Throwable)var4);
            }
        }

    }

    public void updateStream() {
        if (this.stream != null) {
            int var0 = this.removeProcessedBuffers();
            this.pumpBuffers(var0);
        }

    }

    private int removeProcessedBuffers() {
        int var0 = AL10.alGetSourcei(this.source, 4118);
        if (var0 > 0) {
            int[] var1 = new int[var0];
            AL10.alSourceUnqueueBuffers(this.source, var1);
            OpenAlUtil.checkALError("Unqueue buffers");
            AL10.alDeleteBuffers(var1);
            OpenAlUtil.checkALError("Remove processed buffers");
        }

        return var0;
    }
}
