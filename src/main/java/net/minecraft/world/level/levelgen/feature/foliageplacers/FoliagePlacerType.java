package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class FoliagePlacerType<P extends FoliagePlacer> {
    public static final FoliagePlacerType<BlobFoliagePlacer> BLOB_FOLIAGE_PLACER = register("blob_foliage_placer", BlobFoliagePlacer::new);
    public static final FoliagePlacerType<SpruceFoliagePlacer> SPRUCE_FOLIAGE_PLACER = register("spruce_foliage_placer", SpruceFoliagePlacer::new);
    public static final FoliagePlacerType<PineFoliagePlacer> PINE_FOLIAGE_PLACER = register("pine_foliage_placer", PineFoliagePlacer::new);
    public static final FoliagePlacerType<AcaciaFoliagePlacer> ACACIA_FOLIAGE_PLACER = register("acacia_foliage_placer", AcaciaFoliagePlacer::new);
    private final Function<Dynamic<?>, P> deserializer;

    private static <P extends FoliagePlacer> FoliagePlacerType<P> register(String param0, Function<Dynamic<?>, P> param1) {
        return Registry.register(Registry.FOLIAGE_PLACER_TYPES, param0, new FoliagePlacerType<>(param1));
    }

    private FoliagePlacerType(Function<Dynamic<?>, P> param0) {
        this.deserializer = param0;
    }

    public P deserialize(Dynamic<?> param0) {
        return this.deserializer.apply(param0);
    }
}
