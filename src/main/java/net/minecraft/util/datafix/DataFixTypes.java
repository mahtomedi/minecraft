package net.minecraft.util.datafix;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
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
    SAVED_DATA_COMMAND_STORAGE(References.SAVED_DATA_COMMAND_STORAGE),
    SAVED_DATA_FORCED_CHUNKS(References.SAVED_DATA_FORCED_CHUNKS),
    SAVED_DATA_MAP_DATA(References.SAVED_DATA_MAP_DATA),
    SAVED_DATA_MAP_INDEX(References.SAVED_DATA_MAP_INDEX),
    SAVED_DATA_RAIDS(References.SAVED_DATA_RAIDS),
    SAVED_DATA_RANDOM_SEQUENCES(References.SAVED_DATA_RANDOM_SEQUENCES),
    SAVED_DATA_SCOREBOARD(References.SAVED_DATA_SCOREBOARD),
    SAVED_DATA_STRUCTURE_FEATURE_INDICES(References.SAVED_DATA_STRUCTURE_FEATURE_INDICES),
    ADVANCEMENTS(References.ADVANCEMENTS),
    POI_CHUNK(References.POI_CHUNK),
    WORLD_GEN_SETTINGS(References.WORLD_GEN_SETTINGS),
    ENTITY_CHUNK(References.ENTITY_CHUNK);

    public static final Set<TypeReference> TYPES_FOR_LEVEL_LIST;
    private final TypeReference type;

    private DataFixTypes(TypeReference param0) {
        this.type = param0;
    }

    static int currentVersion() {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    public <A> Codec<A> wrapCodec(final Codec<A> param0, final DataFixer param1, final int param2) {
        return new Codec<A>() {
            @Override
            public <T> DataResult<T> encode(A param0x, DynamicOps<T> param1x, T param2x) {
                return param0.encode(param0, param1, param2)
                    .flatMap(param1xxx -> param1.mergeToMap(param1xxx, param1.createString("DataVersion"), param1.createInt(DataFixTypes.currentVersion())));
            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> param0x, T param1x) {
                int var0 = param0.get(param1, "DataVersion").flatMap(param0::getNumberValue).map(Number::intValue).result().orElse(param2);
                Dynamic<T> var1 = new Dynamic<>(param0, param0.remove(param1, "DataVersion"));
                Dynamic<T> var2 = DataFixTypes.this.updateToCurrentVersion(param1, var1, var0);
                return param0.decode(var2);
            }
        };
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
