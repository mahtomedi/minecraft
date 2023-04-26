package net.minecraft.world.entity.ai.goal;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class MoveThroughVillageGoal extends Goal {
    protected final PathfinderMob mob;
    private final double speedModifier;
    @Nullable
    private Path path;
    private BlockPos poiPos;
    private final boolean onlyAtNight;
    private final List<BlockPos> visited = Lists.newArrayList();
    private final int distanceToPoi;
    private final BooleanSupplier canDealWithDoors;

    public MoveThroughVillageGoal(PathfinderMob param0, double param1, boolean param2, int param3, BooleanSupplier param4) {
        this.mob = param0;
        this.speedModifier = param1;
        this.onlyAtNight = param2;
        this.distanceToPoi = param3;
        this.canDealWithDoors = param4;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        if (!GoalUtils.hasGroundPathNavigation(param0)) {
            throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
        }
    }

    @Override
    public boolean canUse() {
        if (!GoalUtils.hasGroundPathNavigation(this.mob)) {
            return false;
        } else {
            this.updateVisited();
            if (this.onlyAtNight && this.mob.level().isDay()) {
                return false;
            } else {
                ServerLevel var0 = (ServerLevel)this.mob.level();
                BlockPos var1 = this.mob.blockPosition();
                if (!var0.isCloseToVillage(var1, 6)) {
                    return false;
                } else {
                    Vec3 var2 = LandRandomPos.getPos(
                        this.mob,
                        15,
                        7,
                        param2 -> {
                            if (!var0.isVillage(param2)) {
                                return Double.NEGATIVE_INFINITY;
                            } else {
                                Optional<BlockPos> var0x = var0.getPoiManager()
                                    .find(param0x -> param0x.is(PoiTypeTags.VILLAGE), this::hasNotVisited, param2, 10, PoiManager.Occupancy.IS_OCCUPIED);
                                return var0x.<Double>map(param1x -> -param1x.distSqr(var1)).orElse(Double.NEGATIVE_INFINITY);
                            }
                        }
                    );
                    if (var2 == null) {
                        return false;
                    } else {
                        Optional<BlockPos> var3 = var0.getPoiManager()
                            .find(
                                param0 -> param0.is(PoiTypeTags.VILLAGE), this::hasNotVisited, BlockPos.containing(var2), 10, PoiManager.Occupancy.IS_OCCUPIED
                            );
                        if (var3.isEmpty()) {
                            return false;
                        } else {
                            this.poiPos = var3.get().immutable();
                            GroundPathNavigation var4 = (GroundPathNavigation)this.mob.getNavigation();
                            boolean var5 = var4.canOpenDoors();
                            var4.setCanOpenDoors(this.canDealWithDoors.getAsBoolean());
                            this.path = var4.createPath(this.poiPos, 0);
                            var4.setCanOpenDoors(var5);
                            if (this.path == null) {
                                Vec3 var6 = DefaultRandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(this.poiPos), (float) (Math.PI / 2));
                                if (var6 == null) {
                                    return false;
                                }

                                var4.setCanOpenDoors(this.canDealWithDoors.getAsBoolean());
                                this.path = this.mob.getNavigation().createPath(var6.x, var6.y, var6.z, 0);
                                var4.setCanOpenDoors(var5);
                                if (this.path == null) {
                                    return false;
                                }
                            }

                            for(int var7 = 0; var7 < this.path.getNodeCount(); ++var7) {
                                Node var8 = this.path.getNode(var7);
                                BlockPos var9 = new BlockPos(var8.x, var8.y + 1, var8.z);
                                if (DoorBlock.isWoodenDoor(this.mob.level(), var9)) {
                                    this.path = this.mob.getNavigation().createPath((double)var8.x, (double)var8.y, (double)var8.z, 0);
                                    break;
                                }
                            }

                            return this.path != null;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.getNavigation().isDone()) {
            return false;
        } else {
            return !this.poiPos.closerToCenterThan(this.mob.position(), (double)(this.mob.getBbWidth() + (float)this.distanceToPoi));
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
    }

    @Override
    public void stop() {
        if (this.mob.getNavigation().isDone() || this.poiPos.closerToCenterThan(this.mob.position(), (double)this.distanceToPoi)) {
            this.visited.add(this.poiPos);
        }

    }

    private boolean hasNotVisited(BlockPos param0) {
        for(BlockPos var0x : this.visited) {
            if (Objects.equals(param0, var0x)) {
                return false;
            }
        }

        return true;
    }

    private void updateVisited() {
        if (this.visited.size() > 15) {
            this.visited.remove(0);
        }

    }
}
