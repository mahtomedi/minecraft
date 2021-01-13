package net.minecraft.world.entity.ai.goal.target;

import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

public class ResetUniversalAngerTargetGoal<T extends Mob & NeutralMob> extends Goal {
    private final T mob;
    private final boolean alertOthersOfSameType;
    private int lastHurtByPlayerTimestamp;

    public ResetUniversalAngerTargetGoal(T param0, boolean param1) {
        this.mob = param0;
        this.alertOthersOfSameType = param1;
    }

    @Override
    public boolean canUse() {
        return this.mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && this.wasHurtByPlayer();
    }

    private boolean wasHurtByPlayer() {
        return this.mob.getLastHurtByMob() != null
            && this.mob.getLastHurtByMob().getType() == EntityType.PLAYER
            && this.mob.getLastHurtByMobTimestamp() > this.lastHurtByPlayerTimestamp;
    }

    @Override
    public void start() {
        this.lastHurtByPlayerTimestamp = this.mob.getLastHurtByMobTimestamp();
        this.mob.forgetCurrentTargetAndRefreshUniversalAnger();
        if (this.alertOthersOfSameType) {
            this.getNearbyMobsOfSameType()
                .stream()
                .filter(param0 -> param0 != this.mob)
                .map(param0 -> (NeutralMob)param0)
                .forEach(NeutralMob::forgetCurrentTargetAndRefreshUniversalAnger);
        }

        super.start();
    }

    private List<Mob> getNearbyMobsOfSameType() {
        double var0 = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB var1 = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(var0, 10.0, var0);
        return this.mob.level.getLoadedEntitiesOfClass(this.mob.getClass(), var1);
    }
}
