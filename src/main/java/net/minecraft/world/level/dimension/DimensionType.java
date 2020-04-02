package net.minecraft.world.level.dimension;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.io.File;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.dimension.end.TheEndDimension;

public class DimensionType implements Serializable {
    public static final DimensionType OVERWORLD = register(
        "overworld", new DimensionType(1, "", "", NormalDimension::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE)
    );
    public static final DimensionType NETHER = register(
        "the_nether", new DimensionType(0, "_nether", "DIM-1", NetherDimension::new, false, FuzzyOffsetBiomeZoomer.INSTANCE)
    );
    public static final DimensionType THE_END = register(
        "the_end", new DimensionType(2, "_end", "DIM1", TheEndDimension::new, false, FuzzyOffsetBiomeZoomer.INSTANCE)
    );
    private final int id;
    private final String fileSuffix;
    private final String folder;
    private final BiFunction<Level, DimensionType, ? extends Dimension> factory;
    private final boolean hasSkylight;
    private final BiomeZoomer biomeZoomer;

    private static DimensionType register(String param0, DimensionType param1) {
        return Registry.registerMapping(Registry.DIMENSION_TYPE, param1.id, param0, param1);
    }

    protected DimensionType(
        int param0, String param1, String param2, BiFunction<Level, DimensionType, ? extends Dimension> param3, boolean param4, BiomeZoomer param5
    ) {
        this.id = param0;
        this.fileSuffix = param1;
        this.folder = param2;
        this.factory = param3;
        this.hasSkylight = param4;
        this.biomeZoomer = param5;
    }

    public static DimensionType of(Dynamic<?> param0) {
        return Registry.DIMENSION_TYPE.get(new ResourceLocation(param0.asString("")));
    }

    public static Iterable<DimensionType> getAllTypes() {
        return Registry.DIMENSION_TYPE;
    }

    public int getId() {
        return this.id + -1;
    }

    public String getFileSuffix() {
        return this.fileSuffix;
    }

    public File getStorageFolder(File param0) {
        return this.folder.isEmpty() ? param0 : new File(param0, this.folder);
    }

    public Dimension create(Level param0) {
        return this.factory.apply(param0, this);
    }

    @Override
    public String toString() {
        return getName(this).toString();
    }

    @Nullable
    public static DimensionType getById(int param0) {
        return Registry.DIMENSION_TYPE.byId(param0 - -1);
    }

    @Nullable
    public static DimensionType getByName(ResourceLocation param0) {
        return Registry.DIMENSION_TYPE.get(param0);
    }

    @Nullable
    public static ResourceLocation getName(DimensionType param0) {
        return Registry.DIMENSION_TYPE.getKey(param0);
    }

    public boolean hasSkyLight() {
        return this.hasSkylight;
    }

    public BiomeZoomer getBiomeZoomer() {
        return this.biomeZoomer;
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return param0.createString(Registry.DIMENSION_TYPE.getKey(this).toString());
    }
}
