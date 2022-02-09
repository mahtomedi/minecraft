package net.minecraft.world.level.levelgen.feature;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
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
import org.slf4j.Logger;

public class MonsterRoomFeature extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityType<?>[] MOBS = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public MonsterRoomFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        Predicate<BlockState> var0 = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos var1 = param0.origin();
        Random var2 = param0.random();
        WorldGenLevel var3 = param0.level();
        int var4 = 3;
        int var5 = var2.nextInt(2) + 2;
        int var6 = -var5 - 1;
        int var7 = var5 + 1;
        int var8 = -1;
        int var9 = 4;
        int var10 = var2.nextInt(2) + 2;
        int var11 = -var10 - 1;
        int var12 = var10 + 1;
        int var13 = 0;

        for(int var14 = var6; var14 <= var7; ++var14) {
            for(int var15 = -1; var15 <= 4; ++var15) {
                for(int var16 = var11; var16 <= var12; ++var16) {
                    BlockPos var17 = var1.offset(var14, var15, var16);
                    Material var18 = var3.getBlockState(var17).getMaterial();
                    boolean var19 = var18.isSolid();
                    if (var15 == -1 && !var19) {
                        return false;
                    }

                    if (var15 == 4 && !var19) {
                        return false;
                    }

                    if ((var14 == var6 || var14 == var7 || var16 == var11 || var16 == var12)
                        && var15 == 0
                        && var3.isEmptyBlock(var17)
                        && var3.isEmptyBlock(var17.above())) {
                        ++var13;
                    }
                }
            }
        }

        if (var13 >= 1 && var13 <= 5) {
            for(int var20 = var6; var20 <= var7; ++var20) {
                for(int var21 = 3; var21 >= -1; --var21) {
                    for(int var22 = var11; var22 <= var12; ++var22) {
                        BlockPos var23 = var1.offset(var20, var21, var22);
                        BlockState var24 = var3.getBlockState(var23);
                        if (var20 == var6 || var21 == -1 || var22 == var11 || var20 == var7 || var21 == 4 || var22 == var12) {
                            if (var23.getY() >= var3.getMinBuildHeight() && !var3.getBlockState(var23.below()).getMaterial().isSolid()) {
                                var3.setBlock(var23, AIR, 2);
                            } else if (var24.getMaterial().isSolid() && !var24.is(Blocks.CHEST)) {
                                if (var21 == -1 && var2.nextInt(4) != 0) {
                                    this.safeSetBlock(var3, var23, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), var0);
                                } else {
                                    this.safeSetBlock(var3, var23, Blocks.COBBLESTONE.defaultBlockState(), var0);
                                }
                            }
                        } else if (!var24.is(Blocks.CHEST) && !var24.is(Blocks.SPAWNER)) {
                            this.safeSetBlock(var3, var23, AIR, var0);
                        }
                    }
                }
            }

            for(int var25 = 0; var25 < 2; ++var25) {
                for(int var26 = 0; var26 < 3; ++var26) {
                    int var27 = var1.getX() + var2.nextInt(var5 * 2 + 1) - var5;
                    int var28 = var1.getY();
                    int var29 = var1.getZ() + var2.nextInt(var10 * 2 + 1) - var10;
                    BlockPos var30 = new BlockPos(var27, var28, var29);
                    if (var3.isEmptyBlock(var30)) {
                        int var31 = 0;

                        for(Direction var32 : Direction.Plane.HORIZONTAL) {
                            if (var3.getBlockState(var30.relative(var32)).getMaterial().isSolid()) {
                                ++var31;
                            }
                        }

                        if (var31 == 1) {
                            this.safeSetBlock(var3, var30, StructurePiece.reorient(var3, var30, Blocks.CHEST.defaultBlockState()), var0);
                            RandomizableContainerBlockEntity.setLootTable(var3, var2, var30, BuiltInLootTables.SIMPLE_DUNGEON);
                            break;
                        }
                    }
                }
            }

            this.safeSetBlock(var3, var1, Blocks.SPAWNER.defaultBlockState(), var0);
            BlockEntity var33 = var3.getBlockEntity(var1);
            if (var33 instanceof SpawnerBlockEntity) {
                ((SpawnerBlockEntity)var33).getSpawner().setEntityId(this.randomEntityId(var2));
            } else {
                LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", var1.getX(), var1.getY(), var1.getZ());
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
