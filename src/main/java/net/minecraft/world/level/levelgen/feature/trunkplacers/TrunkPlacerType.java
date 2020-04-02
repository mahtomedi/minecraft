package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class TrunkPlacerType<P extends TrunkPlacer> {
    public static final TrunkPlacerType<StraightTrunkPlacer> STRAIGHT_TRUNK_PLACER = register("straight_trunk_placer", StraightTrunkPlacer::new);
    public static final TrunkPlacerType<ForkingTrunkPlacer> FORKING_TRUNK_PLACER = register("forking_trunk_placer", ForkingTrunkPlacer::new);
    private final Function<Dynamic<?>, P> deserializer;

    private static <P extends TrunkPlacer> TrunkPlacerType<P> register(String param0, Function<Dynamic<?>, P> param1) {
        return Registry.register(Registry.TRUNK_PLACER_TYPES, param0, new TrunkPlacerType<>(param1));
    }

    private TrunkPlacerType(Function<Dynamic<?>, P> param0) {
        this.deserializer = param0;
    }

    public P deserialize(Dynamic<?> param0) {
        return this.deserializer.apply(param0);
    }
}
