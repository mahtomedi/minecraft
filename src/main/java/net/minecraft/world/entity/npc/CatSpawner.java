package net.minecraft.world.entity.npc;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.phys.AABB;

public class CatSpawner implements CustomSpawner {
    private static final int TICK_DELAY = 1200;
    private int nextTick;

    @Override
    public int tick(ServerLevel param0, boolean param1, boolean param2) {
        if (param2 && param0.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            --this.nextTick;
            if (this.nextTick > 0) {
                return 0;
            } else {
                this.nextTick = 1200;
                Player var0 = param0.getRandomPlayer();
                if (var0 == null) {
                    return 0;
                } else {
                    RandomSource var1 = param0.random;
                    int var2 = (8 + var1.nextInt(24)) * (var1.nextBoolean() ? -1 : 1);
                    int var3 = (8 + var1.nextInt(24)) * (var1.nextBoolean() ? -1 : 1);
                    BlockPos var4 = var0.blockPosition().offset(var2, 0, var3);
                    int var5 = 10;
                    if (!param0.hasChunksAt(var4.getX() - 10, var4.getZ() - 10, var4.getX() + 10, var4.getZ() + 10)) {
                        return 0;
                    } else {
                        if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, param0, var4, EntityType.CAT)) {
                            if (param0.isCloseToVillage(var4, 2)) {
                                return this.spawnInVillage(param0, var4);
                            }

                            if (param0.structureManager().getStructureWithPieceAt(var4, StructureTags.CATS_SPAWN_IN).isValid()) {
                                return this.spawnInHut(param0, var4);
                            }
                        }

                        return 0;
                    }
                }
            }
        } else {
            return 0;
        }
    }

    private int spawnInVillage(ServerLevel param0, BlockPos param1) {
        int var0 = 48;
        if (param0.getPoiManager().getCountInRange(param0x -> param0x.is(PoiTypes.HOME), param1, 48, PoiManager.Occupancy.IS_OCCUPIED) > 4L) {
            List<Cat> var1 = param0.getEntitiesOfClass(Cat.class, new AABB(param1).inflate(48.0, 8.0, 48.0));
            if (var1.size() < 5) {
                return this.spawnCat(param1, param0);
            }
        }

        return 0;
    }

    private int spawnInHut(ServerLevel param0, BlockPos param1) {
        int var0 = 16;
        List<Cat> var1 = param0.getEntitiesOfClass(Cat.class, new AABB(param1).inflate(16.0, 8.0, 16.0));
        return var1.size() < 1 ? this.spawnCat(param1, param0) : 0;
    }

    private int spawnCat(BlockPos param0, ServerLevel param1) {
        Cat var0 = EntityType.CAT.create(param1);
        if (var0 == null) {
            return 0;
        } else {
            var0.finalizeSpawn(param1, param1.getCurrentDifficultyAt(param0), MobSpawnType.NATURAL, null, null);
            var0.moveTo(param0, 0.0F, 0.0F);
            param1.addFreshEntityWithPassengers(var0);
            return 1;
        }
    }
}
