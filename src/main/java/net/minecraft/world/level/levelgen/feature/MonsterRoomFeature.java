package net.minecraft.world.level.levelgen.feature;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
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
        RandomSource var2 = param0.random();
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
                    boolean var18 = var3.getBlockState(var17).isSolid();
                    if (var15 == -1 && !var18) {
                        return false;
                    }

                    if (var15 == 4 && !var18) {
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
            for(int var19 = var6; var19 <= var7; ++var19) {
                for(int var20 = 3; var20 >= -1; --var20) {
                    for(int var21 = var11; var21 <= var12; ++var21) {
                        BlockPos var22 = var1.offset(var19, var20, var21);
                        BlockState var23 = var3.getBlockState(var22);
                        if (var19 == var6 || var20 == -1 || var21 == var11 || var19 == var7 || var20 == 4 || var21 == var12) {
                            if (var22.getY() >= var3.getMinBuildHeight() && !var3.getBlockState(var22.below()).isSolid()) {
                                var3.setBlock(var22, AIR, 2);
                            } else if (var23.isSolid() && !var23.is(Blocks.CHEST)) {
                                if (var20 == -1 && var2.nextInt(4) != 0) {
                                    this.safeSetBlock(var3, var22, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), var0);
                                } else {
                                    this.safeSetBlock(var3, var22, Blocks.COBBLESTONE.defaultBlockState(), var0);
                                }
                            }
                        } else if (!var23.is(Blocks.CHEST) && !var23.is(Blocks.SPAWNER)) {
                            this.safeSetBlock(var3, var22, AIR, var0);
                        }
                    }
                }
            }

            for(int var24 = 0; var24 < 2; ++var24) {
                for(int var25 = 0; var25 < 3; ++var25) {
                    int var26 = var1.getX() + var2.nextInt(var5 * 2 + 1) - var5;
                    int var27 = var1.getY();
                    int var28 = var1.getZ() + var2.nextInt(var10 * 2 + 1) - var10;
                    BlockPos var29 = new BlockPos(var26, var27, var28);
                    if (var3.isEmptyBlock(var29)) {
                        int var30 = 0;

                        for(Direction var31 : Direction.Plane.HORIZONTAL) {
                            if (var3.getBlockState(var29.relative(var31)).isSolid()) {
                                ++var30;
                            }
                        }

                        if (var30 == 1) {
                            this.safeSetBlock(var3, var29, StructurePiece.reorient(var3, var29, Blocks.CHEST.defaultBlockState()), var0);
                            RandomizableContainer.setBlockEntityLootTable(var3, var2, var29, BuiltInLootTables.SIMPLE_DUNGEON);
                            break;
                        }
                    }
                }
            }

            this.safeSetBlock(var3, var1, Blocks.SPAWNER.defaultBlockState(), var0);
            BlockEntity var32 = var3.getBlockEntity(var1);
            if (var32 instanceof SpawnerBlockEntity var33) {
                var33.setEntityId(this.randomEntityId(var2), var2);
            } else {
                LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", var1.getX(), var1.getY(), var1.getZ());
            }

            return true;
        } else {
            return false;
        }
    }

    private EntityType<?> randomEntityId(RandomSource param0) {
        return Util.getRandom(MOBS, param0);
    }
}
