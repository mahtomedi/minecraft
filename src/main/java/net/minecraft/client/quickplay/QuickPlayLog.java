package net.minecraft.client.quickplay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class QuickPlayLog {
    private static final QuickPlayLog INACTIVE = new QuickPlayLog("") {
        @Override
        public void log(Minecraft param0) {
        }

        @Override
        public void setWorldData(QuickPlayLog.Type param0, String param1, String param2) {
        }
    };
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private final Path path;
    @Nullable
    private QuickPlayLog.QuickPlayWorld worldData;

    QuickPlayLog(String param0) {
        this.path = Minecraft.getInstance().gameDirectory.toPath().resolve(param0);
    }

    public static QuickPlayLog of(@Nullable String param0) {
        return param0 == null ? INACTIVE : new QuickPlayLog(param0);
    }

    public void setWorldData(QuickPlayLog.Type param0, String param1, String param2) {
        this.worldData = new QuickPlayLog.QuickPlayWorld(param0, param1, param2);
    }

    public void log(Minecraft param0) {
        if (param0.gameMode != null && this.worldData != null) {
            Util.ioPool()
                .execute(
                    () -> {
                        try {
                            Files.deleteIfExists(this.path);
                        } catch (IOException var3) {
                            LOGGER.error("Failed to delete quickplay log file {}", this.path, var3);
                        }
        
                        QuickPlayLog.QuickPlayEntry var1x = new QuickPlayLog.QuickPlayEntry(this.worldData, Instant.now(), param0.gameMode.getPlayerMode());
                        Codec.list(QuickPlayLog.QuickPlayEntry.CODEC)
                            .encodeStart(JsonOps.INSTANCE, List.of(var1x))
                            .resultOrPartial(Util.prefix("Quick Play: ", LOGGER::error))
                            .ifPresent(param0x -> {
                                try {
                                    Files.createDirectories(this.path.getParent());
                                    Files.writeString(this.path, GSON.toJson(param0x));
                                } catch (IOException var3x) {
                                    LOGGER.error("Failed to write to quickplay log file {}", this.path, var3x);
                                }
            
                            });
                    }
                );
        } else {
            LOGGER.error("Failed to log session for quickplay. Missing world data or gamemode");
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record QuickPlayEntry(QuickPlayLog.QuickPlayWorld quickPlayWorld, Instant lastPlayedTime, GameType gamemode) {
        public static final Codec<QuickPlayLog.QuickPlayEntry> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        QuickPlayLog.QuickPlayWorld.MAP_CODEC.forGetter(QuickPlayLog.QuickPlayEntry::quickPlayWorld),
                        ExtraCodecs.INSTANT_ISO8601.fieldOf("lastPlayedTime").forGetter(QuickPlayLog.QuickPlayEntry::lastPlayedTime),
                        GameType.CODEC.fieldOf("gamemode").forGetter(QuickPlayLog.QuickPlayEntry::gamemode)
                    )
                    .apply(param0, QuickPlayLog.QuickPlayEntry::new)
        );
    }

    @OnlyIn(Dist.CLIENT)
    static record QuickPlayWorld(QuickPlayLog.Type type, String id, String name) {
        public static final MapCodec<QuickPlayLog.QuickPlayWorld> MAP_CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        QuickPlayLog.Type.CODEC.fieldOf("type").forGetter(QuickPlayLog.QuickPlayWorld::type),
                        Codec.STRING.fieldOf("id").forGetter(QuickPlayLog.QuickPlayWorld::id),
                        Codec.STRING.fieldOf("name").forGetter(QuickPlayLog.QuickPlayWorld::name)
                    )
                    .apply(param0, QuickPlayLog.QuickPlayWorld::new)
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type implements StringRepresentable {
        SINGLEPLAYER("singleplayer"),
        MULTIPLAYER("multiplayer"),
        REALMS("realms");

        static final Codec<QuickPlayLog.Type> CODEC = StringRepresentable.fromEnum(QuickPlayLog.Type::values);
        private final String name;

        private Type(String param0) {
            this.name = param0;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
