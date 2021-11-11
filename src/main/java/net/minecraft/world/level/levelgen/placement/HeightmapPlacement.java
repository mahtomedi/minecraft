package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public class HeightmapPlacement extends PlacementModifier {
    public static final Codec<HeightmapPlacement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(param0x -> param0x.heightmap)).apply(param0, HeightmapPlacement::new)
    );
    private final Heightmap.Types heightmap;

    private HeightmapPlacement(Heightmap.Types param0) {
        this.heightmap = param0;
    }

    public static HeightmapPlacement onHeightmap(Heightmap.Types param0) {
        return new HeightmapPlacement(param0);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext param0, Random param1, BlockPos param2) {
        int var0 = param2.getX();
        int var1 = param2.getZ();
        int var2 = param0.getHeight(this.heightmap, var0, var1);
        return var2 > param0.getMinBuildHeight() ? Stream.of(new BlockPos(var0, var2, var1)) : Stream.of();
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.HEIGHTMAP;
    }
}
