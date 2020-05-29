package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
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

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, NoneFeatureConfiguration param5
    ) {
        int var0 = 3;
        int var1 = param3.nextInt(2) + 2;
        int var2 = -var1 - 1;
        int var3 = var1 + 1;
        int var4 = -1;
        int var5 = 4;
        int var6 = param3.nextInt(2) + 2;
        int var7 = -var6 - 1;
        int var8 = var6 + 1;
        int var9 = 0;

        for(int var10 = var2; var10 <= var3; ++var10) {
            for(int var11 = -1; var11 <= 4; ++var11) {
                for(int var12 = var7; var12 <= var8; ++var12) {
                    BlockPos var13 = param4.offset(var10, var11, var12);
                    Material var14 = param0.getBlockState(var13).getMaterial();
                    boolean var15 = var14.isSolid();
                    if (var11 == -1 && !var15) {
                        return false;
                    }

                    if (var11 == 4 && !var15) {
                        return false;
                    }

                    if ((var10 == var2 || var10 == var3 || var12 == var7 || var12 == var8)
                        && var11 == 0
                        && param0.isEmptyBlock(var13)
                        && param0.isEmptyBlock(var13.above())) {
                        ++var9;
                    }
                }
            }
        }

        if (var9 >= 1 && var9 <= 5) {
            for(int var16 = var2; var16 <= var3; ++var16) {
                for(int var17 = 3; var17 >= -1; --var17) {
                    for(int var18 = var7; var18 <= var8; ++var18) {
                        BlockPos var19 = param4.offset(var16, var17, var18);
                        BlockState var20 = param0.getBlockState(var19);
                        if (var16 != var2 && var17 != -1 && var18 != var7 && var16 != var3 && var17 != 4 && var18 != var8) {
                            if (!var20.is(Blocks.CHEST) && !var20.is(Blocks.SPAWNER)) {
                                param0.setBlock(var19, AIR, 2);
                            }
                        } else if (var19.getY() >= 0 && !param0.getBlockState(var19.below()).getMaterial().isSolid()) {
                            param0.setBlock(var19, AIR, 2);
                        } else if (var20.getMaterial().isSolid() && !var20.is(Blocks.CHEST)) {
                            if (var17 == -1 && param3.nextInt(4) != 0) {
                                param0.setBlock(var19, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
                            } else {
                                param0.setBlock(var19, Blocks.COBBLESTONE.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }

            for(int var21 = 0; var21 < 2; ++var21) {
                for(int var22 = 0; var22 < 3; ++var22) {
                    int var23 = param4.getX() + param3.nextInt(var1 * 2 + 1) - var1;
                    int var24 = param4.getY();
                    int var25 = param4.getZ() + param3.nextInt(var6 * 2 + 1) - var6;
                    BlockPos var26 = new BlockPos(var23, var24, var25);
                    if (param0.isEmptyBlock(var26)) {
                        int var27 = 0;

                        for(Direction var28 : Direction.Plane.HORIZONTAL) {
                            if (param0.getBlockState(var26.relative(var28)).getMaterial().isSolid()) {
                                ++var27;
                            }
                        }

                        if (var27 == 1) {
                            param0.setBlock(var26, StructurePiece.reorient(param0, var26, Blocks.CHEST.defaultBlockState()), 2);
                            RandomizableContainerBlockEntity.setLootTable(param0, param3, var26, BuiltInLootTables.SIMPLE_DUNGEON);
                            break;
                        }
                    }
                }
            }

            param0.setBlock(param4, Blocks.SPAWNER.defaultBlockState(), 2);
            BlockEntity var29 = param0.getBlockEntity(param4);
            if (var29 instanceof SpawnerBlockEntity) {
                ((SpawnerBlockEntity)var29).getSpawner().setEntityId(this.randomEntityId(param3));
            } else {
                LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", param4.getX(), param4.getY(), param4.getZ());
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
