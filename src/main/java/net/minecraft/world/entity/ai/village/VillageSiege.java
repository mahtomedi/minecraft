package net.minecraft.world.entity.ai.village;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class VillageSiege implements CustomSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean hasSetupSiege;
    private VillageSiege.State siegeState = VillageSiege.State.SIEGE_DONE;
    private int zombiesToSpawn;
    private int nextSpawnTime;
    private int spawnX;
    private int spawnY;
    private int spawnZ;

    @Override
    public int tick(ServerLevel param0, boolean param1, boolean param2) {
        if (!param0.isDay() && param1) {
            float var0 = param0.getTimeOfDay(0.0F);
            if ((double)var0 == 0.5) {
                this.siegeState = param0.random.nextInt(10) == 0 ? VillageSiege.State.SIEGE_TONIGHT : VillageSiege.State.SIEGE_DONE;
            }

            if (this.siegeState == VillageSiege.State.SIEGE_DONE) {
                return 0;
            } else {
                if (!this.hasSetupSiege) {
                    if (!this.tryToSetupSiege(param0)) {
                        return 0;
                    }

                    this.hasSetupSiege = true;
                }

                if (this.nextSpawnTime > 0) {
                    --this.nextSpawnTime;
                    return 0;
                } else {
                    this.nextSpawnTime = 2;
                    if (this.zombiesToSpawn > 0) {
                        this.trySpawn(param0);
                        --this.zombiesToSpawn;
                    } else {
                        this.siegeState = VillageSiege.State.SIEGE_DONE;
                    }

                    return 1;
                }
            }
        } else {
            this.siegeState = VillageSiege.State.SIEGE_DONE;
            this.hasSetupSiege = false;
            return 0;
        }
    }

    private boolean tryToSetupSiege(ServerLevel param0) {
        for(Player var0 : param0.players()) {
            if (!var0.isSpectator()) {
                BlockPos var1 = var0.blockPosition();
                if (param0.isVillage(var1) && !param0.getBiome(var1).is(BiomeTags.WITHOUT_ZOMBIE_SIEGES)) {
                    for(int var2 = 0; var2 < 10; ++var2) {
                        float var3 = param0.random.nextFloat() * (float) (Math.PI * 2);
                        this.spawnX = var1.getX() + Mth.floor(Mth.cos(var3) * 32.0F);
                        this.spawnY = var1.getY();
                        this.spawnZ = var1.getZ() + Mth.floor(Mth.sin(var3) * 32.0F);
                        if (this.findRandomSpawnPos(param0, new BlockPos(this.spawnX, this.spawnY, this.spawnZ)) != null) {
                            this.nextSpawnTime = 0;
                            this.zombiesToSpawn = 20;
                            break;
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private void trySpawn(ServerLevel param0) {
        Vec3 var0 = this.findRandomSpawnPos(param0, new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
        if (var0 != null) {
            Zombie var1;
            try {
                var1 = new Zombie(param0);
                var1.finalizeSpawn(param0, param0.getCurrentDifficultyAt(var1.blockPosition()), MobSpawnType.EVENT, null, null);
            } catch (Exception var5) {
                LOGGER.warn("Failed to create zombie for village siege at {}", var0, var5);
                return;
            }

            var1.moveTo(var0.x, var0.y, var0.z, param0.random.nextFloat() * 360.0F, 0.0F);
            param0.addFreshEntityWithPassengers(var1);
        }
    }

    @Nullable
    private Vec3 findRandomSpawnPos(ServerLevel param0, BlockPos param1) {
        for(int var0 = 0; var0 < 10; ++var0) {
            int var1 = param1.getX() + param0.random.nextInt(16) - 8;
            int var2 = param1.getZ() + param0.random.nextInt(16) - 8;
            int var3 = param0.getHeight(Heightmap.Types.WORLD_SURFACE, var1, var2);
            BlockPos var4 = new BlockPos(var1, var3, var2);
            if (param0.isVillage(var4) && Monster.checkMonsterSpawnRules(EntityType.ZOMBIE, param0, MobSpawnType.EVENT, var4, param0.random)) {
                return Vec3.atBottomCenterOf(var4);
            }
        }

        return null;
    }

    static enum State {
        SIEGE_CAN_ACTIVATE,
        SIEGE_TONIGHT,
        SIEGE_DONE;
    }
}
