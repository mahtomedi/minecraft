package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public record RandomSpreadStructurePlacement(int spacing, int separation, RandomSpreadType spreadType, int salt, Vec3i locateOffset)
    implements StructurePlacement {
    public static final Codec<RandomSpreadStructurePlacement> CODEC = RecordCodecBuilder.<RandomSpreadStructurePlacement>mapCodec(
            param0 -> param0.group(
                        Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing),
                        Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation),
                        RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(RandomSpreadStructurePlacement::salt),
                        Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(RandomSpreadStructurePlacement::locateOffset)
                    )
                    .apply(param0, RandomSpreadStructurePlacement::new)
        )
        .flatXmap(
            param0 -> param0.spacing <= param0.separation ? DataResult.error("Spacing has to be larger than separation") : DataResult.success(param0),
            DataResult::success
        )
        .codec();

    public RandomSpreadStructurePlacement(int param0, int param1, RandomSpreadType param2, int param3) {
        this(param0, param1, param2, param3, Vec3i.ZERO);
    }

    public ChunkPos getPotentialFeatureChunk(long param0, int param1, int param2) {
        int var0 = this.spacing();
        int var1 = this.separation();
        int var2 = Math.floorDiv(param1, var0);
        int var3 = Math.floorDiv(param2, var0);
        WorldgenRandom var4 = new WorldgenRandom(new LegacyRandomSource(0L));
        var4.setLargeFeatureWithSalt(param0, var2, var3, this.salt());
        int var5 = var0 - var1;
        int var6 = this.spreadType().evaluate(var4, var5);
        int var7 = this.spreadType().evaluate(var4, var5);
        return new ChunkPos(var2 * var0 + var6, var3 * var0 + var7);
    }

    @Override
    public boolean isFeatureChunk(ChunkGenerator param0, long param1, int param2, int param3) {
        ChunkPos var0 = this.getPotentialFeatureChunk(param1, param2, param3);
        return var0.x == param2 && var0.z == param3;
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructurePlacementType.RANDOM_SPREAD;
    }
}
