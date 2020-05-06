package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class PatrolSpawner {
    private int nextTick;

    public int tick(ServerLevel param0, boolean param1, boolean param2) {
        if (!param1) {
            return 0;
        } else if (!param0.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
            return 0;
        } else {
            Random var0 = param0.random;
            --this.nextTick;
            if (this.nextTick > 0) {
                return 0;
            } else {
                this.nextTick += 12000 + var0.nextInt(1200);
                long var1 = param0.getDayTime() / 24000L;
                if (var1 < 5L || !param0.isDay()) {
                    return 0;
                } else if (var0.nextInt(5) != 0) {
                    return 0;
                } else {
                    int var2 = param0.players().size();
                    if (var2 < 1) {
                        return 0;
                    } else {
                        Player var3 = param0.players().get(var0.nextInt(var2));
                        if (var3.isSpectator()) {
                            return 0;
                        } else if (param0.isCloseToVillage(var3.blockPosition(), 2)) {
                            return 0;
                        } else {
                            int var4 = (24 + var0.nextInt(24)) * (var0.nextBoolean() ? -1 : 1);
                            int var5 = (24 + var0.nextInt(24)) * (var0.nextBoolean() ? -1 : 1);
                            BlockPos.MutableBlockPos var6 = var3.blockPosition().mutable().move(var4, 0, var5);
                            if (!param0.hasChunksAt(var6.getX() - 10, var6.getY() - 10, var6.getZ() - 10, var6.getX() + 10, var6.getY() + 10, var6.getZ() + 10)
                                )
                             {
                                return 0;
                            } else {
                                Biome var7 = param0.getBiome(var6);
                                Biome.BiomeCategory var8 = var7.getBiomeCategory();
                                if (var8 == Biome.BiomeCategory.MUSHROOM) {
                                    return 0;
                                } else {
                                    int var9 = 0;
                                    int var10 = (int)Math.ceil((double)param0.getCurrentDifficultyAt(var6).getEffectiveDifficulty()) + 1;

                                    for(int var11 = 0; var11 < var10; ++var11) {
                                        ++var9;
                                        var6.setY(param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, var6).getY());
                                        if (var11 == 0) {
                                            if (!this.spawnPatrolMember(param0, var6, var0, true)) {
                                                break;
                                            }
                                        } else {
                                            this.spawnPatrolMember(param0, var6, var0, false);
                                        }

                                        var6.setX(var6.getX() + var0.nextInt(5) - var0.nextInt(5));
                                        var6.setZ(var6.getZ() + var0.nextInt(5) - var0.nextInt(5));
                                    }

                                    return var9;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean spawnPatrolMember(Level param0, BlockPos param1, Random param2, boolean param3) {
        BlockState var0 = param0.getBlockState(param1);
        if (!NaturalSpawner.isValidEmptySpawnBlock(param0, param1, var0, var0.getFluidState())) {
            return false;
        } else if (!PatrollingMonster.checkPatrollingMonsterSpawnRules(EntityType.PILLAGER, param0, MobSpawnType.PATROL, param1, param2)) {
            return false;
        } else {
            PatrollingMonster var1 = EntityType.PILLAGER.create(param0);
            if (var1 != null) {
                if (param3) {
                    var1.setPatrolLeader(true);
                    var1.findPatrolTarget();
                }

                var1.setPos((double)param1.getX(), (double)param1.getY(), (double)param1.getZ());
                var1.finalizeSpawn(param0, param0.getCurrentDifficultyAt(param1), MobSpawnType.PATROL, null, null);
                param0.addFreshEntity(var1);
                return true;
            } else {
                return false;
            }
        }
    }
}
