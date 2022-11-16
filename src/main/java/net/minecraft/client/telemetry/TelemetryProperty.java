package net.minecraft.client.telemetry;

import com.mojang.authlib.minecraft.TelemetryPropertyContainer;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record TelemetryProperty<T>(String id, String exportKey, Codec<T> codec, TelemetryProperty.Exporter<T> exporter) {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
    public static final TelemetryProperty<String> USER_ID = string("user_id", "userId");
    public static final TelemetryProperty<String> CLIENT_ID = string("client_id", "clientId");
    public static final TelemetryProperty<UUID> MINECRAFT_SESSION_ID = uuid("minecraft_session_id", "deviceSessionId");
    public static final TelemetryProperty<String> GAME_VERSION = string("game_version", "buildDisplayName");
    public static final TelemetryProperty<String> OPERATING_SYSTEM = string("operating_system", "buildPlatform");
    public static final TelemetryProperty<String> PLATFORM = string("platform", "platform");
    public static final TelemetryProperty<Boolean> CLIENT_MODDED = bool("client_modded", "clientModded");
    public static final TelemetryProperty<UUID> WORLD_SESSION_ID = uuid("world_session_id", "worldSessionId");
    public static final TelemetryProperty<Boolean> SERVER_MODDED = bool("server_modded", "serverModded");
    public static final TelemetryProperty<TelemetryProperty.ServerType> SERVER_TYPE = create(
        "server_type", "serverType", TelemetryProperty.ServerType.CODEC, (param0, param1, param2) -> param0.addProperty(param1, param2.getSerializedName())
    );
    public static final TelemetryProperty<Boolean> OPT_IN = bool("opt_in", "isOptional");
    public static final TelemetryProperty<Instant> EVENT_TIMESTAMP_UTC = create(
        "event_timestamp_utc",
        "eventTimestampUtc",
        ExtraCodecs.INSTANT_ISO8601,
        (param0, param1, param2) -> param0.addProperty(param1, TIMESTAMP_FORMATTER.format(param2))
    );
    public static final TelemetryProperty<TelemetryProperty.GameMode> GAME_MODE = create(
        "game_mode", "playerGameMode", TelemetryProperty.GameMode.CODEC, (param0, param1, param2) -> param0.addProperty(param1, param2.id())
    );
    public static final TelemetryProperty<Integer> SECONDS_SINCE_LOAD = integer("seconds_since_load", "secondsSinceLoad");
    public static final TelemetryProperty<Integer> TICKS_SINCE_LOAD = integer("ticks_since_load", "ticksSinceLoad");
    public static final TelemetryProperty<LongList> FRAME_RATE_SAMPLES = longSamples("frame_rate_samples", "serializedFpsSamples");
    public static final TelemetryProperty<LongList> RENDER_TIME_SAMPLES = longSamples("render_time_samples", "serializedRenderTimeSamples");
    public static final TelemetryProperty<LongList> USED_MEMORY_SAMPLES = longSamples("used_memory_samples", "serializedUsedMemoryKbSamples");
    public static final TelemetryProperty<Integer> NUMBER_OF_SAMPLES = integer("number_of_samples", "numSamples");
    public static final TelemetryProperty<Integer> RENDER_DISTANCE = integer("render_distance", "renderDistance");
    public static final TelemetryProperty<Integer> DEDICATED_MEMORY_KB = integer("dedicated_memory_kb", "dedicatedMemoryKb");
    public static final TelemetryProperty<Integer> WORLD_LOAD_TIME_MS = integer("world_load_time_ms", "worldLoadTimeMs");
    public static final TelemetryProperty<Boolean> NEW_WORLD = bool("new_world", "newWorld");

    public static <T> TelemetryProperty<T> create(String param0, String param1, Codec<T> param2, TelemetryProperty.Exporter<T> param3) {
        return new TelemetryProperty<>(param0, param1, param2, param3);
    }

    public static TelemetryProperty<Boolean> bool(String param0, String param1) {
        return create(param0, param1, Codec.BOOL, TelemetryPropertyContainer::addProperty);
    }

    public static TelemetryProperty<String> string(String param0, String param1) {
        return create(param0, param1, Codec.STRING, TelemetryPropertyContainer::addProperty);
    }

    public static TelemetryProperty<Integer> integer(String param0, String param1) {
        return create(param0, param1, Codec.INT, TelemetryPropertyContainer::addProperty);
    }

    public static TelemetryProperty<UUID> uuid(String param0, String param1) {
        return create(param0, param1, UUIDUtil.STRING_CODEC, (param0x, param1x, param2) -> param0x.addProperty(param1x, param2.toString()));
    }

    public static TelemetryProperty<LongList> longSamples(String param0, String param1) {
        return create(
            param0,
            param1,
            Codec.LONG.listOf().xmap(LongArrayList::new, Function.identity()),
            (param0x, param1x, param2) -> param0x.addProperty(param1x, param2.longStream().mapToObj(String::valueOf).collect(Collectors.joining(";")))
        );
    }

    public void export(TelemetryPropertyMap param0, TelemetryPropertyContainer param1) {
        T var0 = param0.get(this);
        if (var0 != null) {
            this.exporter.apply(param1, this.exportKey, var0);
        } else {
            param1.addNullProperty(this.exportKey);
        }

    }

    public MutableComponent title() {
        return Component.translatable("telemetry.property." + this.id + ".title");
    }

    @Override
    public String toString() {
        return "TelemetryProperty[" + this.id + "]";
    }

    @OnlyIn(Dist.CLIENT)
    public interface Exporter<T> {
        void apply(TelemetryPropertyContainer var1, String var2, T var3);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum GameMode implements StringRepresentable {
        SURVIVAL("survival", 0),
        CREATIVE("creative", 1),
        ADVENTURE("adventure", 2),
        SPECTATOR("spectator", 6),
        HARDCORE("hardcore", 99);

        public static final Codec<TelemetryProperty.GameMode> CODEC = StringRepresentable.fromEnum(TelemetryProperty.GameMode::values);
        private final String key;
        private final int id;

        private GameMode(String param0, int param1) {
            this.key = param0;
            this.id = param1;
        }

        public int id() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.key;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum ServerType implements StringRepresentable {
        REALM("realm"),
        LOCAL("local"),
        OTHER("server");

        public static final Codec<TelemetryProperty.ServerType> CODEC = StringRepresentable.fromEnum(TelemetryProperty.ServerType::values);
        private final String key;

        private ServerType(String param0) {
            this.key = param0;
        }

        @Override
        public String getSerializedName() {
            return this.key;
        }
    }
}
