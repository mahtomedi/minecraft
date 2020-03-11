package net.minecraft.world.level;

import com.mojang.datafixers.Dynamic;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LevelType {
    public static final LevelType[] LEVEL_TYPES = new LevelType[16];
    private static final Dynamic<?> EMPTY_SETTINGS = new Dynamic<>(NbtOps.INSTANCE, new CompoundTag());
    public static final LevelType NORMAL = new LevelType(0, "default", 1, ChunkGeneratorProvider::createNormal).setHasReplacement();
    public static final LevelType FLAT = new LevelType(1, "flat", ChunkGeneratorProvider::createFlat).setCustomOptions(true);
    public static final LevelType LARGE_BIOMES = new LevelType(2, "largeBiomes", ChunkGeneratorProvider::createNormal);
    public static final LevelType AMPLIFIED = new LevelType(3, "amplified", ChunkGeneratorProvider::createNormal).setHasHelpText();
    public static final LevelType CUSTOMIZED = new LevelType(4, "customized", "normal", 0, ChunkGeneratorProvider::createNormal)
        .setCustomOptions(true)
        .setSelectableByUser(false);
    public static final LevelType BUFFET = new LevelType(5, "buffet", ChunkGeneratorProvider::createBuffet).setCustomOptions(true);
    public static final LevelType DEBUG_ALL_BLOCK_STATES = new LevelType(6, "debug_all_block_states", ChunkGeneratorProvider::createDebug);
    public static final LevelType NORMAL_1_1 = new LevelType(8, "default_1_1", 0, ChunkGeneratorProvider::createNormal).setSelectableByUser(false);
    private final int id;
    private final String generatorName;
    private final String generatorSerialization;
    private final int version;
    private final Function<Dynamic<?>, ChunkGeneratorProvider> provider;
    private final LazyLoadedValue<ChunkGeneratorProvider> defaultProvider;
    private boolean selectable;
    private boolean replacement;
    private boolean hasHelpText;
    private boolean hasCustomOptions;

    private LevelType(int param0, String param1, BiFunction<LevelType, Dynamic<?>, ChunkGeneratorProvider> param2) {
        this(param0, param1, param1, 0, param2);
    }

    private LevelType(int param0, String param1, int param2, BiFunction<LevelType, Dynamic<?>, ChunkGeneratorProvider> param3) {
        this(param0, param1, param1, param2, param3);
    }

    private LevelType(int param0, String param1, String param2, int param3, BiFunction<LevelType, Dynamic<?>, ChunkGeneratorProvider> param4) {
        this.generatorName = param1;
        this.generatorSerialization = param2;
        this.version = param3;
        this.provider = param1x -> param4.apply(this, param1x);
        this.defaultProvider = new LazyLoadedValue<>(() -> param4.apply(this, EMPTY_SETTINGS));
        this.selectable = true;
        this.id = param0;
        LEVEL_TYPES[param0] = this;
    }

    public String getName() {
        return this.generatorName;
    }

    public String getSerialization() {
        return this.generatorSerialization;
    }

    @OnlyIn(Dist.CLIENT)
    public String getDescriptionId() {
        return "generator." + this.generatorName;
    }

    @OnlyIn(Dist.CLIENT)
    public String getHelpTextId() {
        return this.getDescriptionId() + ".info";
    }

    public int getVersion() {
        return this.version;
    }

    public LevelType getReplacementForVersion(int param0) {
        return this == NORMAL && param0 == 0 ? NORMAL_1_1 : this;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasCustomOptions() {
        return this.hasCustomOptions;
    }

    public LevelType setCustomOptions(boolean param0) {
        this.hasCustomOptions = param0;
        return this;
    }

    private LevelType setSelectableByUser(boolean param0) {
        this.selectable = param0;
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isSelectable() {
        return this.selectable;
    }

    private LevelType setHasReplacement() {
        this.replacement = true;
        return this;
    }

    public boolean hasReplacement() {
        return this.replacement;
    }

    @Nullable
    public static LevelType getLevelType(String param0) {
        for(LevelType var0 : LEVEL_TYPES) {
            if (var0 != null && var0.generatorName.equalsIgnoreCase(param0)) {
                return var0;
            }
        }

        return null;
    }

    public int getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasHelpText() {
        return this.hasHelpText;
    }

    private LevelType setHasHelpText() {
        this.hasHelpText = true;
        return this;
    }

    public ChunkGeneratorProvider createProvider(Dynamic<?> param0) {
        return this.provider.apply(param0);
    }

    public ChunkGeneratorProvider getDefaultProvider() {
        return this.defaultProvider.get();
    }
}
