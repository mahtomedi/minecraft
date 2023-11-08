package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class TrialSpawner {
    public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
    private static final int MAX_MOB_TRACKING_DISTANCE = 47;
    private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(47);
    private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02F;
    private final TrialSpawnerConfig config;
    private final TrialSpawnerData data;
    private final TrialSpawner.StateAccessor stateAccessor;
    private PlayerDetector playerDetector;
    private boolean overridePeacefulAndMobSpawnRule;

    public Codec<TrialSpawner> codec() {
        return RecordCodecBuilder.create(
            param0 -> param0.group(TrialSpawnerConfig.MAP_CODEC.forGetter(TrialSpawner::getConfig), TrialSpawnerData.MAP_CODEC.forGetter(TrialSpawner::getData))
                    .apply(param0, (param0x, param1) -> new TrialSpawner(param0x, param1, this.stateAccessor, this.playerDetector))
        );
    }

    public TrialSpawner(TrialSpawner.StateAccessor param0, PlayerDetector param1) {
        this(TrialSpawnerConfig.DEFAULT, new TrialSpawnerData(), param0, param1);
    }

    public TrialSpawner(TrialSpawnerConfig param0, TrialSpawnerData param1, TrialSpawner.StateAccessor param2, PlayerDetector param3) {
        this.config = param0;
        this.data = param1;
        this.data.setSpawnPotentialsFromConfig(param0);
        this.stateAccessor = param2;
        this.playerDetector = param3;
    }

    public TrialSpawnerConfig getConfig() {
        return this.config;
    }

    public TrialSpawnerData getData() {
        return this.data;
    }

    public TrialSpawnerState getState() {
        return this.stateAccessor.getState();
    }

    public void setState(Level param0, TrialSpawnerState param1) {
        this.stateAccessor.setState(param0, param1);
    }

    public void markUpdated() {
        this.stateAccessor.markUpdated();
    }

    public PlayerDetector getPlayerDetector() {
        return this.playerDetector;
    }

    public boolean canSpawnInLevel(Level param0) {
        if (this.overridePeacefulAndMobSpawnRule) {
            return true;
        } else {
            return param0.getDifficulty() == Difficulty.PEACEFUL ? false : param0.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        }
    }

    public Optional<UUID> spawnMob(ServerLevel param0, BlockPos param1) {
        RandomSource var0 = param0.getRandom();
        SpawnData var1 = this.data.getOrCreateNextSpawnData(this, param0.getRandom());
        CompoundTag var2 = var1.entityToSpawn();
        ListTag var3 = var2.getList("Pos", 6);
        Optional<EntityType<?>> var4 = EntityType.by(var2);
        if (var4.isEmpty()) {
            return Optional.empty();
        } else {
            int var5 = var3.size();
            double var6 = var5 >= 1
                ? var3.getDouble(0)
                : (double)param1.getX() + (var0.nextDouble() - var0.nextDouble()) * (double)this.config.spawnRange() + 0.5;
            double var7 = var5 >= 2 ? var3.getDouble(1) : (double)(param1.getY() + var0.nextInt(3) - 1);
            double var8 = var5 >= 3
                ? var3.getDouble(2)
                : (double)param1.getZ() + (var0.nextDouble() - var0.nextDouble()) * (double)this.config.spawnRange() + 0.5;
            if (!param0.noCollision(var4.get().getAABB(var6, var7, var8))) {
                return Optional.empty();
            } else {
                Vec3 var9 = new Vec3(var6, var7, var8);
                if (!inLineOfSight(param0, param1.getCenter(), var9)) {
                    return Optional.empty();
                } else {
                    BlockPos var10 = BlockPos.containing(var9);
                    if (!SpawnPlacements.checkSpawnRules(var4.get(), param0, MobSpawnType.TRIAL_SPAWNER, var10, param0.getRandom())) {
                        return Optional.empty();
                    } else {
                        Entity var11 = EntityType.loadEntityRecursive(var2, param0, param4 -> {
                            param4.moveTo(var6, var7, var8, var0.nextFloat() * 360.0F, 0.0F);
                            return param4;
                        });
                        if (var11 == null) {
                            return Optional.empty();
                        } else {
                            if (var11 instanceof Mob var12) {
                                if (!var12.checkSpawnObstruction(param0)) {
                                    return Optional.empty();
                                }

                                if (var1.getEntityToSpawn().size() == 1 && var1.getEntityToSpawn().contains("id", 8)) {
                                    var12.finalizeSpawn(param0, param0.getCurrentDifficultyAt(var12.blockPosition()), MobSpawnType.TRIAL_SPAWNER, null, null);
                                    var12.setPersistenceRequired();
                                }
                            }

                            if (!param0.tryAddFreshEntityWithPassengers(var11)) {
                                return Optional.empty();
                            } else {
                                param0.levelEvent(3011, param1, 0);
                                param0.levelEvent(3012, var10, 0);
                                param0.gameEvent(var11, GameEvent.ENTITY_PLACE, var10);
                                return Optional.of(var11.getUUID());
                            }
                        }
                    }
                }
            }
        }
    }

    public void ejectReward(ServerLevel param0, BlockPos param1, ResourceLocation param2) {
        LootTable var0 = param0.getServer().getLootData().getLootTable(param2);
        LootParams var1 = new LootParams.Builder(param0).create(LootContextParamSets.EMPTY);
        ObjectArrayList<ItemStack> var2 = var0.getRandomItems(var1);
        if (!var2.isEmpty()) {
            for(ItemStack var3 : var2) {
                DefaultDispenseItemBehavior.spawnItem(param0, var3, 2, Direction.UP, Vec3.atBottomCenterOf(param1).relative(Direction.UP, 1.2));
            }

            param0.levelEvent(3014, param1, 0);
        }

    }

    public void tickClient(Level param0, BlockPos param1) {
        if (!this.canSpawnInLevel(param0)) {
            this.data.oSpin = this.data.spin;
        } else {
            TrialSpawnerState var0 = this.getState();
            var0.emitParticles(param0, param1);
            if (var0.hasSpinningMob()) {
                double var1 = (double)Math.max(0L, this.data.nextMobSpawnsAt - param0.getGameTime());
                this.data.oSpin = this.data.spin;
                this.data.spin = (this.data.spin + var0.spinningMobSpeed() / (var1 + 200.0)) % 360.0;
            }

            if (var0.isCapableOfSpawning()) {
                RandomSource var2 = param0.getRandom();
                if (var2.nextFloat() <= 0.02F) {
                    param0.playLocalSound(
                        param1, SoundEvents.TRIAL_SPAWNER_AMBIENT, SoundSource.BLOCKS, var2.nextFloat() * 0.25F + 0.75F, var2.nextFloat() + 0.5F, false
                    );
                }
            }

        }
    }

    public void tickServer(ServerLevel param0, BlockPos param1) {
        TrialSpawnerState var0 = this.getState();
        if (!this.canSpawnInLevel(param0)) {
            if (var0.isCapableOfSpawning()) {
                this.data.reset();
                this.setState(param0, TrialSpawnerState.INACTIVE);
            }

        } else {
            if (this.data.currentMobs.removeIf(param2 -> shouldMobBeUntracked(param0, param1, param2))) {
                this.data.nextMobSpawnsAt = param0.getGameTime() + (long)this.config.ticksBetweenSpawn();
            }

            TrialSpawnerState var1 = var0.tickAndGetNext(param1, this, param0);
            if (var1 != var0) {
                this.setState(param0, var1);
            }

        }
    }

    private static boolean shouldMobBeUntracked(ServerLevel param0, BlockPos param1, UUID param2) {
        Entity var0 = param0.getEntity(param2);
        return var0 == null
            || !var0.isAlive()
            || !var0.level().dimension().equals(param0.dimension())
            || var0.blockPosition().distSqr(param1) > (double)MAX_MOB_TRACKING_DISTANCE_SQR;
    }

    private static boolean inLineOfSight(Level param0, Vec3 param1, Vec3 param2) {
        BlockHitResult var0 = param0.clip(new ClipContext(param2, param1, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return var0.getBlockPos().equals(BlockPos.containing(param1)) || var0.getType() == HitResult.Type.MISS;
    }

    public static void addSpawnParticles(Level param0, BlockPos param1, RandomSource param2) {
        for(int var0 = 0; var0 < 20; ++var0) {
            double var1 = (double)param1.getX() + 0.5 + (param2.nextDouble() - 0.5) * 2.0;
            double var2 = (double)param1.getY() + 0.5 + (param2.nextDouble() - 0.5) * 2.0;
            double var3 = (double)param1.getZ() + 0.5 + (param2.nextDouble() - 0.5) * 2.0;
            param0.addParticle(ParticleTypes.SMOKE, var1, var2, var3, 0.0, 0.0, 0.0);
            param0.addParticle(ParticleTypes.FLAME, var1, var2, var3, 0.0, 0.0, 0.0);
        }

    }

    public static void addDetectPlayerParticles(Level param0, BlockPos param1, RandomSource param2, int param3) {
        for(int var0 = 0; var0 < 30 + Math.min(param3, 10) * 5; ++var0) {
            double var1 = (double)(2.0F * param2.nextFloat() - 1.0F) * 0.65;
            double var2 = (double)(2.0F * param2.nextFloat() - 1.0F) * 0.65;
            double var3 = (double)param1.getX() + 0.5 + var1;
            double var4 = (double)param1.getY() + 0.1 + (double)param2.nextFloat() * 0.8;
            double var5 = (double)param1.getZ() + 0.5 + var2;
            param0.addParticle(ParticleTypes.TRIAL_SPAWNER_DETECTION, var3, var4, var5, 0.0, 0.0, 0.0);
        }

    }

    public static void addEjectItemParticles(Level param0, BlockPos param1, RandomSource param2) {
        for(int var0 = 0; var0 < 20; ++var0) {
            double var1 = (double)param1.getX() + 0.4 + param2.nextDouble() * 0.2;
            double var2 = (double)param1.getY() + 0.4 + param2.nextDouble() * 0.2;
            double var3 = (double)param1.getZ() + 0.4 + param2.nextDouble() * 0.2;
            double var4 = param2.nextGaussian() * 0.02;
            double var5 = param2.nextGaussian() * 0.02;
            double var6 = param2.nextGaussian() * 0.02;
            param0.addParticle(ParticleTypes.SMALL_FLAME, var1, var2, var3, var4, var5, var6 * 0.25);
            param0.addParticle(ParticleTypes.SMOKE, var1, var2, var3, var4, var5, var6);
        }

    }

    @Deprecated(
        forRemoval = true
    )
    @VisibleForTesting
    public void setPlayerDetector(PlayerDetector param0) {
        this.playerDetector = param0;
    }

    @Deprecated(
        forRemoval = true
    )
    @VisibleForTesting
    public void overridePeacefulAndMobSpawnRule() {
        this.overridePeacefulAndMobSpawnRule = true;
    }

    public interface StateAccessor {
        void setState(Level var1, TrialSpawnerState var2);

        TrialSpawnerState getState();

        void markUpdated();
    }
}
