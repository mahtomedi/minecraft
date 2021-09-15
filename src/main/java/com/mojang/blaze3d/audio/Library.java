package com.mojang.blaze3d.audio;

import com.google.common.collect.Sets;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.system.MemoryStack;

@OnlyIn(Dist.CLIENT)
public class Library {
    static final Logger LOGGER = LogManager.getLogger();
    private static final int DEFAULT_CHANNEL_COUNT = 30;
    private long device;
    private long context;
    private static final Library.ChannelPool EMPTY = new Library.ChannelPool() {
        @Nullable
        @Override
        public Channel acquire() {
            return null;
        }

        @Override
        public boolean release(Channel param0) {
            return false;
        }

        @Override
        public void cleanup() {
        }

        @Override
        public int getMaxCount() {
            return 0;
        }

        @Override
        public int getUsedCount() {
            return 0;
        }
    };
    private Library.ChannelPool staticChannels = EMPTY;
    private Library.ChannelPool streamingChannels = EMPTY;
    private final Listener listener = new Listener();

    public void init(@Nullable String param0) {
        this.device = openDeviceOrFallback(param0);
        ALCCapabilities var0 = ALC.createCapabilities(this.device);
        if (OpenAlUtil.checkALCError(this.device, "Get capabilities")) {
            throw new IllegalStateException("Failed to get OpenAL capabilities");
        } else if (!var0.OpenALC11) {
            throw new IllegalStateException("OpenAL 1.1 not supported");
        } else {
            this.context = ALC10.alcCreateContext(this.device, (IntBuffer)null);
            ALC10.alcMakeContextCurrent(this.context);
            int var1 = this.getChannelCount();
            int var2 = Mth.clamp((int)Mth.sqrt((float)var1), 2, 8);
            int var3 = Mth.clamp(var1 - var2, 8, 255);
            this.staticChannels = new Library.CountingChannelPool(var3);
            this.streamingChannels = new Library.CountingChannelPool(var2);
            ALCapabilities var4 = AL.createCapabilities(var0);
            OpenAlUtil.checkALError("Initialization");
            if (!var4.AL_EXT_source_distance_model) {
                throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
            } else {
                AL10.alEnable(512);
                if (!var4.AL_EXT_LINEAR_DISTANCE) {
                    throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
                } else {
                    OpenAlUtil.checkALError("Enable per-source distance models");
                    LOGGER.info("OpenAL initialized.");
                }
            }
        }
    }

    private int getChannelCount() {
        try (MemoryStack var0 = MemoryStack.stackPush()) {
            int var1 = ALC10.alcGetInteger(this.device, 4098);
            if (OpenAlUtil.checkALCError(this.device, "Get attributes size")) {
                throw new IllegalStateException("Failed to get OpenAL attributes");
            }

            IntBuffer var2 = var0.mallocInt(var1);
            ALC10.alcGetIntegerv(this.device, 4099, var2);
            if (OpenAlUtil.checkALCError(this.device, "Get attributes")) {
                throw new IllegalStateException("Failed to get OpenAL attributes");
            }

            int var3 = 0;

            while(var3 < var1) {
                int var4 = var2.get(var3++);
                if (var4 == 0) {
                    break;
                }

                int var5 = var2.get(var3++);
                if (var4 == 4112) {
                    return var5;
                }
            }
        }

        return 30;
    }

    @Nullable
    public static String getDefaultDeviceName() {
        return !ALC10.alcIsExtensionPresent(0L, "ALC_ENUMERATE_ALL_EXT") ? null : ALC10.alcGetString(0L, 4115);
    }

    private static long openDeviceOrFallback(@Nullable String param0) {
        OptionalLong var0 = OptionalLong.empty();
        if (param0 != null) {
            var0 = tryOpenDevice(param0);
        }

        if (var0.isEmpty()) {
            var0 = tryOpenDevice(getDefaultDeviceName());
        }

        if (var0.isEmpty()) {
            var0 = tryOpenDevice(null);
        }

        if (var0.isEmpty()) {
            throw new IllegalStateException("Failed to open OpenAL device");
        } else {
            return var0.getAsLong();
        }
    }

    private static OptionalLong tryOpenDevice(@Nullable String param0) {
        long var0 = ALC10.alcOpenDevice(param0);
        return var0 != 0L && !OpenAlUtil.checkALCError(var0, "Open device") ? OptionalLong.of(var0) : OptionalLong.empty();
    }

    public void cleanup() {
        this.staticChannels.cleanup();
        this.streamingChannels.cleanup();
        ALC10.alcDestroyContext(this.context);
        if (this.device != 0L) {
            ALC10.alcCloseDevice(this.device);
        }

    }

    public Listener getListener() {
        return this.listener;
    }

    @Nullable
    public Channel acquireChannel(Library.Pool param0) {
        return (param0 == Library.Pool.STREAMING ? this.streamingChannels : this.staticChannels).acquire();
    }

    public void releaseChannel(Channel param0) {
        if (!this.staticChannels.release(param0) && !this.streamingChannels.release(param0)) {
            throw new IllegalStateException("Tried to release unknown channel");
        }
    }

    public String getDebugString() {
        return String.format(
            "Sounds: %d/%d + %d/%d",
            this.staticChannels.getUsedCount(),
            this.staticChannels.getMaxCount(),
            this.streamingChannels.getUsedCount(),
            this.streamingChannels.getMaxCount()
        );
    }

    public List<String> getAvailableSoundDevices() {
        List<String> var0 = ALUtil.getStringList(0L, 4115);
        return var0 == null ? Collections.emptyList() : var0;
    }

    @OnlyIn(Dist.CLIENT)
    interface ChannelPool {
        @Nullable
        Channel acquire();

        boolean release(Channel var1);

        void cleanup();

        int getMaxCount();

        int getUsedCount();
    }

    @OnlyIn(Dist.CLIENT)
    static class CountingChannelPool implements Library.ChannelPool {
        private final int limit;
        private final Set<Channel> activeChannels = Sets.newIdentityHashSet();

        public CountingChannelPool(int param0) {
            this.limit = param0;
        }

        @Nullable
        @Override
        public Channel acquire() {
            if (this.activeChannels.size() >= this.limit) {
                Library.LOGGER.warn("Maximum sound pool size {} reached", this.limit);
                return null;
            } else {
                Channel var0 = Channel.create();
                if (var0 != null) {
                    this.activeChannels.add(var0);
                }

                return var0;
            }
        }

        @Override
        public boolean release(Channel param0) {
            if (!this.activeChannels.remove(param0)) {
                return false;
            } else {
                param0.destroy();
                return true;
            }
        }

        @Override
        public void cleanup() {
            this.activeChannels.forEach(Channel::destroy);
            this.activeChannels.clear();
        }

        @Override
        public int getMaxCount() {
            return this.limit;
        }

        @Override
        public int getUsedCount() {
            return this.activeChannels.size();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Pool {
        STATIC,
        STREAMING;
    }
}
