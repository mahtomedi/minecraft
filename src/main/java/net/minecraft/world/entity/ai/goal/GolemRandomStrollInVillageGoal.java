package net.minecraft.world.entity.ai.goal;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class GolemRandomStrollInVillageGoal extends RandomStrollGoal {
    public GolemRandomStrollInVillageGoal(PathfinderMob param0, double param1) {
        super(param0, param1, 240, false);
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        float var0 = this.mob.level.random.nextFloat();
        if (this.mob.level.random.nextFloat() < 0.3F) {
            return this.getPositionTowardsAnywhere();
        } else {
            Vec3 var1;
            if (var0 < 0.7F) {
                var1 = this.getPositionTowardsVillagerWhoWantsGolem();
                if (var1 == null) {
                    var1 = this.getPositionTowardsPoi();
                }
            } else {
                var1 = this.getPositionTowardsPoi();
                if (var1 == null) {
                    var1 = this.getPositionTowardsVillagerWhoWantsGolem();
                }
            }

            return var1 == null ? this.getPositionTowardsAnywhere() : var1;
        }
    }

    @Nullable
    private Vec3 getPositionTowardsAnywhere() {
        return LandRandomPos.getPos(this.mob, 10, 7);
    }

    @Nullable
    private Vec3 getPositionTowardsVillagerWhoWantsGolem() {
        ServerLevel var0 = (ServerLevel)this.mob.level;
        List<Villager> var1 = var0.getEntities(EntityType.VILLAGER, this.mob.getBoundingBox().inflate(32.0), this::doesVillagerWantGolem);
        if (var1.isEmpty()) {
            return null;
        } else {
            Villager var2 = var1.get(this.mob.level.random.nextInt(var1.size()));
            Vec3 var3 = var2.position();
            return LandRandomPos.getPosTowards(this.mob, 10, 7, var3);
        }
    }

    @Nullable
    private Vec3 getPositionTowardsPoi() {
        SectionPos var0 = this.getRandomVillageSection();
        if (var0 == null) {
            return null;
        } else {
            BlockPos var1 = this.getRandomPoiWithinSection(var0);
            return var1 == null ? null : LandRandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(var1));
        }
    }

    @Nullable
    private SectionPos getRandomVillageSection() {
        ServerLevel var0 = (ServerLevel)this.mob.level;
        List<SectionPos> var1 = SectionPos.cube(SectionPos.of(this.mob), 2).filter(param1 -> var0.sectionsToVillage(param1) == 0).collect(Collectors.toList());
        return var1.isEmpty() ? null : var1.get(var0.random.nextInt(var1.size()));
    }

    @Nullable
    private BlockPos getRandomPoiWithinSection(SectionPos param0) {
        ServerLevel var0 = (ServerLevel)this.mob.level;
        PoiManager var1 = var0.getPoiManager();
        List<BlockPos> var2 = var1.getInRange(param0x -> true, param0.center(), 8, PoiManager.Occupancy.IS_OCCUPIED)
            .map(PoiRecord::getPos)
            .collect(Collectors.toList());
        return var2.isEmpty() ? null : var2.get(var0.random.nextInt(var2.size()));
    }

    private boolean doesVillagerWantGolem(Villager param0) {
        return param0.wantsToSpawnGolem(this.mob.level.getGameTime());
    }
}
