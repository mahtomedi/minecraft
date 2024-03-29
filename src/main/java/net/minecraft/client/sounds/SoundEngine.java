package net.minecraft.client.sounds;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@OnlyIn(Dist.CLIENT)
public class SoundEngine {
    private static final Marker MARKER = MarkerFactory.getMarker("SOUNDS");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float PITCH_MIN = 0.5F;
    private static final float PITCH_MAX = 2.0F;
    private static final float VOLUME_MIN = 0.0F;
    private static final float VOLUME_MAX = 1.0F;
    private static final int MIN_SOURCE_LIFETIME = 20;
    private static final Set<ResourceLocation> ONLY_WARN_ONCE = Sets.newHashSet();
    private static final long DEFAULT_DEVICE_CHECK_INTERVAL_MS = 1000L;
    public static final String MISSING_SOUND = "FOR THE DEBUG!";
    public static final String OPEN_AL_SOFT_PREFIX = "OpenAL Soft on ";
    public static final int OPEN_AL_SOFT_PREFIX_LENGTH = "OpenAL Soft on ".length();
    private final SoundManager soundManager;
    private final Options options;
    private boolean loaded;
    private final Library library = new Library();
    private final Listener listener = this.library.getListener();
    private final SoundBufferLibrary soundBuffers;
    private final SoundEngineExecutor executor = new SoundEngineExecutor();
    private final ChannelAccess channelAccess = new ChannelAccess(this.library, this.executor);
    private int tickCount;
    private long lastDeviceCheckTime;
    private final AtomicReference<SoundEngine.DeviceCheckState> devicePoolState = new AtomicReference<>(SoundEngine.DeviceCheckState.NO_CHANGE);
    private final Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = Maps.newHashMap();
    private final Multimap<SoundSource, SoundInstance> instanceBySource = HashMultimap.create();
    private final List<TickableSoundInstance> tickingSounds = Lists.newArrayList();
    private final Map<SoundInstance, Integer> queuedSounds = Maps.newHashMap();
    private final Map<SoundInstance, Integer> soundDeleteTime = Maps.newHashMap();
    private final List<SoundEventListener> listeners = Lists.newArrayList();
    private final List<TickableSoundInstance> queuedTickableSounds = Lists.newArrayList();
    private final List<Sound> preloadQueue = Lists.newArrayList();

    public SoundEngine(SoundManager param0, Options param1, ResourceProvider param2) {
        this.soundManager = param0;
        this.options = param1;
        this.soundBuffers = new SoundBufferLibrary(param2);
    }

    public void reload() {
        ONLY_WARN_ONCE.clear();

        for(SoundEvent var0 : BuiltInRegistries.SOUND_EVENT) {
            if (var0 != SoundEvents.EMPTY) {
                ResourceLocation var1 = var0.getLocation();
                if (this.soundManager.getSoundEvent(var1) == null) {
                    LOGGER.warn("Missing sound for event: {}", BuiltInRegistries.SOUND_EVENT.getKey(var0));
                    ONLY_WARN_ONCE.add(var1);
                }
            }
        }

        this.destroy();
        this.loadLibrary();
    }

    private synchronized void loadLibrary() {
        if (!this.loaded) {
            try {
                String var0 = this.options.soundDevice().get();
                this.library.init("".equals(var0) ? null : var0, this.options.directionalAudio().get());
                this.listener.reset();
                this.listener.setGain(this.options.getSoundSourceVolume(SoundSource.MASTER));
                this.soundBuffers.preload(this.preloadQueue).thenRun(this.preloadQueue::clear);
                this.loaded = true;
                LOGGER.info(MARKER, "Sound engine started");
            } catch (RuntimeException var2) {
                LOGGER.error(MARKER, "Error starting SoundSystem. Turning off sounds & music", (Throwable)var2);
            }

        }
    }

    private float getVolume(@Nullable SoundSource param0) {
        return param0 != null && param0 != SoundSource.MASTER ? this.options.getSoundSourceVolume(param0) : 1.0F;
    }

    public void updateCategoryVolume(SoundSource param0, float param1) {
        if (this.loaded) {
            if (param0 == SoundSource.MASTER) {
                this.listener.setGain(param1);
            } else {
                this.instanceToChannel.forEach((param0x, param1x) -> {
                    float var0 = this.calculateVolume(param0x);
                    param1x.execute(param1xx -> {
                        if (var0 <= 0.0F) {
                            param1xx.stop();
                        } else {
                            param1xx.setVolume(var0);
                        }

                    });
                });
            }
        }
    }

