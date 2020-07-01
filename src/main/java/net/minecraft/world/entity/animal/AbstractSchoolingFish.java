package net.minecraft.world.entity.animal;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class AbstractSchoolingFish extends AbstractFish {
    private AbstractSchoolingFish leader;
    private int schoolSize = 1;

    public AbstractSchoolingFish(EntityType<? extends AbstractSchoolingFish> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(5, new FollowFlockLeaderGoal(this));
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return this.getMaxSchoolSize();
    }

    public int getMaxSchoolSize() {
        return super.getMaxSpawnClusterSize();
    }

    @Override
    protected boolean canRandomSwim() {
        return !this.isFollower();
    }

    public boolean isFollower() {
        return this.leader != null && this.leader.isAlive();
    }

    public AbstractSchoolingFish startFollowing(AbstractSchoolingFish param0) {
        this.leader = param0;
        param0.addFollower();
        return param0;
    }

    public void stopFollowing() {
        this.leader.removeFollower();
        this.leader = null;
    }

    private void addFollower() {
        ++this.schoolSize;
    }

    private void removeFollower() {
        --this.schoolSize;
    }

    public boolean canBeFollowed() {
        return this.hasFollowers() && this.schoolSize < this.getMaxSchoolSize();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hasFollowers() && this.level.random.nextInt(200) == 1) {
            List<AbstractFish> var0 = this.level.getEntitiesOfClass(this.getClass(), this.getBoundingBox().inflate(8.0, 8.0, 8.0));
            if (var0.size() <= 1) {
                this.schoolSize = 1;
            }
        }

    }

    public boolean hasFollowers() {
        return this.schoolSize > 1;
    }

    public boolean inRangeOfLeader() {
        return this.distanceToSqr(this.leader) <= 121.0;
    }

    public void pathToLeader() {
        if (this.isFollower()) {
            this.getNavigation().moveTo(this.leader, 1.0);
        }

    }

    public void addFollowers(Stream<AbstractSchoolingFish> param0) {
        param0.limit((long)(this.getMaxSchoolSize() - this.schoolSize)).filter(param0x -> param0x != this).forEach(param0x -> param0x.startFollowing(this));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        super.finalizeSpawn(param0, param1, param2, param3, param4);
        if (param3 == null) {
            param3 = new AbstractSchoolingFish.SchoolSpawnGroupData(this);
        } else {
            this.startFollowing(((AbstractSchoolingFish.SchoolSpawnGroupData)param3).leader);
        }

        return param3;
    }

    public static class SchoolSpawnGroupData implements SpawnGroupData {
        public final AbstractSchoolingFish leader;

        public SchoolSpawnGroupData(AbstractSchoolingFish param0) {
            this.leader = param0;
        }
    }
}
