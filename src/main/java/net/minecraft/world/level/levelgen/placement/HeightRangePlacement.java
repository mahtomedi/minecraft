package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public class HeightRangePlacement extends PlacementModifier {
    public static final Codec<HeightRangePlacement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(HeightProvider.CODEC.fieldOf("height").forGetter(param0x -> param0x.height)).apply(param0, HeightRangePlacement::new)
    );
    private final HeightProvider height;

    private HeightRangePlacement(HeightProvider param0) {
        this.height = param0;
    }

    public static HeightRangePlacement of(HeightProvider param0) {
        return new HeightRangePlacement(param0);
    }

    public static HeightRangePlacement uniform(VerticalAnchor param0, VerticalAnchor param1) {
        return of(UniformHeight.of(param0, param1));
    }

    public static HeightRangePlacement triangle(VerticalAnchor param0, VerticalAnchor param1) {
        return of(TrapezoidHeight.of(param0, param1));
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext param0, RandomSource param1, BlockPos param2) {
        return Stream.of(param2.atY(this.height.sample(param1, param0)));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.HEIGHT_RANGE;
    }
}
