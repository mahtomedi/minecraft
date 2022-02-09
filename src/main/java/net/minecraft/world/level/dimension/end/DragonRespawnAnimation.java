package net.minecraft.world.level.dimension.end;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;

public enum DragonRespawnAnimation {
    START {
        @Override
        public void tick(ServerLevel param0, EndDragonFight param1, List<EndCrystal> param2, int param3, BlockPos param4) {
            BlockPos var0 = new BlockPos(0, 128, 0);

            for(EndCrystal var1 : param2) {
                var1.setBeamTarget(var0);
            }

            param1.setRespawnStage(PREPARING_TO_SUMMON_PILLARS);
        }
    },
    PREPARING_TO_SUMMON_PILLARS {
        @Override
        public void tick(ServerLevel param0, EndDragonFight param1, List<EndCrystal> param2, int param3, BlockPos param4) {
            if (param3 < 100) {
                if (param3 == 0 || param3 == 50 || param3 == 51 || param3 == 52 || param3 >= 95) {
                    param0.levelEvent(3001, new BlockPos(0, 128, 0), 0);
                }
            } else {
                param1.setRespawnStage(SUMMONING_PILLARS);
            }

        }
    },
    SUMMONING_PILLARS {
        @Override
        public void tick(ServerLevel param0, EndDragonFight param1, List<EndCrystal> param2, int param3, BlockPos param4) {
            int var0 = 40;
            boolean var1 = param3 % 40 == 0;
            boolean var2 = param3 % 40 == 39;
            if (var1 || var2) {
                List<SpikeFeature.EndSpike> var3 = SpikeFeature.getSpikesForLevel(param0);
                int var4 = param3 / 40;
                if (var4 < var3.size()) {
                    SpikeFeature.EndSpike var5 = var3.get(var4);
                    if (var1) {
                        for(EndCrystal var6 : param2) {
                            var6.setBeamTarget(new BlockPos(var5.getCenterX(), var5.getHeight() + 1, var5.getCenterZ()));
                        }
                    } else {
                        int var7 = 10;

                        for(BlockPos var8 : BlockPos.betweenClosed(
                            new BlockPos(var5.getCenterX() - 10, var5.getHeight() - 10, var5.getCenterZ() - 10),
                            new BlockPos(var5.getCenterX() + 10, var5.getHeight() + 10, var5.getCenterZ() + 10)
                        )) {
                            param0.removeBlock(var8, false);
                        }

                        param0.explode(
                            null,
                            (double)((float)var5.getCenterX() + 0.5F),
                            (double)var5.getHeight(),
                            (double)((float)var5.getCenterZ() + 0.5F),
                            5.0F,
                            Explosion.BlockInteraction.DESTROY
                        );
                        SpikeConfiguration var9 = new SpikeConfiguration(true, ImmutableList.of(var5), new BlockPos(0, 128, 0));
                        Feature.END_SPIKE
                            .place(var9, param0, param0.getChunkSource().getGenerator(), new Random(), new BlockPos(var5.getCenterX(), 45, var5.getCenterZ()));
                    }
                } else if (var1) {
                    param1.setRespawnStage(SUMMONING_DRAGON);
                }
            }

        }
    },
    SUMMONING_DRAGON {
        @Override
        public void tick(ServerLevel param0, EndDragonFight param1, List<EndCrystal> param2, int param3, BlockPos param4) {
            if (param3 >= 100) {
                param1.setRespawnStage(END);
                param1.resetSpikeCrystals();

                for(EndCrystal var0 : param2) {
                    var0.setBeamTarget(null);
                    param0.explode(var0, var0.getX(), var0.getY(), var0.getZ(), 6.0F, Explosion.BlockInteraction.NONE);
                    var0.discard();
                }
            } else if (param3 >= 80) {
                param0.levelEvent(3001, new BlockPos(0, 128, 0), 0);
            } else if (param3 == 0) {
                for(EndCrystal var1 : param2) {
                    var1.setBeamTarget(new BlockPos(0, 128, 0));
                }
            } else if (param3 < 5) {
                param0.levelEvent(3001, new BlockPos(0, 128, 0), 0);
            }

        }
    },
    END {
        @Override
        public void tick(ServerLevel param0, EndDragonFight param1, List<EndCrystal> param2, int param3, BlockPos param4) {
        }
    };

    public abstract void tick(ServerLevel var1, EndDragonFight var2, List<EndCrystal> var3, int var4, BlockPos var5);
}
