package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

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
    public void place(LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, Random param2, List<BlockPos> param3, List<BlockPos> param4) {
        if (!(param2.nextFloat() >= this.probability)) {
            Direction var0 = BeehiveBlock.getRandomOffset(param2);
            int var1 = !param4.isEmpty()
                ? Math.max(param4.get(0).getY() - 1, param3.get(0).getY())
                : Math.min(param3.get(0).getY() + 1 + param2.nextInt(3), param3.get(param3.size() - 1).getY());
            List<BlockPos> var2 = param3.stream().filter(param1x -> param1x.getY() == var1).collect(Collectors.toList());
            if (!var2.isEmpty()) {
                BlockPos var3 = var2.get(param2.nextInt(var2.size()));
                BlockPos var4 = var3.relative(var0);
                if (Feature.isAir(param0, var4) && Feature.isAir(param0, var4.relative(Direction.SOUTH))) {
                    param1.accept(var4, Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.SOUTH));
                    param0.getBlockEntity(var4, BlockEntityType.BEEHIVE).ifPresent(param1x -> {
                        int var0x = 2 + param2.nextInt(2);

                        for(int var1x = 0; var1x < var0x; ++var1x) {
                            CompoundTag var2x = new CompoundTag();
                            var2x.putString("id", Registry.ENTITY_TYPE.getKey(EntityType.BEE).toString());
                            param1x.storeBee(var2x, param2.nextInt(599), false);
                        }

                    });
                }
            }
        }
    }
}
