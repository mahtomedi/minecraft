package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.resources.sounds.SoundEventRegistrationSerializer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.MultipliedFloats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SoundManager extends SimplePreparableReloadListener<SoundManager.Preparations> {
    public static final Sound EMPTY_SOUND = new Sound(
        "meta:missing_sound", ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), 1, Sound.Type.FILE, false, false, 16
    );
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String SOUNDS_PATH = "sounds.json";
    private static final Gson GSON = new GsonBuilder()
        .registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
        .registerTypeAdapter(SoundEventRegistration.class, new SoundEventRegistrationSerializer())
        .create();
    private static final TypeToken<Map<String, SoundEventRegistration>> SOUND_EVENT_REGISTRATION_TYPE = new TypeToken<Map<String, SoundEventRegistration>>() {
    };
    private final Map<ResourceLocation, WeighedSoundEvents> registry = Maps.newHashMap();
    private final SoundEngine soundEngine;
    private final Map<ResourceLocation, Resource> soundCache = new HashMap<>();

    public SoundManager(Options param0) {
        this.soundEngine = new SoundEngine(this, param0, ResourceProvider.fromMap(this.soundCache));
    }

    protected SoundManager.Preparations prepare(ResourceManager param0, ProfilerFiller param1) {
        SoundManager.Preparations var0 = new SoundManager.Preparations();
        param1.startTick();
        param1.push("list");
        var0.listResources(param0);
        param1.pop();

        for(String var1 : param0.getNamespaces()) {
            param1.push(var1);

            try {
                for(Resource var3 : param0.getResourceStack(new ResourceLocation(var1, "sounds.json"))) {
                    param1.push(var3.sourcePackId());

                    try (Reader var4 = var3.openAsReader()) {
                        param1.push("parse");
                        Map<String, SoundEventRegistration> var5 = GsonHelper.fromJson(GSON, var4, SOUND_EVENT_REGISTRATION_TYPE);
                        param1.popPush("register");

                        for(Entry<String, SoundEventRegistration> var6 : var5.entrySet()) {
                            var0.handleRegistration(new ResourceLocation(var1, var6.getKey()), var6.getValue());
                        }

                        param1.pop();
                    } catch (RuntimeException var15) {
                        LOGGER.warn("Invalid {} in resourcepack: '{}'", "sounds.json", var3.sourcePackId(), var15);
                    }

                    param1.pop();
                }
            } catch (IOException var16) {
            }

            param1.pop();
        }

        param1.endTick();
        return var0;
    }

    protected void apply(SoundManager.Preparations param0, ResourceManager param1, ProfilerFiller param2) {
        param0.apply(this.registry, this.soundCache, this.soundEngine);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            for(ResourceLocation var0 : this.registry.keySet()) {
                WeighedSoundEvents var1 = this.registry.get(var0);
                if (!ComponentUtils.isTranslationResolvable(var1.getSubtitle()) && Registry.SOUND_EVENT.containsKey(var0)) {
                    LOGGER.error("Missing subtitle {} for sound event: {}", var1.getSubtitle(), var0);
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            for(ResourceLocation var2 : this.registry.keySet()) {
                if (!Registry.SOUND_EVENT.containsKey(var2)) {
                    LOGGER.debug("Not having sound event for: {}", var2);
                }
            }
        }

        this.soundEngine.reload();
    }

    public List<String> getAvailableSoundDevices() {
        return this.soundEngine.getAvailableSoundDevices();
    }

    static boolean validateSoundResource(Sound param0, ResourceLocation param1, ResourceProvider param2) {
        ResourceLocation var0 = param0.getPath();
        if (param2.getResource(var0).isEmpty()) {
            LOGGER.warn("File {} does not exist, cannot add it to event {}", var0, param1);
            return false;
        } else {
            return true;
        }
    }

    @Nullable
    public WeighedSoundEvents getSoundEvent(ResourceLocation param0) {
        return this.registry.get(param0);
    }

    public Collection<ResourceLocation> getAvailableSounds() {
        return this.registry.keySet();
    }

    public void queueTickingSound(TickableSoundInstance param0) {
        this.soundEngine.queueTickingSound(param0);
    }

    public void play(SoundInstance param0) {
        this.soundEngine.play(param0);
    }

    public void playDelayed(SoundInstance param0, int param1) {
        this.soundEngine.playDelayed(param0, param1);
    }

    public void updateSource(Camera param0) {
        this.soundEngine.updateSource(param0);
    }

    public void pause() {
        this.soundEngine.pause();
    }

    public void stop() {
        this.soundEngine.stopAll();
    }

    public void destroy() {
        this.soundEngine.destroy();
    }

    public void tick(boolean param0) {
        this.soundEngine.tick(param0);
    }

    public void resume() {
        this.soundEngine.resume();
    }

    public void updateSourceVolume(SoundSource param0, float param1) {
        if (param0 == SoundSource.MASTER && param1 <= 0.0F) {
            this.stop();
        }

        this.soundEngine.updateCategoryVolume(param0, param1);
    }

    public void stop(SoundInstance param0) {
        this.soundEngine.stop(param0);
    }

    public boolean isActive(SoundInstance param0) {
        return this.soundEngine.isActive(param0);
    }

    public void addListener(SoundEventListener param0) {
        this.soundEngine.addEventListener(param0);
    }

    public void removeListener(SoundEventListener param0) {
        this.soundEngine.removeEventListener(param0);
    }

    public void stop(@Nullable ResourceLocation param0, @Nullable SoundSource param1) {
        this.soundEngine.stop(param0, param1);
    }

    public String getDebugString() {
        return this.soundEngine.getDebugString();
    }

    public void reload() {
        this.soundEngine.reload();
    }

    @OnlyIn(Dist.CLIENT)
    protected static class Preparations {
        final Map<ResourceLocation, WeighedSoundEvents> registry = Maps.newHashMap();
        private Map<ResourceLocation, Resource> soundCache = Map.of();

        void listResources(ResourceManager param0) {
            this.soundCache = Sound.SOUND_LISTER.listMatchingResources(param0);
        }

        void handleRegistration(ResourceLocation param0, SoundEventRegistration param1) {
            WeighedSoundEvents var0 = this.registry.get(param0);
            boolean var1 = var0 == null;
            if (var1 || param1.isReplace()) {
                if (!var1) {
                    SoundManager.LOGGER.debug("Replaced sound event location {}", param0);
                }

                var0 = new WeighedSoundEvents(param0, param1.getSubtitle());
                this.registry.put(param0, var0);
            }

            ResourceProvider var2 = ResourceProvider.fromMap(this.soundCache);

            for(final Sound var3 : param1.getSounds()) {
                final ResourceLocation var4 = var3.getLocation();
                Weighted<Sound> var6;
                switch(var3.getType()) {
                    case FILE:
                        if (!SoundManager.validateSoundResource(var3, param0, var2)) {
                            continue;
                        }

                        var6 = var3;
                        break;
                    case SOUND_EVENT:
                        var6 = new Weighted<Sound>() {
                            @Override
                            public int getWeight() {
                                WeighedSoundEvents var0 = Preparations.this.registry.get(var4);
                                return var0 == null ? 0 : var0.getWeight();
                            }

                            public Sound getSound(RandomSource param0) {
                                WeighedSoundEvents var0 = Preparations.this.registry.get(var4);
                                if (var0 == null) {
                                    return SoundManager.EMPTY_SOUND;
                                } else {
                                    Sound var1 = var0.getSound(param0);
                                    return new Sound(
                                        var1.getLocation().toString(),
                                        new MultipliedFloats(var1.getVolume(), var3.getVolume()),
                                        new MultipliedFloats(var1.getPitch(), var3.getPitch()),
                                        var3.getWeight(),
                                        Sound.Type.FILE,
                                        var1.shouldStream() || var3.shouldStream(),
                                        var1.shouldPreload(),
                                        var1.getAttenuationDistance()
                                    );
                                }
                            }

                            @Override
                            public void preloadIfRequired(SoundEngine param0) {
                                WeighedSoundEvents var0 = Preparations.this.registry.get(var4);
                                if (var0 != null) {
                                    var0.preloadIfRequired(param0);
                                }
                            }
                        };
                        break;
                    default:
                        throw new IllegalStateException("Unknown SoundEventRegistration type: " + var3.getType());
                }

                var0.addSound(var6);
            }

        }

        public void apply(Map<ResourceLocation, WeighedSoundEvents> param0, Map<ResourceLocation, Resource> param1, SoundEngine param2) {
            param0.clear();
            param1.clear();
            param1.putAll(this.soundCache);

            for(Entry<ResourceLocation, WeighedSoundEvents> var0 : this.registry.entrySet()) {
                param0.put(var0.getKey(), var0.getValue());
                var0.getValue().preloadIfRequired(param2);
            }

        }
    }
}
