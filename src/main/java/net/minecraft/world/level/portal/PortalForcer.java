package net.minecraft.world.level.portal;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

public class PortalForcer {
    private static final int TICKET_RADIUS = 3;
    private static final int SEARCH_RADIUS = 128;
    private static final int CREATE_RADIUS = 16;
    private static final int FRAME_HEIGHT = 5;
    private static final int FRAME_WIDTH = 4;
    private static final int FRAME_BOX = 3;
    private static final int FRAME_HEIGHT_START = -1;
    private static final int FRAME_HEIGHT_END = 4;
    private static final int FRAME_WIDTH_START = -1;
    private static final int FRAME_WIDTH_END = 3;
    private static final int FRAME_BOX_START = -1;
    private static final int FRAME_BOX_END = 2;
    private static final int NOTHING_FOUND = -1;
    private final ServerLevel level;

    public PortalForcer(ServerLevel param0) {
        this.level = param0;
    }

    public Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos param0, boolean param1, WorldBorder param2) {
        PoiManager var0 = this.level.getPoiManager();
        int var1 = param1 ? 16 : 128;
        var0.ensureLoadedAndValid(this.level, param0, var1);
        Optional<PoiRecord> var2 = var0.getInSquare(param0x -> param0x.is(PoiTypes.NETHER_PORTAL), param0, var1, PoiManager.Occupancy.ANY)
            .filter(param1x -> param2.isWithinBounds(param1x.getPos()))
            .sorted(Comparator.<PoiRecord>comparingDouble(param1x -> param1x.getPos().distSqr(param0)).thenComparingInt(param0x -> param0x.getPos().getY()))
            .filter(param0x -> this.level.getBlockState(param0x.getPos()).hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
            .findFirst();
        return var2.map(
            param0x -> {
                BlockPos var0x = param0x.getPos();
                this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(var0x), 3, var0x);
                BlockState var1x = this.level.getBlockState(var0x);
                return BlockUtil.getLargestRectangleAround(
                    var0x,
                    var1x.getValue(BlockStateProperties.HORIZONTAL_AXIS),
                    21,
                    Direction.Axis.Y,
                    21,
                    param1x -> this.level.getBlockState(param1x) == var1x
                );
            }
        );
    }

    public Optional<BlockUtil.FoundRectangle> createPortal(BlockPos param0, Direction.Axis param1) {
        Direction var0 = Direction.get(Direction.AxisDirection.POSITIVE, param1);
        double var1 = -1.0;
        BlockPos var2 = null;
        double var3 = -1.0;
        BlockPos var4 = null;
        WorldBorder var5 = this.level.getWorldBorder();
        int var6 = Math.min(this.level.getMaxBuildHeight(), this.level.getMinBuildHeight() + this.level.getLogicalHeight()) - 1;
        BlockPos.MutableBlockPos var7 = param0.mutable();

        for(BlockPos.MutableBlockPos var8 : BlockPos.spiralAround(param0, 16, Direction.EAST, Direction.SOUTH)) {
            int var9 = Math.min(var6, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, var8.getX(), var8.getZ()));
            int var10 = 1;
            if (var5.isWithinBounds(var8) && var5.isWithinBounds(var8.move(var0, 1))) {
                var8.move(var0.getOpposite(), 1);

                for(int var11 = var9; var11 >= this.level.getMinBuildHeight(); --var11) {
                    var8.setY(var11);
                    if (this.level.isEmptyBlock(var8)) {
                        int var12 = var11;

                        while(var11 > this.level.getMinBuildHeight() && this.level.isEmptyBlock(var8.move(Direction.DOWN))) {
                            --var11;
                        }

                        if (var11 + 4 <= var6) {
                            int var13 = var12 - var11;
                            if (var13 <= 0 || var13 >= 3) {
                                var8.setY(var11);
                                if (this.canHostFrame(var8, var7, var0, 0)) {
                                    double var14 = param0.distSqr(var8);
                                    if (this.canHostFrame(var8, var7, var0, -1) && this.canHostFrame(var8, var7, var0, 1) && (var1 == -1.0 || var1 > var14)) {
                                        var1 = var14;
                                        var2 = var8.immutable();
                                    }

                                    if (var1 == -1.0 && (var3 == -1.0 || var3 > var14)) {
                                        var3 = var14;
                                        var4 = var8.immutable();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (var1 == -1.0 && var3 != -1.0) {
            var2 = var4;
            var1 = var3;
        }

        if (var1 == -1.0) {
            int var15 = Math.max(this.level.getMinBuildHeight() - -1, 70);
            int var16 = var6 - 9;
            if (var16 < var15) {
                return Optional.empty();
            }

            var2 = new BlockPos(param0.getX(), Mth.clamp(param0.getY(), var15, var16), param0.getZ()).immutable();
            Direction var17 = var0.getClockWise();
            if (!var5.isWithinBounds(var2)) {
                return Optional.empty();
            }

            for(int var18 = -1; var18 < 2; ++var18) {
                for(int var19 = 0; var19 < 2; ++var19) {
                    for(int var20 = -1; var20 < 3; ++var20) {
                        BlockState var21 = var20 < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        var7.setWithOffset(var2, var19 * var0.getStepX() + var18 * var17.getStepX(), var20, var19 * var0.getStepZ() + var18 * var17.getStepZ());
                        this.level.setBlockAndUpdate(var7, var21);
                    }
                }
            }
        }

        for(int var22 = -1; var22 < 3; ++var22) {
            for(int var23 = -1; var23 < 4; ++var23) {
                if (var22 == -1 || var22 == 2 || var23 == -1 || var23 == 3) {
                    var7.setWithOffset(var2, var22 * var0.getStepX(), var23, var22 * var0.getStepZ());
                    this.level.setBlock(var7, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
            }
        }

        BlockState var24 = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, param1);

        for(int var25 = 0; var25 < 2; ++var25) {
            for(int var26 = 0; var26 < 3; ++var26) {
                var7.setWithOffset(var2, var25 * var0.getStepX(), var26, var25 * var0.getStepZ());
                this.level.setBlock(var7, var24, 18);
            }
        }

        return Optional.of(new BlockUtil.FoundRectangle(var2.immutable(), 2, 3));
    }

    private boolean canHostFrame(BlockPos param0, BlockPos.MutableBlockPos param1, Direction param2, int param3) {
        Direction var0 = param2.getClockWise();

        for(int var1 = -1; var1 < 3; ++var1) {
            for(int var2 = -1; var2 < 4; ++var2) {
                param1.setWithOffset(param0, param2.getStepX() * var1 + var0.getStepX() * param3, var2, param2.getStepZ() * var1 + var0.getStepZ() * param3);
                if (var2 < 0 && !this.level.getBlockState(param1).getMaterial().isSolid()) {
                    return false;
                }

                if (var2 >= 0 && !this.level.isEmptyBlock(param1)) {
                    return false;
                }
            }
        }

        return true;
    }
}
