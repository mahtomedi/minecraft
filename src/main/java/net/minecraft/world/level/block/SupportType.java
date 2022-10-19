package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum SupportType {
    FULL {
        @Override
        public boolean isSupporting(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
            return Block.isFaceFull(param0.getBlockSupportShape(param1, param2), param3);
        }
    },
    CENTER {
        private final int CENTER_SUPPORT_WIDTH = 1;
        private final VoxelShape CENTER_SUPPORT_SHAPE = Block.box(7.0, 0.0, 7.0, 9.0, 10.0, 9.0);
        private static final List<TagKey<Block>> OVERRIDE_TO_SUPPORT = Lists.newArrayList(BlockTags.CEILING_HANGING_SIGNS, BlockTags.WALL_HANGING_SIGNS);

        @Override
        public boolean isSupporting(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
            for(TagKey<Block> var0 : OVERRIDE_TO_SUPPORT) {
                if (param0.is(var0)) {
                    return true;
                }
            }

            return !Shapes.joinIsNotEmpty(param0.getBlockSupportShape(param1, param2).getFaceShape(param3), this.CENTER_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
        }
    },
    RIGID {
        private final int RIGID_SUPPORT_WIDTH = 2;
        private final VoxelShape RIGID_SUPPORT_SHAPE = Shapes.join(Shapes.block(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), BooleanOp.ONLY_FIRST);

        @Override
        public boolean isSupporting(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
            return !Shapes.joinIsNotEmpty(param0.getBlockSupportShape(param1, param2).getFaceShape(param3), this.RIGID_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
        }
    };

    public abstract boolean isSupporting(BlockState var1, BlockGetter var2, BlockPos var3, Direction var4);
}
