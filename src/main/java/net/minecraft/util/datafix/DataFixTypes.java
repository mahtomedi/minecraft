package net.minecraft.util.datafix;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.fixes.References;

public enum DataFixTypes {
    LEVEL(References.LEVEL),
    PLAYER(References.PLAYER),
    CHUNK(References.CHUNK),
    HOTBAR(References.HOTBAR),
    OPTIONS(References.OPTIONS),
    STRUCTURE(References.STRUCTURE),
    STATS(References.STATS),
    SAVED_DATA(References.SAVED_DATA),
    ADVANCEMENTS(References.ADVANCEMENTS),
    POI_CHUNK(References.POI_CHUNK),
    WORLD_GEN_SETTINGS(References.WORLD_GEN_SETTINGS),
    ENTITY_CHUNK(References.ENTITY_CHUNK);

    public static final Set<TypeReference> TYPES_FOR_LEVEL_LIST;
    private final TypeReference type;

    private DataFixTypes(TypeReference param0) {
        this.type = param0;
    }

    private static int currentVersion() {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    public <T> Dynamic<T> update(DataFixer param0, Dynamic<T> param1, int param2, int param3) {
        return param0.update(this.type, param1, param2, param3);
    }

    public <T> Dynamic<T> updateToCurrentVersion(DataFixer param0, Dynamic<T> param1, int param2) {
        return this.update(param0, param1, param2, currentVersion());
    }

    public CompoundTag update(DataFixer param0, CompoundTag param1, int param2, int param3) {
        return this.update(param0, new Dynamic<>(NbtOps.INSTANCE, param1), param2, param3).getValue();
    }

    public CompoundTag updateToCurrentVersion(DataFixer param0, CompoundTag param1, int param2) {
        return this.update(param0, param1, param2, currentVersion());
    }

    static {
        TYPES_FOR_LEVEL_LIST = Set.of(LEVEL.type);
    }
}
