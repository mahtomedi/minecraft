package net.minecraft.client.telemetry;

import com.mojang.authlib.minecraft.TelemetryEvent;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TelemetryEventType {
    static final Map<String, TelemetryEventType> REGISTRY = new Object2ObjectLinkedOpenHashMap<>();
    public static final Codec<TelemetryEventType> CODEC = Codec.STRING.comapFlatMap(param0 -> {
        TelemetryEventType var0 = REGISTRY.get(param0);
        return var0 != null ? DataResult.success(var0) : DataResult.error(() -> "No TelemetryEventType with key: '" + param0 + "'");
    }, TelemetryEventType::id);
    private static final List<TelemetryProperty<?>> GLOBAL_PROPERTIES = List.of(
        TelemetryProperty.USER_ID,
        TelemetryProperty.CLIENT_ID,
        TelemetryProperty.MINECRAFT_SESSION_ID,
        TelemetryProperty.GAME_VERSION,
        TelemetryProperty.OPERATING_SYSTEM,
        TelemetryProperty.PLATFORM,
        TelemetryProperty.CLIENT_MODDED,
        TelemetryProperty.EVENT_TIMESTAMP_UTC,
        TelemetryProperty.OPT_IN
    );
    private static final List<TelemetryProperty<?>> WORLD_SESSION_PROPERTIES = Stream.concat(
            GLOBAL_PROPERTIES.stream(), Stream.of(TelemetryProperty.WORLD_SESSION_ID, TelemetryProperty.SERVER_MODDED, TelemetryProperty.SERVER_TYPE)
        )
        .toList();
    public static final TelemetryEventType WORLD_LOADED = builder("world_loaded", "WorldLoaded")
        .defineAll(WORLD_SESSION_PROPERTIES)
        .define(TelemetryProperty.GAME_MODE)
        .register();
    public static final TelemetryEventType PERFORMANCE_METRICS = builder("performance_metrics", "PerformanceMetrics")
        .defineAll(WORLD_SESSION_PROPERTIES)
        .define(TelemetryProperty.FRAME_RATE_SAMPLES)
        .define(TelemetryProperty.RENDER_TIME_SAMPLES)
        .define(TelemetryProperty.USED_MEMORY_SAMPLES)
        .define(TelemetryProperty.NUMBER_OF_SAMPLES)
        .define(TelemetryProperty.RENDER_DISTANCE)
        .define(TelemetryProperty.DEDICATED_MEMORY_KB)
        .optIn()
        .register();
    public static final TelemetryEventType WORLD_LOAD_TIMES = builder("world_load_times", "WorldLoadTimes")
        .defineAll(WORLD_SESSION_PROPERTIES)
        .define(TelemetryProperty.WORLD_LOAD_TIME_MS)
        .define(TelemetryProperty.NEW_WORLD)
        .optIn()
        .register();
    public static final TelemetryEventType WORLD_UNLOADED = builder("world_unloaded", "WorldUnloaded")
        .defineAll(WORLD_SESSION_PROPERTIES)
        .define(TelemetryProperty.SECONDS_SINCE_LOAD)
        .define(TelemetryProperty.TICKS_SINCE_LOAD)
        .register();
    private final String id;
    private final String exportKey;
    private final List<TelemetryProperty<?>> properties;
    private final boolean isOptIn;
    private final Codec<TelemetryEventInstance> codec;

    TelemetryEventType(String param0, String param1, List<TelemetryProperty<?>> param2, boolean param3) {
        this.id = param0;
        this.exportKey = param1;
        this.properties = param2;
        this.isOptIn = param3;
        this.codec = TelemetryPropertyMap.createCodec(param2).xmap(param0x -> new TelemetryEventInstance(this, param0x), TelemetryEventInstance::properties);
    }

    public static TelemetryEventType.Builder builder(String param0, String param1) {
        return new TelemetryEventType.Builder(param0, param1);
    }

    public String id() {
        return this.id;
    }

    public List<TelemetryProperty<?>> properties() {
        return this.properties;
    }

    public Codec<TelemetryEventInstance> codec() {
        return this.codec;
    }

    public boolean isOptIn() {
        return this.isOptIn;
    }

    public TelemetryEvent export(TelemetrySession param0, TelemetryPropertyMap param1) {
        TelemetryEvent var0 = param0.createNewEvent(this.exportKey);

        for(TelemetryProperty<?> var1 : this.properties) {
            var1.export(param1, var0);
        }

        return var0;
    }

    public <T> boolean contains(TelemetryProperty<T> param0) {
        return this.properties.contains(param0);
    }

    @Override
    public String toString() {
        return "TelemetryEventType[" + this.id + "]";
    }

    public MutableComponent title() {
        return this.makeTranslation("title");
    }

    public MutableComponent description() {
        return this.makeTranslation("description");
    }

    private MutableComponent makeTranslation(String param0) {
        return Component.translatable("telemetry.event." + this.id + "." + param0);
    }

    public static List<TelemetryEventType> values() {
        return List.copyOf(REGISTRY.values());
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final String id;
        private final String exportKey;
        private final List<TelemetryProperty<?>> properties = new ArrayList();
        private boolean isOptIn;

        Builder(String param0, String param1) {
            this.id = param0;
            this.exportKey = param1;
        }

        public TelemetryEventType.Builder defineAll(List<TelemetryProperty<?>> param0) {
            this.properties.addAll(param0);
            return this;
        }

        public <T> TelemetryEventType.Builder define(TelemetryProperty<T> param0) {
            this.properties.add(param0);
            return this;
        }

        public TelemetryEventType.Builder optIn() {
            this.isOptIn = true;
            return this;
        }

        public TelemetryEventType register() {
            TelemetryEventType var0 = new TelemetryEventType(this.id, this.exportKey, List.copyOf(this.properties), this.isOptIn);
            if (TelemetryEventType.REGISTRY.putIfAbsent(this.id, var0) != null) {
                throw new IllegalStateException("Duplicate TelemetryEventType with key: '" + this.id + "'");
            } else {
                return var0;
            }
        }
    }
}
