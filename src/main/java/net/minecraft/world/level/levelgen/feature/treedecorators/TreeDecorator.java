package net.minecraft.world.level.levelgen.feature.treedecorators;

import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class TreeDecorator implements Serializable {
    protected final TreeDecoratorType<?> type;

    protected TreeDecorator(TreeDecoratorType<?> param0) {
        this.type = param0;
    }

    public abstract void place(LevelAccessor var1, Random var2, List<BlockPos> var3, List<BlockPos> var4, Set<BlockPos> var5, BoundingBox var6);

    protected void placeVine(LevelWriter param0, BlockPos param1, BooleanProperty param2, Set<BlockPos> param3, BoundingBox param4) {
        this.setBlock(param0, param1, Blocks.VINE.defaultBlockState().setValue(param2, Boolean.valueOf(true)), param3, param4);
    }

    protected void setBlock(LevelWriter param0, BlockPos param1, BlockState param2, Set<BlockPos> param3, BoundingBox param4) {
        param0.setBlock(param1, param2, 19);
        param3.add(param1);
        param4.expand(new BoundingBox(param1, param1));
    }
}