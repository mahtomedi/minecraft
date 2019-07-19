package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.phys.Vec3;

public class WaterAvoidingRandomFlyingGoal extends WaterAvoidingRandomStrollGoal {
    public WaterAvoidingRandomFlyingGoal(PathfinderMob param0, double param1) {
        super(param0, param1);
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        Vec3 var0 = null;
        if (this.mob.isInWater()) {
            var0 = RandomPos.getLandPos(this.mob, 15, 15);
        }

        if (this.mob.getRandom().nextFloat() >= this.probability) {
            var0 = this.getTreePos();
        }

        return var0 == null ? super.getPosition() : var0;
    }

    @Nullable
    private Vec3 getTreePos() {
        BlockPos var0 = new BlockPos(this.mob);
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

        for(BlockPos var4 : BlockPos.betweenClosed(
            Mth.floor(this.mob.x - 3.0),
            Mth.floor(this.mob.y - 6.0),
            Mth.floor(this.mob.z - 3.0),
            Mth.floor(this.mob.x + 3.0),
            Mth.floor(this.mob.y + 6.0),
            Mth.floor(this.mob.z + 3.0)
        )) {
            if (!var0.equals(var4)) {
                Block var5 = this.mob.level.getBlockState(var2.set(var4).move(Direction.DOWN)).getBlock();
                boolean var6 = var5 instanceof LeavesBlock || var5.is(BlockTags.LOGS);
                if (var6 && this.mob.level.isEmptyBlock(var4) && this.mob.level.isEmptyBlock(var1.set(var4).move(Direction.UP))) {
                    return new Vec3((double)var4.getX(), (double)var4.getY(), (double)var4.getZ());
                }
            }
        }

        return null;
    }
}
