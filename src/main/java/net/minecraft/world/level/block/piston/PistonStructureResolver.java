package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

public class PistonStructureResolver {
    public static final int MAX_PUSH_DEPTH = 12;
    private final Level level;
    private final BlockPos pistonPos;
    private final boolean extending;
    private final BlockPos startPos;
    private final Direction pushDirection;
    private final List<BlockPos> toPush = Lists.newArrayList();
    private final List<BlockPos> toDestroy = Lists.newArrayList();
    private final Direction pistonDirection;

    public PistonStructureResolver(Level param0, BlockPos param1, Direction param2, boolean param3) {
        this.level = param0;
        this.pistonPos = param1;
        this.pistonDirection = param2;
        this.extending = param3;
        if (param3) {
            this.pushDirection = param2;
            this.startPos = param1.relative(param2);
        } else {
            this.pushDirection = param2.getOpposite();
            this.startPos = param1.relative(param2, 2);
        }

    }

    public boolean resolve() {
        this.toPush.clear();
        this.toDestroy.clear();
        BlockState var0 = this.level.getBlockState(this.startPos);
        if (!PistonBaseBlock.isPushable(var0, this.level, this.startPos, this.pushDirection, false, this.pistonDirection)) {
            if (this.extending && var0.getPistonPushReaction() == PushReaction.DESTROY) {
                this.toDestroy.add(this.startPos);
                return true;
            } else {
                return false;
            }
        } else if (!this.addBlockLine(this.startPos, this.pushDirection)) {
            return false;
        } else {
            for(int var1 = 0; var1 < this.toPush.size(); ++var1) {
                BlockPos var2 = this.toPush.get(var1);
                if (isSticky(this.level.getBlockState(var2)) && !this.addBranchingBlocks(var2)) {
                    return false;
                }
            }

            return true;
        }
    }

    private static boolean isSticky(BlockState param0) {
        return param0.is(Blocks.SLIME_BLOCK) || param0.is(Blocks.HONEY_BLOCK);
    }

    private static boolean canStickToEachOther(BlockState param0, BlockState param1) {
        if (param0.is(Blocks.HONEY_BLOCK) && param1.is(Blocks.SLIME_BLOCK)) {
            return false;
        } else if (param0.is(Blocks.SLIME_BLOCK) && param1.is(Blocks.HONEY_BLOCK)) {
            return false;
        } else {
            return isSticky(param0) || isSticky(param1);
        }
    }

    private boolean addBlockLine(BlockPos param0, Direction param1) {
        BlockState var0 = this.level.getBlockState(param0);
        if (var0.isAir()) {
            return true;
        } else if (!PistonBaseBlock.isPushable(var0, this.level, param0, this.pushDirection, false, param1)) {
            return true;
        } else if (param0.equals(this.pistonPos)) {
            return true;
        } else if (this.toPush.contains(param0)) {
            return true;
        } else {
            int var1 = 1;
            if (var1 + this.toPush.size() > 12) {
                return false;
            } else {
                while(isSticky(var0)) {
                    BlockPos var2 = param0.relative(this.pushDirection.getOpposite(), var1);
                    BlockState var3 = var0;
                    var0 = this.level.getBlockState(var2);
                    if (var0.isAir()
                        || !canStickToEachOther(var3, var0)
                        || !PistonBaseBlock.isPushable(var0, this.level, var2, this.pushDirection, false, this.pushDirection.getOpposite())
                        || var2.equals(this.pistonPos)) {
                        break;
                    }

                    if (++var1 + this.toPush.size() > 12) {
                        return false;
                    }
                }

                int var4 = 0;

                for(int var5 = var1 - 1; var5 >= 0; --var5) {
                    this.toPush.add(param0.relative(this.pushDirection.getOpposite(), var5));
                    ++var4;
                }

                int var6 = 1;

                while(true) {
                    BlockPos var7 = param0.relative(this.pushDirection, var6);
                    int var8 = this.toPush.indexOf(var7);
                    if (var8 > -1) {
                        this.reorderListAtCollision(var4, var8);

                        for(int var9 = 0; var9 <= var8 + var4; ++var9) {
                            BlockPos var10 = this.toPush.get(var9);
                            if (isSticky(this.level.getBlockState(var10)) && !this.addBranchingBlocks(var10)) {
                                return false;
                            }
                        }

                        return true;
                    }

                    var0 = this.level.getBlockState(var7);
                    if (var0.isAir()) {
                        return true;
                    }

                    if (!PistonBaseBlock.isPushable(var0, this.level, var7, this.pushDirection, true, this.pushDirection) || var7.equals(this.pistonPos)) {
                        return false;
                    }

                    if (var0.getPistonPushReaction() == PushReaction.DESTROY) {
                        this.toDestroy.add(var7);
                        return true;
                    }

                    if (this.toPush.size() >= 12) {
                        return false;
                    }

                    this.toPush.add(var7);
                    ++var4;
                    ++var6;
                }
            }
        }
    }

    private void reorderListAtCollision(int param0, int param1) {
        List<BlockPos> var0 = Lists.newArrayList();
        List<BlockPos> var1 = Lists.newArrayList();
        List<BlockPos> var2 = Lists.newArrayList();
        var0.addAll(this.toPush.subList(0, param1));
        var1.addAll(this.toPush.subList(this.toPush.size() - param0, this.toPush.size()));
        var2.addAll(this.toPush.subList(param1, this.toPush.size() - param0));
        this.toPush.clear();
        this.toPush.addAll(var0);
        this.toPush.addAll(var1);
        this.toPush.addAll(var2);
    }

    private boolean addBranchingBlocks(BlockPos param0) {
        BlockState var0 = this.level.getBlockState(param0);

        for(Direction var1 : Direction.values()) {
            if (var1.getAxis() != this.pushDirection.getAxis()) {
                BlockPos var2 = param0.relative(var1);
                BlockState var3 = this.level.getBlockState(var2);
                if (canStickToEachOther(var3, var0) && !this.addBlockLine(var2, var1)) {
                    return false;
                }
            }
        }

        return true;
    }

    public Direction getPushDirection() {
        return this.pushDirection;
    }

    public List<BlockPos> getToPush() {
        return this.toPush;
    }

    public List<BlockPos> getToDestroy() {
        return this.toDestroy;
    }
}
