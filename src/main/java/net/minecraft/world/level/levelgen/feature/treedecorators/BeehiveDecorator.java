package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BeehiveDecorator extends TreeDecorator {
    private final float probability;

    public BeehiveDecorator(float param0) {
        super(TreeDecoratorType.BEEHIVE);
        this.probability = param0;
    }

    public <T> BeehiveDecorator(Dynamic<T> param0) {
        this(param0.get("probability").asFloat(0.0F));
    }

    @Override
    public void place(LevelAccessor param0, Random param1, List<BlockPos> param2, List<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5) {
        if (!(param1.nextFloat() >= this.probability)) {
            Direction var0 = BeehiveBlock.SPAWN_DIRECTIONS[param1.nextInt(BeehiveBlock.SPAWN_DIRECTIONS.length)];
            int var1 = !param3.isEmpty()
                ? Math.max(param3.get(0).getY() - 1, param2.get(0).getY())
                : Math.min(param2.get(0).getY() + 1 + param1.nextInt(3), param2.get(param2.size() - 1).getY());
            List<BlockPos> var2 = param2.stream().filter(param1x -> param1x.getY() == var1).collect(Collectors.toList());
            BlockPos var3 = var2.get(param1.nextInt(var2.size()));
            BlockPos var4 = var3.relative(var0);
            if (AbstractTreeFeature.isAir(param0, var4) && AbstractTreeFeature.isAir(param0, var4.relative(Direction.SOUTH))) {
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

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
                param0,
                param0.createMap(
                    ImmutableMap.of(
                        param0.createString("type"),
                        param0.createString(Registry.TREE_DECORATOR_TYPES.getKey(this.type).toString()),
                        param0.createString("probability"),
                        param0.createFloat(this.probability)
                    )
                )
            )
            .getValue();
    }
}
