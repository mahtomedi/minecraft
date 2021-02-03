package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MonsterRoomFeature extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityType<?>[] MOBS = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public MonsterRoomFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        BlockPos var0 = param0.origin();
        Random var1 = param0.random();
        WorldGenLevel var2 = param0.level();
        int var3 = 3;
        int var4 = var1.nextInt(2) + 2;
        int var5 = -var4 - 1;
        int var6 = var4 + 1;
        int var7 = -1;
        int var8 = 4;
        int var9 = var1.nextInt(2) + 2;
        int var10 = -var9 - 1;
        int var11 = var9 + 1;
        int var12 = 0;

        for(int var13 = var5; var13 <= var6; ++var13) {
            for(int var14 = -1; var14 <= 4; ++var14) {
                for(int var15 = var10; var15 <= var11; ++var15) {
                    BlockPos var16 = var0.offset(var13, var14, var15);
                    Material var17 = var2.getBlockState(var16).getMaterial();
                    boolean var18 = var17.isSolid();
                    if (var14 == -1 && !var18) {
                        return false;
                    }

                    if (var14 == 4 && !var18) {
                        return false;
                    }

                    if ((var13 == var5 || var13 == var6 || var15 == var10 || var15 == var11)
                        && var14 == 0
                        && var2.isEmptyBlock(var16)
                        && var2.isEmptyBlock(var16.above())) {
                        ++var12;
                    }
                }
            }
        }

        if (var12 >= 1 && var12 <= 5) {
            for(int var19 = var5; var19 <= var6; ++var19) {
                for(int var20 = 3; var20 >= -1; --var20) {
                    for(int var21 = var10; var21 <= var11; ++var21) {
                        BlockPos var22 = var0.offset(var19, var20, var21);
                        BlockState var23 = var2.getBlockState(var22);
                        if (var19 == var5 || var20 == -1 || var21 == var10 || var19 == var6 || var20 == 4 || var21 == var11) {
                            if (var22.getY() >= var2.getMinBuildHeight() && !var2.getBlockState(var22.below()).getMaterial().isSolid()) {
                                var2.setBlock(var22, AIR, 2);
                            } else if (var23.getMaterial().isSolid() && !var23.is(Blocks.CHEST)) {
                                if (var20 == -1 && var1.nextInt(4) != 0) {
                                    var2.setBlock(var22, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
                                } else {
                                    var2.setBlock(var22, Blocks.COBBLESTONE.defaultBlockState(), 2);
                                }
                            }
                        } else if (!var23.is(Blocks.CHEST) && !var23.is(Blocks.SPAWNER)) {
                            var2.setBlock(var22, AIR, 2);
                        }
                    }
                }
            }

            for(int var24 = 0; var24 < 2; ++var24) {
                for(int var25 = 0; var25 < 3; ++var25) {
                    int var26 = var0.getX() + var1.nextInt(var4 * 2 + 1) - var4;
                    int var27 = var0.getY();
                    int var28 = var0.getZ() + var1.nextInt(var9 * 2 + 1) - var9;
                    BlockPos var29 = new BlockPos(var26, var27, var28);
                    if (var2.isEmptyBlock(var29)) {
                        int var30 = 0;

                        for(Direction var31 : Direction.Plane.HORIZONTAL) {
                            if (var2.getBlockState(var29.relative(var31)).getMaterial().isSolid()) {
                                ++var30;
                            }
                        }

                        if (var30 == 1) {
                            var2.setBlock(var29, StructurePiece.reorient(var2, var29, Blocks.CHEST.defaultBlockState()), 2);
                            RandomizableContainerBlockEntity.setLootTable(var2, var1, var29, BuiltInLootTables.SIMPLE_DUNGEON);
                            break;
                        }
                    }
                }
            }

            var2.setBlock(var0, Blocks.SPAWNER.defaultBlockState(), 2);
            BlockEntity var32 = var2.getBlockEntity(var0);
            if (var32 instanceof SpawnerBlockEntity) {
                ((SpawnerBlockEntity)var32).getSpawner().setEntityId(this.randomEntityId(var1));
            } else {
                LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", var0.getX(), var0.getY(), var0.getZ());
            }

            return true;
        } else {
            return false;
        }
    }

    private EntityType<?> randomEntityId(Random param0) {
        return Util.getRandom(MOBS, param0);
    }
}
