package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BeehiveDecorator extends TreeDecorator {
    public static final Codec<BeehiveDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
        .fieldOf("probability")
        .xmap(BeehiveDecorator::new, param0 -> param0.probability)
        .codec();
    private static final Direction WORLDGEN_FACING = Direction.SOUTH;
    private static final Direction[] SPAWN_DIRECTIONS = Direction.Plane.HORIZONTAL
        .stream()
        .filter(param0 -> param0 != WORLDGEN_FACING.getOpposite())
        .toArray(param0 -> new Direction[param0]);
    private final float probability;

    public BeehiveDecorator(float param0) {
        this.probability = param0;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.BEEHIVE;
    }

    @Override
    public void place(TreeDecorator.Context param0) {
        RandomSource var0 = param0.random();
        if (!(var0.nextFloat() >= this.probability)) {
            List<BlockPos> var1 = param0.leaves();
            List<BlockPos> var2 = param0.logs();
            int var3 = !var1.isEmpty()
                ? Math.max(var1.get(0).getY() - 1, var2.get(0).getY() + 1)
                : Math.min(var2.get(0).getY() + 1 + var0.nextInt(3), var2.get(var2.size() - 1).getY());
            List<BlockPos> var4 = var2.stream()
                .filter(param1 -> param1.getY() == var3)
                .flatMap(param0x -> Stream.of(SPAWN_DIRECTIONS).map(param0x::relative))
                .collect(Collectors.toList());
            if (!var4.isEmpty()) {
                Collections.shuffle(var4);
                Optional<BlockPos> var5 = var4.stream().filter(param1 -> param0.isAir(param1) && param0.isAir(param1.relative(WORLDGEN_FACING))).findFirst();
                if (!var5.isEmpty()) {
                    param0.setBlock(var5.get(), Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, WORLDGEN_FACING));
                    param0.level().getBlockEntity(var5.get(), BlockEntityType.BEEHIVE).ifPresent(param1 -> {
                        int var0x = 2 + var0.nextInt(2);

                        for(int var1x = 0; var1x < var0x; ++var1x) {
                            CompoundTag var2x = new CompoundTag();
                            var2x.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BEE).toString());
                            param1.storeBee(var2x, var0.nextInt(599), false);
                        }

                    });
                }
            }
        }
    }
}
