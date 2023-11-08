package net.minecraft.world.level.block.entity.trialspawner;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public enum TrialSpawnerState implements StringRepresentable {
    INACTIVE("inactive", 0, TrialSpawnerState.ParticleEmission.NONE, -1.0, false),
    WAITING_FOR_PLAYERS("waiting_for_players", 4, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, 200.0, true),
    ACTIVE("active", 8, TrialSpawnerState.ParticleEmission.FLAMES_AND_SMOKE, 1000.0, true),
    WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, -1.0, false),
    EJECTING_REWARD("ejecting_reward", 8, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, -1.0, false),
    COOLDOWN("cooldown", 0, TrialSpawnerState.ParticleEmission.SMOKE_INSIDE_AND_TOP_FACE, -1.0, false);

    private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0F;
    private static final int TIME_BETWEEN_EACH_EJECTION = Mth.floor(30.0F);
    private final String name;
    private final int lightLevel;
    private final double spinningMobSpeed;
    private final TrialSpawnerState.ParticleEmission particleEmission;
    private final boolean isCapableOfSpawning;

    private TrialSpawnerState(String param0, int param1, TrialSpawnerState.ParticleEmission param2, double param3, boolean param4) {
        this.name = param0;
        this.lightLevel = param1;
        this.particleEmission = param2;
        this.spinningMobSpeed = param3;
        this.isCapableOfSpawning = param4;
    }

    TrialSpawnerState tickAndGetNext(BlockPos param0, TrialSpawner param1, ServerLevel param2) {
        TrialSpawnerData var0 = param1.getData();
        TrialSpawnerConfig var1 = param1.getConfig();
        PlayerDetector var2 = param1.getPlayerDetector();
        TrialSpawnerState var10000;
        switch(this) {
            case INACTIVE:
                var10000 = var0.getOrCreateDisplayEntity(param1, param2, WAITING_FOR_PLAYERS) == null ? this : WAITING_FOR_PLAYERS;
                break;
            case WAITING_FOR_PLAYERS:
                if (!var0.hasMobToSpawn()) {
                    var10000 = INACTIVE;
                } else {
                    var0.tryDetectPlayers(param2, param0, var2, var1.requiredPlayerRange());
                    var10000 = var0.detectedPlayers.isEmpty() ? this : ACTIVE;
                }
                break;
            case ACTIVE:
                if (!var0.hasMobToSpawn()) {
                    var10000 = INACTIVE;
                } else {
                    int var3 = var0.countAdditionalPlayers(param0);
                    var0.tryDetectPlayers(param2, param0, var2, var1.requiredPlayerRange());
                    if (var0.hasFinishedSpawningAllMobs(var1, var3)) {
                        if (var0.haveAllCurrentMobsDied()) {
                            var0.cooldownEndsAt = param2.getGameTime() + (long)var1.targetCooldownLength();
                            var0.totalMobsSpawned = 0;
                            var0.nextMobSpawnsAt = 0L;
                            var10000 = WAITING_FOR_REWARD_EJECTION;
                            break;
                        }
                    } else if (var0.isReadyToSpawnNextMob(param2, var1, var3)) {
                        param1.spawnMob(param2, param0).ifPresent(param4 -> {
                            var0.currentMobs.add(param4);
                            ++var0.totalMobsSpawned;
                            var0.nextMobSpawnsAt = param2.getGameTime() + (long)var1.ticksBetweenSpawn();
                            var0.spawnPotentials.getRandom(param2.getRandom()).ifPresent(param2x -> {
                                var0.nextSpawnData = Optional.of(param2x.getData());
                                param1.markUpdated();
                            });
                        });
                    }

                    var10000 = this;
                }
                break;
            case WAITING_FOR_REWARD_EJECTION:
                if (var0.isReadyToOpenShutter(param2, var1, 40.0F)) {
                    param2.playSound(null, param0, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS);
                    var10000 = EJECTING_REWARD;
                } else {
                    var10000 = this;
                }
                break;
            case EJECTING_REWARD:
                if (!var0.isReadyToEjectItems(param2, var1, (float)TIME_BETWEEN_EACH_EJECTION)) {
                    var10000 = this;
                } else if (var0.detectedPlayers.isEmpty()) {
                    param2.playSound(null, param0, SoundEvents.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS);
                    var0.ejectingLootTable = Optional.empty();
                    var10000 = COOLDOWN;
                } else {
                    if (var0.ejectingLootTable.isEmpty()) {
                        var0.ejectingLootTable = var1.lootTablesToEject().getRandomValue(param2.getRandom());
                    }

                    var0.ejectingLootTable.ifPresent(param3 -> param1.ejectReward(param2, param0, param3));
                    var0.detectedPlayers.remove(var0.detectedPlayers.iterator().next());
                    var10000 = this;
                }
                break;
            case COOLDOWN:
                if (var0.isCooldownFinished(param2)) {
                    var0.cooldownEndsAt = 0L;
                    var10000 = WAITING_FOR_PLAYERS;
                } else {
                    var10000 = this;
                }
                break;
            default:
                throw new IncompatibleClassChangeError();
        }

        return var10000;
    }

    public int lightLevel() {
        return this.lightLevel;
    }

    public double spinningMobSpeed() {
        return this.spinningMobSpeed;
    }

    public boolean hasSpinningMob() {
        return this.spinningMobSpeed >= 0.0;
    }

    public boolean isCapableOfSpawning() {
        return this.isCapableOfSpawning;
    }

    public void emitParticles(Level param0, BlockPos param1) {
        this.particleEmission.emit(param0, param0.getRandom(), param1);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static class LightLevel {
        private static final int UNLIT = 0;
        private static final int HALF_LIT = 4;
        private static final int LIT = 8;

        private LightLevel() {
        }
    }

    interface ParticleEmission {
        TrialSpawnerState.ParticleEmission NONE = (param0, param1, param2) -> {
        };
        TrialSpawnerState.ParticleEmission SMALL_FLAMES = (param0, param1, param2) -> {
            if (param1.nextInt(2) == 0) {
                Vec3 var0 = param2.getCenter().offsetRandom(param1, 0.9F);
                addParticle(ParticleTypes.SMALL_FLAME, var0, param0);
            }

        };
        TrialSpawnerState.ParticleEmission FLAMES_AND_SMOKE = (param0, param1, param2) -> {
            Vec3 var0 = param2.getCenter().offsetRandom(param1, 1.0F);
            addParticle(ParticleTypes.SMOKE, var0, param0);
            addParticle(ParticleTypes.FLAME, var0, param0);
        };
        TrialSpawnerState.ParticleEmission SMOKE_INSIDE_AND_TOP_FACE = (param0, param1, param2) -> {
            Vec3 var0 = param2.getCenter().offsetRandom(param1, 0.9F);
            if (param1.nextInt(3) == 0) {
                addParticle(ParticleTypes.SMOKE, var0, param0);
            }

            if (param0.getGameTime() % 20L == 0L) {
                Vec3 var1 = param2.getCenter().add(0.0, 0.5, 0.0);
                int var2 = param0.getRandom().nextInt(4) + 20;

                for(int var3 = 0; var3 < var2; ++var3) {
                    addParticle(ParticleTypes.SMOKE, var1, param0);
                }
            }

        };

        private static void addParticle(SimpleParticleType param0, Vec3 param1, Level param2) {
            param2.addParticle(param0, param1.x(), param1.y(), param1.z(), 0.0, 0.0, 0.0);
        }

        void emit(Level var1, RandomSource var2, BlockPos var3);
    }

    static class SpinningMob {
        private static final double NONE = -1.0;
        private static final double SLOW = 200.0;
        private static final double FAST = 1000.0;

        private SpinningMob() {
        }
    }
}
