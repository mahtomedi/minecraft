package net.minecraft.world.entity.npc;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;

public class WanderingTraderSpawner implements CustomSpawner {
    private static final int DEFAULT_TICK_DELAY = 1200;
    public static final int DEFAULT_SPAWN_DELAY = 24000;
    private static final int MIN_SPAWN_CHANCE = 25;
    private static final int MAX_SPAWN_CHANCE = 75;
    private static final int SPAWN_CHANCE_INCREASE = 25;
    private static final int SPAWN_ONE_IN_X_CHANCE = 10;
    private static final int NUMBER_OF_SPAWN_ATTEMPTS = 10;
    private final RandomSource random = RandomSource.create();
    private final ServerLevelData serverLevelData;
    private int tickDelay;
    private int spawnDelay;
    private int spawnChance;

    public WanderingTraderSpawner(ServerLevelData param0) {
        this.serverLevelData = param0;
        this.tickDelay = 1200;
        this.spawnDelay = param0.getWanderingTraderSpawnDelay();
        this.spawnChance = param0.getWanderingTraderSpawnChance();
        if (this.spawnDelay == 0 && this.spawnChance == 0) {
            this.spawnDelay = 24000;
            param0.setWanderingTraderSpawnDelay(this.spawnDelay);
            this.spawnChance = 25;
            param0.setWanderingTraderSpawnChance(this.spawnChance);
        }

    }

    @Override
    public int tick(ServerLevel param0, boolean param1, boolean param2) {
        if (!param0.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)) {
            return 0;
        } else if (--this.tickDelay > 0) {
            return 0;
        } else {
            this.tickDelay = 1200;
            this.spawnDelay -= 1200;
            this.serverLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
            if (this.spawnDelay > 0) {
                return 0;
            } else {
                this.spawnDelay = 24000;
                if (!param0.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                    return 0;
                } else {
                    int var0 = this.spawnChance;
                    this.spawnChance = Mth.clamp(this.spawnChance + 25, 25, 75);
                    this.serverLevelData.setWanderingTraderSpawnChance(this.spawnChance);
                    if (this.random.nextInt(100) > var0) {
                        return 0;
                    } else if (this.spawn(param0)) {
                        this.spawnChance = 25;
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }

    private boolean spawn(ServerLevel param0) {
        Player var0 = param0.getRandomPlayer();
        if (var0 == null) {
            return true;
        } else if (this.random.nextInt(10) != 0) {
            return false;
        } else {
            BlockPos var1 = var0.blockPosition();
            int var2 = 48;
            PoiManager var3 = param0.getPoiManager();
            Optional<BlockPos> var4 = var3.find(param0x -> param0x.is(PoiTypes.MEETING), param0x -> true, var1, 48, PoiManager.Occupancy.ANY);
            BlockPos var5 = var4.orElse(var1);
            BlockPos var6 = this.findSpawnPositionNear(param0, var5, 48);
            if (var6 != null && this.hasEnoughSpace(param0, var6)) {
                if (param0.getBiome(var6).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
                    return false;
                }

                WanderingTrader var7 = EntityType.WANDERING_TRADER.spawn(param0, var6, MobSpawnType.EVENT);
                if (var7 != null) {
                    for(int var8 = 0; var8 < 2; ++var8) {
                        this.tryToSpawnLlamaFor(param0, var7, 4);
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

    private void tryToSpawnLlamaFor(ServerLevel param0, WanderingTrader param1, int param2) {
        BlockPos var0 = this.findSpawnPositionNear(param0, param1.blockPosition(), param2);
        if (var0 != null) {
            TraderLlama var1 = EntityType.TRADER_LLAMA.spawn(param0, var0, MobSpawnType.EVENT);
            if (var1 != null) {
                var1.setLeashedTo(param1, true);
            }
        }
    }

    @Nullable
    private BlockPos findSpawnPositionNear(LevelReader param0, BlockPos param1, int param2) {
        BlockPos var0 = null;

        for(int var1 = 0; var1 < 10; ++var1) {
            int var2 = param1.getX() + this.random.nextInt(param2 * 2) - param2;
            int var3 = param1.getZ() + this.random.nextInt(param2 * 2) - param2;
            int var4 = param0.getHeight(Heightmap.Types.WORLD_SURFACE, var2, var3);
            BlockPos var5 = new BlockPos(var2, var4, var3);
            if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, param0, var5, EntityType.WANDERING_TRADER)) {
                var0 = var5;
                break;
            }
        }

        return var0;
    }

    private boolean hasEnoughSpace(BlockGetter param0, BlockPos param1) {
        for(BlockPos var0 : BlockPos.betweenClosed(param1, param1.offset(1, 2, 1))) {
            if (!param0.getBlockState(var0).getCollisionShape(param0, var0).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
