package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public class TrunkPlacerType<P extends TrunkPlacer> {
    public static final TrunkPlacerType<StraightTrunkPlacer> STRAIGHT_TRUNK_PLACER = register("straight_trunk_placer", StraightTrunkPlacer.CODEC);
    public static final TrunkPlacerType<ForkingTrunkPlacer> FORKING_TRUNK_PLACER = register("forking_trunk_placer", ForkingTrunkPlacer.CODEC);
    public static final TrunkPlacerType<GiantTrunkPlacer> GIANT_TRUNK_PLACER = register("giant_trunk_placer", GiantTrunkPlacer.CODEC);
    public static final TrunkPlacerType<MegaJungleTrunkPlacer> MEGA_JUNGLE_TRUNK_PLACER = register("mega_jungle_trunk_placer", MegaJungleTrunkPlacer.CODEC);
    public static final TrunkPlacerType<DarkOakTrunkPlacer> DARK_OAK_TRUNK_PLACER = register("dark_oak_trunk_placer", DarkOakTrunkPlacer.CODEC);
    public static final TrunkPlacerType<FancyTrunkPlacer> FANCY_TRUNK_PLACER = register("fancy_trunk_placer", FancyTrunkPlacer.CODEC);
    public static final TrunkPlacerType<BendingTrunkPlacer> BENDING_TRUNK_PLACER = register("bending_trunk_placer", BendingTrunkPlacer.CODEC);
    private final Codec<P> codec;

    private static <P extends TrunkPlacer> TrunkPlacerType<P> register(String param0, Codec<P> param1) {
        return Registry.register(Registry.TRUNK_PLACER_TYPES, param0, new TrunkPlacerType<>(param1));
    }

    private TrunkPlacerType(Codec<P> param0) {
        this.codec = param0;
    }

    public Codec<P> codec() {
        return this.codec;
    }
}
