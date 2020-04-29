package net.minecraft.world.entity.npc;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;

public class WanderingTraderSpawner {
    private final Random random = new Random();
    private final ServerLevel level;
    private final ServerLevelData serverLevelData;
    private int tickDelay;
    private int spawnDelay;
    private int spawnChance;

    public WanderingTraderSpawner(ServerLevel param0, ServerLevelData param1) {
        this.level = param0;
        this.serverLevelData = param1;
        this.tickDelay = 1200;
        this.spawnDelay = param1.getWanderingTraderSpawnDelay();
        this.spawnChance = param1.getWanderingTraderSpawnChance();
        if (this.spawnDelay == 0 && this.spawnChance == 0) {
            this.spawnDelay = 24000;
            param1.setWanderingTraderSpawnDelay(this.spawnDelay);
            this.spawnChance = 25;
            param1.setWanderingTraderSpawnChance(this.spawnChance);
        }

    }

    public void tick() {
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)) {
            if (--this.tickDelay <= 0) {
                this.tickDelay = 1200;
                this.spawnDelay -= 1200;
                this.serverLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
                if (this.spawnDelay <= 0) {
                    this.spawnDelay = 24000;
                    if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        int var0 = this.spawnChance;
                        this.spawnChance = Mth.clamp(this.spawnChance + 25, 25, 75);
                        this.serverLevelData.setWanderingTraderSpawnChance(this.spawnChance);
                        if (this.random.nextInt(100) <= var0) {
                            if (this.spawn()) {
                                this.spawnChance = 25;
                            }

                        }
                    }
                }
            }
        }
    }

    private boolean spawn() {
        Player var0 = this.level.getRandomPlayer();
        if (var0 == null) {
            return true;
        } else if (this.random.nextInt(10) != 0) {
            return false;
        } else {
            BlockPos var1 = var0.blockPosition();
            int var2 = 48;
            PoiManager var3 = this.level.getPoiManager();
            Optional<BlockPos> var4 = var3.find(PoiType.MEETING.getPredicate(), param0 -> true, var1, 48, PoiManager.Occupancy.ANY);
            BlockPos var5 = var4.orElse(var1);
            BlockPos var6 = this.findSpawnPositionNear(var5, 48);
            if (var6 != null && this.hasEnoughSpace(var6)) {
                if (this.level.getBiome(var6) == Biomes.THE_VOID) {
                    return false;
                }

                WanderingTrader var7 = EntityType.WANDERING_TRADER.spawn(this.level, null, null, null, var6, MobSpawnType.EVENT, false, false);
                if (var7 != null) {
                    for(int var8 = 0; var8 < 2; ++var8) {
                        this.tryToSpawnLlamaFor(var7, 4);
                    }

                    this.serverLevelData.setWanderingTraderId(var7.getUUID());
                    var7.setDespawnDelay(48000);
                    var7.setWanderTarget(var5);
                    var7.restrictTo(var5, 16);
                    return true;
                }
            }

            return false;
        }
    }

    private void tryToSpawnLlamaFor(WanderingTrader param0, int param1) {
        BlockPos var0 = this.findSpawnPositionNear(param0.blockPosition(), param1);
        if (var0 != null) {
            TraderLlama var1 = EntityType.TRADER_LLAMA.spawn(this.level, null, null, null, var0, MobSpawnType.EVENT, false, false);
            if (var1 != null) {
                var1.setLeashedTo(param0, true);
            }
        }
    }

    @Nullable
    private BlockPos findSpawnPositionNear(BlockPos param0, int param1) {
        BlockPos var0 = null;

        for(int var1 = 0; var1 < 10; ++var1) {
            int var2 = param0.getX() + this.random.nextInt(param1 * 2) - param1;
            int var3 = param0.getZ() + this.random.nextInt(param1 * 2) - param1;
            int var4 = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, var2, var3);
            BlockPos var5 = new BlockPos(var2, var4, var3);
            if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, this.level, var5, EntityType.WANDERING_TRADER)) {
                var0 = var5;
                break;
            }
        }

        return var0;
    }

    private boolean hasEnoughSpace(BlockPos param0) {
        for(BlockPos var0 : BlockPos.betweenClosed(param0, param0.offset(1, 2, 1))) {
            if (!this.level.getBlockState(var0).getCollisionShape(this.level, var0).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