    public void destroy() {
        if (this.loaded) {
            this.stopAll();
            this.soundBuffers.clear();
            this.library.cleanup();
            this.loaded = false;
        }

    }

    public void emergencyShutdown() {
        if (this.loaded) {
            this.library.cleanup();
        }

    }

    public void stop(SoundInstance param0) {
        if (this.loaded) {
            ChannelAccess.ChannelHandle var0 = this.instanceToChannel.get(param0);
            if (var0 != null) {
                var0.execute(Channel::stop);
            }
        }

    }

    public void stopAll() {
        if (this.loaded) {
            this.executor.flush();
            this.instanceToChannel.values().forEach(param0 -> param0.execute(Channel::stop));
            this.instanceToChannel.clear();
            this.channelAccess.clear();
            this.queuedSounds.clear();
            this.tickingSounds.clear();
            this.instanceBySource.clear();
            this.soundDeleteTime.clear();
            this.queuedTickableSounds.clear();
        }

    }

    public void addEventListener(SoundEventListener param0) {
        this.listeners.add(param0);
    }

    public void removeEventListener(SoundEventListener param0) {
        this.listeners.remove(param0);
    }

    private boolean shouldChangeDevice() {
        if (this.library.isCurrentDeviceDisconnected()) {
            LOGGER.info("Audio device was lost!");
            return true;
        } else {
            long var0 = Util.getMillis();
            boolean var1 = var0 - this.lastDeviceCheckTime >= 1000L;
            if (var1) {
                this.lastDeviceCheckTime = var0;
                if (this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.NO_CHANGE, SoundEngine.DeviceCheckState.ONGOING)) {
                    String var2 = this.options.soundDevice().get();
                    Util.ioPool().execute(() -> {
                        if ("".equals(var2)) {
                            if (this.library.hasDefaultDeviceChanged()) {
                                LOGGER.info("System default audio device has changed!");
                                this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.ONGOING, SoundEngine.DeviceCheckState.CHANGE_DETECTED);
                            }
                        } else if (!this.library.getCurrentDeviceName().equals(var2) && this.library.getAvailableSoundDevices().contains(var2)) {
                            LOGGER.info("Preferred audio device has become available!");
                            this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.ONGOING, SoundEngine.DeviceCheckState.CHANGE_DETECTED);
                        }

                        this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.ONGOING, SoundEngine.DeviceCheckState.NO_CHANGE);
                    });
                }
            }

            return this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.CHANGE_DETECTED, SoundEngine.DeviceCheckState.NO_CHANGE);
        }
    }

    public void tick(boolean param0) {
        if (this.shouldChangeDevice()) {
            this.reload();
        }

        if (!param0) {
            this.tickNonPaused();
        }

        this.channelAccess.scheduleTick();
    }

    private void tickNonPaused() {
        ++this.tickCount;
        this.queuedTickableSounds.stream().filter(SoundInstance::canPlaySound).forEach(this::play);
        this.queuedTickableSounds.clear();

        for(TickableSoundInstance var0 : this.tickingSounds) {
            if (!var0.canPlaySound()) {
                this.stop(var0);
            }

            var0.tick();
            if (var0.isStopped()) {
                this.stop(var0);
            } else {
                float var1 = this.calculateVolume(var0);
                float var2 = this.calculatePitch(var0);
                Vec3 var3 = new Vec3(var0.getX(), var0.getY(), var0.getZ());
                ChannelAccess.ChannelHandle var4 = this.instanceToChannel.get(var0);
                if (var4 != null) {
                    var4.execute(param3 -> {
                        param3.setVolume(var1);
                        param3.setPitch(var2);
                        param3.setSelfPosition(var3);
                    });
                }
            }
        }

        Iterator<Entry<SoundInstance, ChannelAccess.ChannelHandle>> var5 = this.instanceToChannel.entrySet().iterator();

        while(var5.hasNext()) {
            Entry<SoundInstance, ChannelAccess.ChannelHandle> var6 = var5.next();
            ChannelAccess.ChannelHandle var7 = var6.getValue();
            SoundInstance var8 = var6.getKey();
            float var9 = this.options.getSoundSourceVolume(var8.getSource());
            if (var9 <= 0.0F) {
                var7.execute(Channel::stop);
                var5.remove();
            } else if (var7.isStopped()) {
                int var10 = this.soundDeleteTime.get(var8);
                if (var10 <= this.tickCount) {
                    if (shouldLoopManually(var8)) {
                        this.queuedSounds.put(var8, this.tickCount + var8.getDelay());
                    }

                    var5.remove();
                    LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", var7);
                    this.soundDeleteTime.remove(var8);

                    try {
                        this.instanceBySource.remove(var8.getSource(), var8);
                    } catch (RuntimeException var8) {
                    }

                    if (var8 instanceof TickableSoundInstance) {
                        this.tickingSounds.remove(var8);
                    }
                }
            }
        }

        Iterator<Entry<SoundInstance, Integer>> var11 = this.queuedSounds.entrySet().iterator();

        while(var11.hasNext()) {
            Entry<SoundInstance, Integer> var12 = var11.next();
            if (this.tickCount >= var12.getValue()) {
                SoundInstance var13 = var12.getKey();
                if (var13 instanceof TickableSoundInstance) {
                    ((TickableSoundInstance)var13).tick();
                }

                this.play(var13);
                var11.remove();
            }
        }

    }

    private static boolean requiresManualLooping(SoundInstance param0) {
        return param0.getDelay() > 0;
    }

    private static boolean shouldLoopManually(SoundInstance param0) {
        return param0.isLooping() && requiresManualLooping(param0);
    }

    private static boolean shouldLoopAutomatically(SoundInstance param0) {
        return param0.isLooping() && !requiresManualLooping(param0);
    }

    public boolean isActive(SoundInstance param0) {
        if (!this.loaded) {
            return false;
        } else {
            return this.soundDeleteTime.containsKey(param0) && this.soundDeleteTime.get(param0) <= this.tickCount
                ? true
                : this.instanceToChannel.containsKey(param0);
        }
    }

    public void play(SoundInstance param0) {
        if (this.loaded) {
            if (param0.canPlaySound()) {
                WeighedSoundEvents var0x = param0.resolve(this.soundManager);
                ResourceLocation var1x = param0.getLocation();
                if (var0x == null) {
                    if (ONLY_WARN_ONCE.add(var1x)) {
                        LOGGER.warn(MARKER, "Unable to play unknown soundEvent: {}", var1x);
                    }

                } else {
                    Sound var2x = param0.getSound();
                    if (var2x != SoundManager.INTENTIONALLY_EMPTY_SOUND) {
                        if (var2x == SoundManager.EMPTY_SOUND) {
                            if (ONLY_WARN_ONCE.add(var1x)) {
                                LOGGER.warn(MARKER, "Unable to play empty soundEvent: {}", var1x);
                            }

                        } else {
                            float var3x = param0.getVolume();
                            float var4x = Math.max(var3x, 1.0F) * (float)var2x.getAttenuationDistance();
                            SoundSource var5x = param0.getSource();
                            float var6x = this.calculateVolume(var3x, var5x);
                            float var7x = this.calculatePitch(param0);
                            SoundInstance.Attenuation var8x = param0.getAttenuation();
                            boolean var9x = param0.isRelative();
                            if (var6x == 0.0F && !param0.canStartSilent()) {
                                LOGGER.debug(MARKER, "Skipped playing sound {}, volume was zero.", var2x.getLocation());
                            } else {
                                Vec3 var10x = new Vec3(param0.getX(), param0.getY(), param0.getZ());
                                if (!this.listeners.isEmpty()) {
                                    boolean var11x = var9x
                                        || var8x == SoundInstance.Attenuation.NONE
                                        || this.listener.getListenerPosition().distanceToSqr(var10x) < (double)(var4x * var4x);
                                    if (var11x) {
                                        for(SoundEventListener var12x : this.listeners) {
                                            var12x.onPlaySound(param0, var0x);
                                        }
                                    } else {
                                        LOGGER.debug(MARKER, "Did not notify listeners of soundEvent: {}, it is too far away to hear", var1x);
                                    }
                                }

                                if (this.listener.getGain() <= 0.0F) {
                                    LOGGER.debug(MARKER, "Skipped playing soundEvent: {}, master volume was zero", var1x);
                                } else {
                                    boolean var13x = shouldLoopAutomatically(param0);
                                    boolean var14 = var2x.shouldStream();
                                    CompletableFuture<ChannelAccess.ChannelHandle> var15 = this.channelAccess
                                        .createHandle(var2x.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC);
                                    ChannelAccess.ChannelHandle var16 = var15.join();
                                    if (var16 == null) {
                                        if (SharedConstants.IS_RUNNING_IN_IDE) {
                                            LOGGER.warn("Failed to create new sound handle");
                                        }

                                    } else {
                                        LOGGER.debug(MARKER, "Playing sound {} for event {}", var2x.getLocation(), var1x);
                                        this.soundDeleteTime.put(param0, this.tickCount + 20);
                                        this.instanceToChannel.put(param0, var16);
                                        this.instanceBySource.put(var5x, param0);
                                        var16.execute(param8 -> {
                                            param8.setPitch(var7x);
                                            param8.setVolume(var6x);
                                            if (var8x == SoundInstance.Attenuation.LINEAR) {
                                                param8.linearAttenuation(var4x);
                                            } else {
                                                param8.disableAttenuation();
                                            }

                                            param8.setLooping(var13x && !var14);
                                            param8.setSelfPosition(var10x);
                                            param8.setRelative(var9x);
                                        });
                                        if (!var14) {
                                            this.soundBuffers.getCompleteBuffer(var2x.getPath()).thenAccept(param1 -> var16.execute(param1x -> {
                                                    param1x.attachStaticBuffer(param1);
                                                    param1x.play();
                                                }));
                                        } else {
                                            this.soundBuffers.getStream(var2x.getPath(), var13x).thenAccept(param1 -> var16.execute(param1x -> {
                                                    param1x.attachBufferStream(param1);
                                                    param1x.play();
                                                }));
                                        }

                                        if (param0 instanceof TickableSoundInstance) {
                                            this.tickingSounds.add((TickableSoundInstance)param0);
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void queueTickingSound(TickableSoundInstance param0) {
        this.queuedTickableSounds.add(param0);
    }

    public void requestPreload(Sound param0) {
        this.preloadQueue.add(param0);
    }

    private float calculatePitch(SoundInstance param0) {
        return Mth.clamp(param0.getPitch(), 0.5F, 2.0F);
    }

    private float calculateVolume(SoundInstance param0) {
        return this.calculateVolume(param0.getVolume(), param0.getSource());
    }

    private float calculateVolume(float param0, SoundSource param1) {
        return Mth.clamp(param0 * this.getVolume(param1), 0.0F, 1.0F);
    }

    public void pause() {
        if (this.loaded) {
            this.channelAccess.executeOnChannels(param0 -> param0.forEach(Channel::pause));
        }

    }

    public void resume() {
        if (this.loaded) {
            this.channelAccess.executeOnChannels(param0 -> param0.forEach(Channel::unpause));
        }

    }

    public void playDelayed(SoundInstance param0, int param1) {
        this.queuedSounds.put(param0, this.tickCount + param1);
    }

    public void updateSource(Camera param0) {
        if (this.loaded && param0.isInitialized()) {
            Vec3 var0 = param0.getPosition();
            Vector3f var1 = param0.getLookVector();
            Vector3f var2 = param0.getUpVector();
            this.executor.execute(() -> {
                this.listener.setListenerPosition(var0);
                this.listener.setListenerOrientation(var1, var2);
            });
        }
    }

    public void stop(@Nullable ResourceLocation param0, @Nullable SoundSource param1) {
        if (param1 != null) {
            for(SoundInstance var0 : this.instanceBySource.get(param1)) {
                if (param0 == null || var0.getLocation().equals(param0)) {
                    this.stop(var0);
                }
            }
        } else if (param0 == null) {
            this.stopAll();
        } else {
            for(SoundInstance var1 : this.instanceToChannel.keySet()) {
                if (var1.getLocation().equals(param0)) {
                    this.stop(var1);
                }
            }
        }

    }

    public String getDebugString() {
        return this.library.getDebugString();
    }

    public List<String> getAvailableSoundDevices() {
        return this.library.getAvailableSoundDevices();
    }

    @OnlyIn(Dist.CLIENT)
    static enum DeviceCheckState {
        ONGOING,
        CHANGE_DETECTED,
        NO_CHANGE;
    }
}
