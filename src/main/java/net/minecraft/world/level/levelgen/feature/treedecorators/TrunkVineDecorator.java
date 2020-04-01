package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class TrunkVineDecorator extends TreeDecorator {
    public TrunkVineDecorator() {
        super(TreeDecoratorType.TRUNK_VINE);
    }

    public <T> TrunkVineDecorator(Dynamic<T> param0) {
        this();
    }

    @Override
    public void place(LevelAccessor param0, Random param1, List<BlockPos> param2, List<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5) {
        param2.forEach(param4x -> {
            if (param1.nextInt(3) > 0) {
                BlockPos var3x = param4x.west();
                if (AbstractTreeFeature.isAir(param0, var3x)) {
                    this.placeVine(param0, var3x, VineBlock.EAST, param4, param5);
                }
            }

            if (param1.nextInt(3) > 0) {
                BlockPos var1 = param4x.east();
                if (AbstractTreeFeature.isAir(param0, var1)) {
                    this.placeVine(param0, var1, VineBlock.WEST, param4, param5);
                }
            }

            if (param1.nextInt(3) > 0) {
                BlockPos var2 = param4x.north();
                if (AbstractTreeFeature.isAir(param0, var2)) {
                    this.placeVine(param0, var2, VineBlock.SOUTH, param4, param5);
                }
            }

            if (param1.nextInt(3) > 0) {
                BlockPos var3 = param4x.south();
                if (AbstractTreeFeature.isAir(param0, var3)) {
                    this.placeVine(param0, var3, VineBlock.NORTH, param4, param5);
                }
            }

        });
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
                param0,
                param0.createMap(ImmutableMap.of(param0.createString("type"), param0.createString(Registry.TREE_DECORATOR_TYPES.getKey(this.type).toString())))
            )
            .getValue();
    }

    public static TrunkVineDecorator random(Random param0) {
        return new TrunkVineDecorator();
    }
}
