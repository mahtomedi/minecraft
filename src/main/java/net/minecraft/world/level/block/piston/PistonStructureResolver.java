package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

public class PistonStructureResolver {
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
                if (isSticky(this.level.getBlockState(var2).getBlock()) && !this.addBranchingBlocks(var2)) {
                    return false;
                }
            }

            return true;
        }
    }

    private static boolean isSticky(Block param0) {
        return param0 == Blocks.SLIME_BLOCK || param0 == Blocks.HONEY_BLOCK;
    }

    private static boolean canStickToEachOther(Block param0, Block param1) {
        if (param0 == Blocks.HONEY_BLOCK && param1 == Blocks.SLIME_BLOCK) {
            return false;
        } else if (param0 == Blocks.SLIME_BLOCK && param1 == Blocks.HONEY_BLOCK) {
            return false;
        } else {
            return isSticky(param0) || isSticky(param1);
        }
    }

    private boolean addBlockLine(BlockPos param0, Direction param1) {
        BlockState var0 = this.level.getBlockState(param0);
        Block var1 = var0.getBlock();
        if (var0.isAir()) {
            return true;
        } else if (!PistonBaseBlock.isPushable(var0, this.level, param0, this.pushDirection, false, param1)) {
            return true;
        } else if (param0.equals(this.pistonPos)) {
            return true;
        } else if (this.toPush.contains(param0)) {
            return true;
        } else {
            int var2 = 1;
            if (var2 + this.toPush.size() > 12) {
                return false;
            } else {
                while(isSticky(var1)) {
                    BlockPos var3 = param0.relative(this.pushDirection.getOpposite(), var2);
                    Block var4 = var1;
                    var0 = this.level.getBlockState(var3);
                    var1 = var0.getBlock();
                    if (var0.isAir()
                        || !canStickToEachOther(var4, var1)
                        || !PistonBaseBlock.isPushable(var0, this.level, var3, this.pushDirection, false, this.pushDirection.getOpposite())
                        || var3.equals(this.pistonPos)) {
                        break;
                    }

                    if (++var2 + this.toPush.size() > 12) {
                        return false;
                    }
                }

                int var5 = 0;

                for(int var6 = var2 - 1; var6 >= 0; --var6) {
                    this.toPush.add(param0.relative(this.pushDirection.getOpposite(), var6));
                    ++var5;
                }

                int var7 = 1;

                while(true) {
                    BlockPos var8 = param0.relative(this.pushDirection, var7);
                    int var9 = this.toPush.indexOf(var8);
                    if (var9 > -1) {
                        this.reorderListAtCollision(var5, var9);

                        for(int var10 = 0; var10 <= var9 + var5; ++var10) {
                            BlockPos var11 = this.toPush.get(var10);
                            if (isSticky(this.level.getBlockState(var11).getBlock()) && !this.addBranchingBlocks(var11)) {
                                return false;
                            }
                        }

                        return true;
                    }

                    var0 = this.level.getBlockState(var8);
                    if (var0.isAir()) {
                        return true;
                    }

                    if (!PistonBaseBlock.isPushable(var0, this.level, var8, this.pushDirection, true, this.pushDirection) || var8.equals(this.pistonPos)) {
                        return false;
                    }

                    if (var0.getPistonPushReaction() == PushReaction.DESTROY) {
                        this.toDestroy.add(var8);
                        return true;
                    }

                    if (this.toPush.size() >= 12) {
                        return false;
                    }

                    this.toPush.add(var8);
                    ++var5;
                    ++var7;
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
                if (canStickToEachOther(var3.getBlock(), var0.getBlock()) && !this.addBlockLine(var2, var1)) {
                    return false;
                }
            }
        }

        return true;
    }

    public List<BlockPos> getToPush() {
        return this.toPush;
    }

    public List<BlockPos> getToDestroy() {
        return this.toDestroy;
    }
}
