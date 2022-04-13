package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class AttachedToLeavesDecorator extends TreeDecorator {
    public static final Codec<AttachedToLeavesDecorator> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(param0x -> param0x.probability),
                    Codec.intRange(0, 16).fieldOf("exclusion_radius_xz").forGetter(param0x -> param0x.exclusionRadiusXZ),
                    Codec.intRange(0, 16).fieldOf("exclusion_radius_y").forGetter(param0x -> param0x.exclusionRadiusY),
                    BlockStateProvider.CODEC.fieldOf("block_provider").forGetter(param0x -> param0x.blockProvider),
                    Codec.intRange(1, 16).fieldOf("required_empty_blocks").forGetter(param0x -> param0x.requiredEmptyBlocks),
                    ExtraCodecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions").forGetter(param0x -> param0x.directions)
                )
                .apply(param0, AttachedToLeavesDecorator::new)
    );
    protected final float probability;
    protected final int exclusionRadiusXZ;
    protected final int exclusionRadiusY;
    protected final BlockStateProvider blockProvider;
    protected final int requiredEmptyBlocks;
    protected final List<Direction> directions;

    public AttachedToLeavesDecorator(float param0, int param1, int param2, BlockStateProvider param3, int param4, List<Direction> param5) {
        this.probability = param0;
        this.exclusionRadiusXZ = param1;
        this.exclusionRadiusY = param2;
        this.blockProvider = param3;
        this.requiredEmptyBlocks = param4;
        this.directions = param5;
    }

    @Override
    public void place(TreeDecorator.Context param0) {
        Set<BlockPos> var0 = new HashSet<>();
        RandomSource var1 = param0.random();

        for(BlockPos var2 : Util.shuffledCopy(param0.leaves(), var1)) {
            Direction var3 = Util.getRandom(this.directions, var1);
            BlockPos var4 = var2.relative(var3);
            if (!var0.contains(var4) && var1.nextFloat() < this.probability && this.hasRequiredEmptyBlocks(param0, var2, var3)) {
                BlockPos var5 = var4.offset(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
                BlockPos var6 = var4.offset(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);

                for(BlockPos var7 : BlockPos.betweenClosed(var5, var6)) {
                    var0.add(var7.immutable());
                }

                param0.setBlock(var4, this.blockProvider.getState(var1, var4));
            }
        }

    }

    private boolean hasRequiredEmptyBlocks(TreeDecorator.Context param0, BlockPos param1, Direction param2) {
        for(int var0 = 1; var0 <= this.requiredEmptyBlocks; ++var0) {
            BlockPos var1 = param1.relative(param2, var0);
            if (!param0.isAir(var1)) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.ATTACHED_TO_LEAVES;
    }
}
