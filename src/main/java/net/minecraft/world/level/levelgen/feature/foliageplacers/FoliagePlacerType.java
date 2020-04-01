package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class FoliagePlacerType<P extends FoliagePlacer> {
    public static final FoliagePlacerType<BlobFoliagePlacer> BLOB_FOLIAGE_PLACER = register(
        "blob_foliage_placer", BlobFoliagePlacer::new, BlobFoliagePlacer::random
    );
    public static final FoliagePlacerType<SpruceFoliagePlacer> SPRUCE_FOLIAGE_PLACER = register(
        "spruce_foliage_placer", SpruceFoliagePlacer::new, SpruceFoliagePlacer::random
    );
    public static final FoliagePlacerType<PineFoliagePlacer> PINE_FOLIAGE_PLACER = register(
        "pine_foliage_placer", PineFoliagePlacer::new, PineFoliagePlacer::random
    );
    public static final FoliagePlacerType<AcaciaFoliagePlacer> ACACIA_FOLIAGE_PLACER = register(
        "acacia_foliage_placer", AcaciaFoliagePlacer::new, AcaciaFoliagePlacer::random
    );
    private final Function<Dynamic<?>, P> deserializer;
    private final Function<Random, P> randomProvider;

    private static <P extends FoliagePlacer> FoliagePlacerType<P> register(String param0, Function<Dynamic<?>, P> param1, Function<Random, P> param2) {
        return Registry.register(Registry.FOLIAGE_PLACER_TYPES, param0, new FoliagePlacerType<>(param1, param2));
    }

    private FoliagePlacerType(Function<Dynamic<?>, P> param0, Function<Random, P> param1) {
        this.deserializer = param0;
        this.randomProvider = param1;
    }

    public P deserialize(Dynamic<?> param0) {
        return this.deserializer.apply(param0);
    }

    public P random(Random param0) {
        return this.randomProvider.apply(param0);
    }
}
