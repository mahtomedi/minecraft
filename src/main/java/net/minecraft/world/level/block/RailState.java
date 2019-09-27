package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public class RailState {
    private final Level level;
    private final BlockPos pos;
    private final BaseRailBlock block;
    private BlockState state;
    private final boolean isStraight;
    private final List<BlockPos> connections = Lists.newArrayList();

    public RailState(Level param0, BlockPos param1, BlockState param2) {
        this.level = param0;
        this.pos = param1;
        this.state = param2;
        this.block = (BaseRailBlock)param2.getBlock();
        RailShape var0 = param2.getValue(this.block.getShapeProperty());
        this.isStraight = this.block.isStraight();
        this.updateConnections(var0);
    }

    public List<BlockPos> getConnections() {
        return this.connections;
    }

    private void updateConnections(RailShape param0) {
        this.connections.clear();
        switch(param0) {
            case NORTH_SOUTH:
                this.connections.add(this.pos.north());
                this.connections.add(this.pos.south());
                break;
            case EAST_WEST:
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.east());
                break;
            case ASCENDING_EAST:
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.east().above());
                break;
            case ASCENDING_WEST:
                this.connections.add(this.pos.west().above());
                this.connections.add(this.pos.east());
                break;
            case ASCENDING_NORTH:
                this.connections.add(this.pos.north().above());
                this.connections.add(this.pos.south());
                break;
            case ASCENDING_SOUTH:
                this.connections.add(this.pos.north());
                this.connections.add(this.pos.south().above());
                break;
            case SOUTH_EAST:
                this.connections.add(this.pos.east());
                this.connections.add(this.pos.south());
                break;
            case SOUTH_WEST:
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.south());
                break;
            case NORTH_WEST:
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.north());
                break;
            case NORTH_EAST:
                this.connections.add(this.pos.east());
                this.connections.add(this.pos.north());
        }

    }

    private void removeSoftConnections() {
        for(int var0 = 0; var0 < this.connections.size(); ++var0) {
            RailState var1 = this.getRail(this.connections.get(var0));
            if (var1 != null && var1.connectsTo(this)) {
                this.connections.set(var0, var1.pos);
            } else {
                this.connections.remove(var0--);
            }
        }

    }

    private boolean hasRail(BlockPos param0) {
        return BaseRailBlock.isRail(this.level, param0) || BaseRailBlock.isRail(this.level, param0.above()) || BaseRailBlock.isRail(this.level, param0.below());
    }

    @Nullable
    private RailState getRail(BlockPos param0) {
        BlockState var1 = this.level.getBlockState(param0);
        if (BaseRailBlock.isRail(var1)) {
            return new RailState(this.level, param0, var1);
        } else {
            BlockPos var0 = param0.above();
            var1 = this.level.getBlockState(var0);
            if (BaseRailBlock.isRail(var1)) {
                return new RailState(this.level, var0, var1);
            } else {
                var0 = param0.below();
                var1 = this.level.getBlockState(var0);
                return BaseRailBlock.isRail(var1) ? new RailState(this.level, var0, var1) : null;
            }
        }
    }

    private boolean connectsTo(RailState param0) {
        return this.hasConnection(param0.pos);
    }

    private boolean hasConnection(BlockPos param0) {
        for(int var0 = 0; var0 < this.connections.size(); ++var0) {
            BlockPos var1 = this.connections.get(var0);
            if (var1.getX() == param0.getX() && var1.getZ() == param0.getZ()) {
                return true;
            }
        }

        return false;
    }

    protected int countPotentialConnections() {
        int var0 = 0;

        for(Direction var1 : Direction.Plane.HORIZONTAL) {
            if (this.hasRail(this.pos.relative(var1))) {
                ++var0;
            }
        }

        return var0;
    }

    private boolean canConnectTo(RailState param0) {
        return this.connectsTo(param0) || this.connections.size() != 2;
    }

    private void connectTo(RailState param0) {
        this.connections.add(param0.pos);
        BlockPos var0 = this.pos.north();
        BlockPos var1 = this.pos.south();
        BlockPos var2 = this.pos.west();
        BlockPos var3 = this.pos.east();
        boolean var4 = this.hasConnection(var0);
        boolean var5 = this.hasConnection(var1);
        boolean var6 = this.hasConnection(var2);
        boolean var7 = this.hasConnection(var3);
        RailShape var8 = null;
        if (var4 || var5) {
            var8 = RailShape.NORTH_SOUTH;
        }

        if (var6 || var7) {
            var8 = RailShape.EAST_WEST;
        }

        if (!this.isStraight) {
            if (var5 && var7 && !var4 && !var6) {
                var8 = RailShape.SOUTH_EAST;
            }

            if (var5 && var6 && !var4 && !var7) {
                var8 = RailShape.SOUTH_WEST;
            }

            if (var4 && var6 && !var5 && !var7) {
                var8 = RailShape.NORTH_WEST;
            }

            if (var4 && var7 && !var5 && !var6) {
                var8 = RailShape.NORTH_EAST;
            }
        }

        if (var8 == RailShape.NORTH_SOUTH) {
            if (BaseRailBlock.isRail(this.level, var0.above())) {
                var8 = RailShape.ASCENDING_NORTH;
            }

            if (BaseRailBlock.isRail(this.level, var1.above())) {
                var8 = RailShape.ASCENDING_SOUTH;
            }
        }

        if (var8 == RailShape.EAST_WEST) {
            if (BaseRailBlock.isRail(this.level, var3.above())) {
                var8 = RailShape.ASCENDING_EAST;
            }

            if (BaseRailBlock.isRail(this.level, var2.above())) {
                var8 = RailShape.ASCENDING_WEST;
            }
        }

        if (var8 == null) {
            var8 = RailShape.NORTH_SOUTH;
        }

        this.state = this.state.setValue(this.block.getShapeProperty(), var8);
        this.level.setBlock(this.pos, this.state, 3);
    }

    private boolean hasNeighborRail(BlockPos param0) {
        RailState var0 = this.getRail(param0);
        if (var0 == null) {
            return false;
        } else {
            var0.removeSoftConnections();
            return var0.canConnectTo(this);
        }
    }

    public RailState place(boolean param0, boolean param1, RailShape param2) {
        BlockPos var0 = this.pos.north();
        BlockPos var1 = this.pos.south();
        BlockPos var2 = this.pos.west();
        BlockPos var3 = this.pos.east();
        boolean var4 = this.hasNeighborRail(var0);
        boolean var5 = this.hasNeighborRail(var1);
        boolean var6 = this.hasNeighborRail(var2);
        boolean var7 = this.hasNeighborRail(var3);
        RailShape var8 = null;
        boolean var9 = var4 || var5;
        boolean var10 = var6 || var7;
        if (var9 && !var10) {
            var8 = RailShape.NORTH_SOUTH;
        }

        if (var10 && !var9) {
            var8 = RailShape.EAST_WEST;
        }

        boolean var11 = var5 && var7;
        boolean var12 = var5 && var6;
        boolean var13 = var4 && var7;
        boolean var14 = var4 && var6;
        if (!this.isStraight) {
            if (var11 && !var4 && !var6) {
                var8 = RailShape.SOUTH_EAST;
            }

            if (var12 && !var4 && !var7) {
                var8 = RailShape.SOUTH_WEST;
            }

            if (var14 && !var5 && !var7) {
                var8 = RailShape.NORTH_WEST;
            }

            if (var13 && !var5 && !var6) {
                var8 = RailShape.NORTH_EAST;
            }
        }

        if (var8 == null) {
            if (var9 && var10) {
                var8 = param2;
            } else if (var9) {
                var8 = RailShape.NORTH_SOUTH;
            } else if (var10) {
                var8 = RailShape.EAST_WEST;
            }

            if (!this.isStraight) {
                if (param0) {
                    if (var11) {
                        var8 = RailShape.SOUTH_EAST;
                    }

                    if (var12) {
                        var8 = RailShape.SOUTH_WEST;
                    }

                    if (var13) {
                        var8 = RailShape.NORTH_EAST;
                    }

                    if (var14) {
                        var8 = RailShape.NORTH_WEST;
                    }
                } else {
                    if (var14) {
                        var8 = RailShape.NORTH_WEST;
                    }

                    if (var13) {
                        var8 = RailShape.NORTH_EAST;
                    }

                    if (var12) {
                        var8 = RailShape.SOUTH_WEST;
                    }

                    if (var11) {
                        var8 = RailShape.SOUTH_EAST;
                    }
                }
            }
        }

        if (var8 == RailShape.NORTH_SOUTH) {
            if (BaseRailBlock.isRail(this.level, var0.above())) {
                var8 = RailShape.ASCENDING_NORTH;
            }

            if (BaseRailBlock.isRail(this.level, var1.above())) {
                var8 = RailShape.ASCENDING_SOUTH;
            }
        }

        if (var8 == RailShape.EAST_WEST) {
            if (BaseRailBlock.isRail(this.level, var3.above())) {
                var8 = RailShape.ASCENDING_EAST;
            }

            if (BaseRailBlock.isRail(this.level, var2.above())) {
                var8 = RailShape.ASCENDING_WEST;
            }
        }

        if (var8 == null) {
            var8 = param2;
        }

        this.updateConnections(var8);
        this.state = this.state.setValue(this.block.getShapeProperty(), var8);
        if (param1 || this.level.getBlockState(this.pos) != this.state) {
            this.level.setBlock(this.pos, this.state, 3);

            for(int var15 = 0; var15 < this.connections.size(); ++var15) {
                RailState var16 = this.getRail(this.connections.get(var15));
                if (var16 != null) {
                    var16.removeSoftConnections();
                    if (var16.canConnectTo(this)) {
                        var16.connectTo(this);
                    }
                }
            }
        }

        return this;
    }

    public BlockState getState() {
        return this.state;
    }
}
