package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class AlterGroundDecorator extends TreeDecorator {
    private final BlockStateProvider provider;

    public AlterGroundDecorator(BlockStateProvider param0) {
        super(TreeDecoratorType.ALTER_GROUND);
        this.provider = param0;
    }

    public <T> AlterGroundDecorator(Dynamic<T> param0) {
        this(
            Registry.BLOCKSTATE_PROVIDER_TYPES
                .get(new ResourceLocation(param0.get("provider").get("type").asString().orElseThrow(RuntimeException::new)))
                .deserialize(param0.get("provider").orElseEmptyMap())
        );
    }

    @Override
    public void place(LevelAccessor param0, Random param1, List<BlockPos> param2, List<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5) {
        int var0 = param2.get(0).getY();
        param2.stream().filter(param1x -> param1x.getY() == var0).forEach(param2x -> {
            this.placeCircle(param0, param1, param2x.west().north());
            this.placeCircle(param0, param1, param2x.east(2).north());
            this.placeCircle(param0, param1, param2x.west().south(2));
            this.placeCircle(param0, param1, param2x.east(2).south(2));

            for(int var0x = 0; var0x < 5; ++var0x) {
                int var1x = param1.nextInt(64);
                int var2x = var1x % 8;
                int var3x = var1x / 8;
                if (var2x == 0 || var2x == 7 || var3x == 0 || var3x == 7) {
                    this.placeCircle(param0, param1, param2x.offset(-3 + var2x, 0, -3 + var3x));
                }
            }

        });
    }

    private void placeCircle(LevelSimulatedRW param0, Random param1, BlockPos param2) {
        for(int var0 = -2; var0 <= 2; ++var0) {
            for(int var1 = -2; var1 <= 2; ++var1) {
                if (Math.abs(var0) != 2 || Math.abs(var1) != 2) {
                    this.placeBlockAt(param0, param1, param2.offset(var0, 0, var1));
                }
            }
        }

    }

    private void placeBlockAt(LevelSimulatedRW param0, Random param1, BlockPos param2) {
        for(int var0 = 2; var0 >= -3; --var0) {
            BlockPos var1 = param2.above(var0);
            if (AbstractTreeFeature.isGrassOrDirt(param0, var1)) {
                param0.setBlock(var1, this.provider.getState(param1, param2), 19);
                break;
            }

            if (!AbstractTreeFeature.isAir(param0, var1) && var0 < 0) {
                break;
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
                        param0.createString("provider"),
                        this.provider.serialize(param0)
                    )
                )
            )
            .getValue();
    }

    public static AlterGroundDecorator random(Random param0) {
        return new AlterGroundDecorator(BlockStateProvider.random(param0));
    }
}
