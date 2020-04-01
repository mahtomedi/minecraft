package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
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

    public MonsterRoomFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, Function<Random, ? extends NoneFeatureConfiguration> param1) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        int var0 = 3;
        int var1 = param2.nextInt(2) + 2;
        int var2 = -var1 - 1;
        int var3 = var1 + 1;
        int var4 = -1;
        int var5 = 4;
        int var6 = param2.nextInt(2) + 2;
        int var7 = -var6 - 1;
        int var8 = var6 + 1;
        int var9 = 0;

        for(int var10 = var2; var10 <= var3; ++var10) {
            for(int var11 = -1; var11 <= 4; ++var11) {
                for(int var12 = var7; var12 <= var8; ++var12) {
                    BlockPos var13 = param3.offset(var10, var11, var12);
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
                        BlockPos var19 = param3.offset(var16, var17, var18);
                        if (var16 != var2 && var17 != -1 && var18 != var7 && var16 != var3 && var17 != 4 && var18 != var8) {
                            if (param0.getBlockState(var19).getBlock() != Blocks.CHEST) {
                                param0.setBlock(var19, AIR, 2);
                            }
                        } else if (var19.getY() >= 0 && !param0.getBlockState(var19.below()).getMaterial().isSolid()) {
                            param0.setBlock(var19, AIR, 2);
                        } else if (param0.getBlockState(var19).getMaterial().isSolid() && param0.getBlockState(var19).getBlock() != Blocks.CHEST) {
                            if (var17 == -1 && param2.nextInt(4) != 0) {
                                param0.setBlock(var19, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
                            } else {
                                param0.setBlock(var19, Blocks.COBBLESTONE.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }

            for(int var20 = 0; var20 < 2; ++var20) {
                for(int var21 = 0; var21 < 3; ++var21) {
                    int var22 = param3.getX() + param2.nextInt(var1 * 2 + 1) - var1;
                    int var23 = param3.getY();
                    int var24 = param3.getZ() + param2.nextInt(var6 * 2 + 1) - var6;
                    BlockPos var25 = new BlockPos(var22, var23, var24);
                    if (param0.isEmptyBlock(var25)) {
                        int var26 = 0;

                        for(Direction var27 : Direction.Plane.HORIZONTAL) {
                            if (param0.getBlockState(var25.relative(var27)).getMaterial().isSolid()) {
                                ++var26;
                            }
                        }

                        if (var26 == 1) {
                            param0.setBlock(var25, StructurePiece.reorient(param0, var25, Blocks.CHEST.defaultBlockState()), 2);
                            RandomizableContainerBlockEntity.setLootTable(param0, param2, var25, BuiltInLootTables.SIMPLE_DUNGEON);
                            break;
                        }
                    }
                }
            }

            param0.setBlock(param3, Blocks.SPAWNER.defaultBlockState(), 2);
            BlockEntity var28 = param0.getBlockEntity(param3);
            if (var28 instanceof SpawnerBlockEntity) {
                ((SpawnerBlockEntity)var28).getSpawner().setEntityId(this.randomEntityId(param2));
            } else {
                LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", param3.getX(), param3.getY(), param3.getZ());
            }

            return true;
        } else {
            return false;
        }
    }

    private EntityType<?> randomEntityId(Random param0) {
        return MOBS[param0.nextInt(MOBS.length)];
    }
}
