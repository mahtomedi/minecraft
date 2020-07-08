package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BeehiveDecorator extends TreeDecorator {
    public static final Codec<BeehiveDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
        .fieldOf("probability")
        .xmap(BeehiveDecorator::new, param0 -> param0.probability)
        .codec();
    private final float probability;

    public BeehiveDecorator(float param0) {
        this.probability = param0;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.BEEHIVE;
    }

    @Override
    public void place(WorldGenLevel param0, Random param1, List<BlockPos> param2, List<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5) {
        if (!(param1.nextFloat() >= this.probability)) {
            Direction var0 = BeehiveBlock.getRandomOffset(param1);
            int var1 = !param3.isEmpty()
                ? Math.max(param3.get(0).getY() - 1, param2.get(0).getY())
                : Math.min(param2.get(0).getY() + 1 + param1.nextInt(3), param2.get(param2.size() - 1).getY());
            List<BlockPos> var2 = param2.stream().filter(param1x -> param1x.getY() == var1).collect(Collectors.toList());
            if (!var2.isEmpty()) {
                BlockPos var3 = var2.get(param1.nextInt(var2.size()));
                BlockPos var4 = var3.relative(var0);
                if (Feature.isAir(param0, var4) && Feature.isAir(param0, var4.relative(Direction.SOUTH))) {
                    BlockState var5 = Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.SOUTH);
                    this.setBlock(param0, var4, var5, param4, param5);
                    BlockEntity var6 = param0.getBlockEntity(var4);
                    if (var6 instanceof BeehiveBlockEntity) {
                        BeehiveBlockEntity var7 = (BeehiveBlockEntity)var6;
                        int var8 = 2 + param1.nextInt(2);

                        for(int var9 = 0; var9 < var8; ++var9) {
                            Bee var10 = new Bee(EntityType.BEE, param0.getLevel());
                            var7.addOccupantWithPresetTicks(var10, false, param1.nextInt(599));
                        }
                    }

                }
            }
        }
    }
}
