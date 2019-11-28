package net.minecraft.world.level;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.Vec3;

public class PortalForcer {
    private final ServerLevel level;
    private final Random random;

    public PortalForcer(ServerLevel param0) {
        this.level = param0;
        this.random = new Random(param0.getSeed());
    }

    public boolean findAndMoveToPortal(Entity param0, float param1) {
        Vec3 var0 = param0.getPortalEntranceOffset();
        Direction var1 = param0.getPortalEntranceForwards();
        BlockPattern.PortalInfo var2 = this.findPortal(new BlockPos(param0), param0.getDeltaMovement(), var1, var0.x, var0.y, param0 instanceof Player);
        if (var2 == null) {
            return false;
        } else {
            Vec3 var3 = var2.pos;
            Vec3 var4 = var2.speed;
            param0.setDeltaMovement(var4);
            param0.yRot = param1 + (float)var2.angle;
            param0.forceMove(var3.x, var3.y, var3.z);
            return true;
        }
    }

    @Nullable
    public BlockPattern.PortalInfo findPortal(BlockPos param0, Vec3 param1, Direction param2, double param3, double param4, boolean param5) {
        PoiManager var0 = this.level.getPoiManager();
        var0.ensureLoadedAndValid(this.level, param0, 128);
        List<PoiRecord> var1 = var0.getInSquare(param0x -> param0x == PoiType.NETHER_PORTAL, param0, 128, PoiManager.Occupancy.ANY)
            .collect(Collectors.toList());
        Optional<PoiRecord> var2 = var1.stream()
            .min(Comparator.<PoiRecord>comparingDouble(param1x -> param1x.getPos().distSqr(param0)).thenComparingInt(param0x -> param0x.getPos().getY()));
        return var2.<BlockPattern.PortalInfo>map(param4x -> {
            BlockPos var0x = param4x.getPos();
            this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(var0x), 3, var0x);
            BlockPattern.BlockPatternMatch var1x = NetherPortalBlock.getPortalShape(this.level, var0x);
            return var1x.getPortalOutput(param2, var0x, param4, param1, param3);
        }).orElse(null);
    }

    public boolean createPortal(Entity param0) {
        int var0 = 16;
        double var1 = -1.0;
        int var2 = Mth.floor(param0.getX());
        int var3 = Mth.floor(param0.getY());
        int var4 = Mth.floor(param0.getZ());
        int var5 = var2;
        int var6 = var3;
        int var7 = var4;
        int var8 = 0;
        int var9 = this.random.nextInt(4);
        BlockPos.MutableBlockPos var10 = new BlockPos.MutableBlockPos();

        for(int var11 = var2 - 16; var11 <= var2 + 16; ++var11) {
            double var12 = (double)var11 + 0.5 - param0.getX();

            for(int var13 = var4 - 16; var13 <= var4 + 16; ++var13) {
                double var14 = (double)var13 + 0.5 - param0.getZ();

                label276:
                for(int var15 = this.level.getHeight() - 1; var15 >= 0; --var15) {
                    if (this.level.isEmptyBlock(var10.set(var11, var15, var13))) {
                        while(var15 > 0 && this.level.isEmptyBlock(var10.set(var11, var15 - 1, var13))) {
                            --var15;
                        }

                        for(int var16 = var9; var16 < var9 + 4; ++var16) {
                            int var17 = var16 % 2;
                            int var18 = 1 - var17;
                            if (var16 % 4 >= 2) {
                                var17 = -var17;
                                var18 = -var18;
                            }

                            for(int var19 = 0; var19 < 3; ++var19) {
                                for(int var20 = 0; var20 < 4; ++var20) {
                                    for(int var21 = -1; var21 < 4; ++var21) {
                                        int var22 = var11 + (var20 - 1) * var17 + var19 * var18;
                                        int var23 = var15 + var21;
                                        int var24 = var13 + (var20 - 1) * var18 - var19 * var17;
                                        var10.set(var22, var23, var24);
                                        if (var21 < 0 && !this.level.getBlockState(var10).getMaterial().isSolid()
                                            || var21 >= 0 && !this.level.isEmptyBlock(var10)) {
                                            continue label276;
                                        }
                                    }
                                }
                            }

                            double var25 = (double)var15 + 0.5 - param0.getY();
                            double var26 = var12 * var12 + var25 * var25 + var14 * var14;
                            if (var1 < 0.0 || var26 < var1) {
                                var1 = var26;
                                var5 = var11;
                                var6 = var15;
                                var7 = var13;
                                var8 = var16 % 4;
                            }
                        }
                    }
                }
            }
        }

        if (var1 < 0.0) {
            for(int var27 = var2 - 16; var27 <= var2 + 16; ++var27) {
                double var28 = (double)var27 + 0.5 - param0.getX();

                for(int var29 = var4 - 16; var29 <= var4 + 16; ++var29) {
                    double var30 = (double)var29 + 0.5 - param0.getZ();

                    label214:
                    for(int var31 = this.level.getHeight() - 1; var31 >= 0; --var31) {
                        if (this.level.isEmptyBlock(var10.set(var27, var31, var29))) {
                            while(var31 > 0 && this.level.isEmptyBlock(var10.set(var27, var31 - 1, var29))) {
                                --var31;
                            }

                            for(int var32 = var9; var32 < var9 + 2; ++var32) {
                                int var33 = var32 % 2;
                                int var34 = 1 - var33;

                                for(int var35 = 0; var35 < 4; ++var35) {
                                    for(int var36 = -1; var36 < 4; ++var36) {
                                        int var37 = var27 + (var35 - 1) * var33;
                                        int var38 = var31 + var36;
                                        int var39 = var29 + (var35 - 1) * var34;
                                        var10.set(var37, var38, var39);
                                        if (var36 < 0 && !this.level.getBlockState(var10).getMaterial().isSolid()
                                            || var36 >= 0 && !this.level.isEmptyBlock(var10)) {
                                            continue label214;
                                        }
                                    }
                                }

                                double var40 = (double)var31 + 0.5 - param0.getY();
                                double var41 = var28 * var28 + var40 * var40 + var30 * var30;
                                if (var1 < 0.0 || var41 < var1) {
                                    var1 = var41;
                                    var5 = var27;
                                    var6 = var31;
                                    var7 = var29;
                                    var8 = var32 % 2;
                                }
                            }
                        }
                    }
                }
            }
        }

        int var43 = var5;
        int var44 = var6;
        int var45 = var7;
        int var46 = var8 % 2;
        int var47 = 1 - var46;
        if (var8 % 4 >= 2) {
            var46 = -var46;
            var47 = -var47;
        }

        if (var1 < 0.0) {
            var6 = Mth.clamp(var6, 70, this.level.getHeight() - 10);
            var44 = var6;

            for(int var48 = -1; var48 <= 1; ++var48) {
                for(int var49 = 1; var49 < 3; ++var49) {
                    for(int var50 = -1; var50 < 3; ++var50) {
                        int var51 = var43 + (var49 - 1) * var46 + var48 * var47;
                        int var52 = var44 + var50;
                        int var53 = var45 + (var49 - 1) * var47 - var48 * var46;
                        boolean var54 = var50 < 0;
                        var10.set(var51, var52, var53);
                        this.level.setBlockAndUpdate(var10, var54 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        for(int var55 = -1; var55 < 3; ++var55) {
            for(int var56 = -1; var56 < 4; ++var56) {
                if (var55 == -1 || var55 == 2 || var56 == -1 || var56 == 3) {
                    var10.set(var43 + var55 * var46, var44 + var56, var45 + var55 * var47);
                    this.level.setBlock(var10, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
            }
        }

        BlockState var57 = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, var46 == 0 ? Direction.Axis.Z : Direction.Axis.X);

        for(int var58 = 0; var58 < 2; ++var58) {
            for(int var59 = 0; var59 < 3; ++var59) {
                var10.set(var43 + var58 * var46, var44 + var59, var45 + var58 * var47);
                this.level.setBlock(var10, var57, 18);
            }
        }

        return true;
    }
}
