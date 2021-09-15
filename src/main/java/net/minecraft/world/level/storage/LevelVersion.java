package net.minecraft.world.level.storage;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.SharedConstants;

public class LevelVersion {
    private final int levelDataVersion;
    private final long lastPlayed;
    private final String minecraftVersionName;
    private final DataVersion minecraftVersion;
    private final boolean snapshot;

    private LevelVersion(int param0, long param1, String param2, int param3, String param4, boolean param5) {
        this.levelDataVersion = param0;
        this.lastPlayed = param1;
        this.minecraftVersionName = param2;
        this.minecraftVersion = new DataVersion(param3, param4);
        this.snapshot = param5;
    }

    public static LevelVersion parse(Dynamic<?> param0) {
        int var0 = param0.get("version").asInt(0);
        long var1 = param0.get("LastPlayed").asLong(0L);
        OptionalDynamic<?> var2 = param0.get("Version");
        return var2.result().isPresent()
            ? new LevelVersion(
                var0,
                var1,
                var2.get("Name").asString(SharedConstants.getCurrentVersion().getName()),
                var2.get("Id").asInt(SharedConstants.getCurrentVersion().getDataVersion().getVersion()),
                var2.get("Series").asString(DataVersion.MAIN_SERIES),
                var2.get("Snapshot").asBoolean(!SharedConstants.getCurrentVersion().isStable())
            )
            : new LevelVersion(var0, var1, "", 0, DataVersion.MAIN_SERIES, false);
    }

    public int levelDataVersion() {
        return this.levelDataVersion;
    }

    public long lastPlayed() {
        return this.lastPlayed;
    }

    public String minecraftVersionName() {
        return this.minecraftVersionName;
    }

    public DataVersion minecraftVersion() {
        return this.minecraftVersion;
    }

    public boolean snapshot() {
        return this.snapshot;
    }
}
