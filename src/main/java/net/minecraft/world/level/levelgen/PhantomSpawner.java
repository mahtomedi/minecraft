package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class PhantomSpawner implements CustomSpawner {
    private int nextTick;

    @Override
    public int tick(ServerLevel param0, boolean param1, boolean param2) {
        if (!param1) {
            return 0;
        } else if (!param0.getGameRules().getBoolean(GameRules.RULE_DOINSOMNIA)) {
            return 0;
        } else {
            RandomSource var0 = param0.random;
            --this.nextTick;
            if (this.nextTick > 0) {
                return 0;
            } else {
                this.nextTick += (60 + var0.nextInt(60)) * 20;
                if (param0.getSkyDarken() < 5 && param0.dimensionType().hasSkyLight()) {
                    return 0;
                } else {
                    int var1 = 0;

                    for(ServerPlayer var2 : param0.players()) {
                        if (!var2.isSpectator()) {
                            BlockPos var3 = var2.blockPosition();
                            if (!param0.dimensionType().hasSkyLight() || var3.getY() >= param0.getSeaLevel() && param0.canSeeSky(var3)) {
                                DifficultyInstance var4 = param0.getCurrentDifficultyAt(var3);
                                if (var4.isHarderThan(var0.nextFloat() * 3.0F)) {
                                    ServerStatsCounter var5 = var2.getStats();
                                    int var6 = Mth.clamp(var5.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
                                    int var7 = 24000;
                                    if (var0.nextInt(var6) >= 72000) {
                                        BlockPos var8 = var3.above(20 + var0.nextInt(15)).east(-10 + var0.nextInt(21)).south(-10 + var0.nextInt(21));
                                        BlockState var9 = param0.getBlockState(var8);
                                        FluidState var10 = param0.getFluidState(var8);
                                        if (NaturalSpawner.isValidEmptySpawnBlock(param0, var8, var9, var10, EntityType.PHANTOM)) {
                                            SpawnGroupData var11 = null;
                                            int var12 = 1 + var0.nextInt(var4.getDifficulty().getId() + 1);

                                            for(int var13 = 0; var13 < var12; ++var13) {
                                                Phantom var14 = EntityType.PHANTOM.create(param0);
                                                if (var14 != null) {
                                                    var14.moveTo(var8, 0.0F, 0.0F);
                                                    var11 = var14.finalizeSpawn(param0, var4, MobSpawnType.NATURAL, var11, null);
                                                    param0.addFreshEntityWithPassengers(var14);
                                                    ++var1;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return var1;
                }
            }
        }
    }
}
