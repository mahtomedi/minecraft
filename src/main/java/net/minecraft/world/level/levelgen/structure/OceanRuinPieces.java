package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class OceanRuinPieces {
    private static final ResourceLocation[] WARM_RUINS = new ResourceLocation[]{
        new ResourceLocation("underwater_ruin/warm_1"),
        new ResourceLocation("underwater_ruin/warm_2"),
        new ResourceLocation("underwater_ruin/warm_3"),
        new ResourceLocation("underwater_ruin/warm_4"),
        new ResourceLocation("underwater_ruin/warm_5"),
        new ResourceLocation("underwater_ruin/warm_6"),
        new ResourceLocation("underwater_ruin/warm_7"),
        new ResourceLocation("underwater_ruin/warm_8")
    };
    private static final ResourceLocation[] RUINS_BRICK = new ResourceLocation[]{
        new ResourceLocation("underwater_ruin/brick_1"),
        new ResourceLocation("underwater_ruin/brick_2"),
        new ResourceLocation("underwater_ruin/brick_3"),
        new ResourceLocation("underwater_ruin/brick_4"),
        new ResourceLocation("underwater_ruin/brick_5"),
        new ResourceLocation("underwater_ruin/brick_6"),
        new ResourceLocation("underwater_ruin/brick_7"),
        new ResourceLocation("underwater_ruin/brick_8")
    };
    private static final ResourceLocation[] RUINS_CRACKED = new ResourceLocation[]{
        new ResourceLocation("underwater_ruin/cracked_1"),
        new ResourceLocation("underwater_ruin/cracked_2"),
        new ResourceLocation("underwater_ruin/cracked_3"),
        new ResourceLocation("underwater_ruin/cracked_4"),
        new ResourceLocation("underwater_ruin/cracked_5"),
        new ResourceLocation("underwater_ruin/cracked_6"),
        new ResourceLocation("underwater_ruin/cracked_7"),
        new ResourceLocation("underwater_ruin/cracked_8")
    };
    private static final ResourceLocation[] RUINS_MOSSY = new ResourceLocation[]{
        new ResourceLocation("underwater_ruin/mossy_1"),
        new ResourceLocation("underwater_ruin/mossy_2"),
        new ResourceLocation("underwater_ruin/mossy_3"),
        new ResourceLocation("underwater_ruin/mossy_4"),
        new ResourceLocation("underwater_ruin/mossy_5"),
        new ResourceLocation("underwater_ruin/mossy_6"),
        new ResourceLocation("underwater_ruin/mossy_7"),
        new ResourceLocation("underwater_ruin/mossy_8")
    };
    private static final ResourceLocation[] BIG_RUINS_BRICK = new ResourceLocation[]{
        new ResourceLocation("underwater_ruin/big_brick_1"),
        new ResourceLocation("underwater_ruin/big_brick_2"),
        new ResourceLocation("underwater_ruin/big_brick_3"),
        new ResourceLocation("underwater_ruin/big_brick_8")
    };
    private static final ResourceLocation[] BIG_RUINS_MOSSY = new ResourceLocation[]{
        new ResourceLocation("underwater_ruin/big_mossy_1"),
        new ResourceLocation("underwater_ruin/big_mossy_2"),
        new ResourceLocation("underwater_ruin/big_mossy_3"),
        new ResourceLocation("underwater_ruin/big_mossy_8")
    };
    private static final ResourceLocation[] BIG_RUINS_CRACKED = new ResourceLocation[]{
        new ResourceLocation("underwater_ruin/big_cracked_1"),
        new ResourceLocation("underwater_ruin/big_cracked_2"),
        new ResourceLocation("underwater_ruin/big_cracked_3"),
        new ResourceLocation("underwater_ruin/big_cracked_8")
    };
    private static final ResourceLocation[] BIG_WARM_RUINS = new ResourceLocation[]{
        new ResourceLocation("underwater_ruin/big_warm_4"),
        new ResourceLocation("underwater_ruin/big_warm_5"),
        new ResourceLocation("underwater_ruin/big_warm_6"),
        new ResourceLocation("underwater_ruin/big_warm_7")
    };

    private static ResourceLocation getSmallWarmRuin(Random param0) {
        return Util.getRandom(WARM_RUINS, param0);
    }

    private static ResourceLocation getBigWarmRuin(Random param0) {
        return Util.getRandom(BIG_WARM_RUINS, param0);
    }

    public static void addPieces(
        StructureManager param0, BlockPos param1, Rotation param2, List<StructurePiece> param3, Random param4, OceanRuinConfiguration param5
    ) {
        boolean var0 = param4.nextFloat() <= param5.largeProbability;
        float var1 = var0 ? 0.9F : 0.8F;
        addPiece(param0, param1, param2, param3, param4, param5, var0, var1);
        if (var0 && param4.nextFloat() <= param5.clusterProbability) {
            addClusterRuins(param0, param4, param2, param1, param5, param3);
        }

    }

    private static void addClusterRuins(
        StructureManager param0, Random param1, Rotation param2, BlockPos param3, OceanRuinConfiguration param4, List<StructurePiece> param5
    ) {
        int var0 = param3.getX();
        int var1 = param3.getZ();
        BlockPos var2 = StructureTemplate.transform(new BlockPos(15, 0, 15), Mirror.NONE, param2, BlockPos.ZERO).offset(var0, 0, var1);
        BoundingBox var3 = BoundingBox.createProper(var0, 0, var1, var2.getX(), 0, var2.getZ());
        BlockPos var4 = new BlockPos(Math.min(var0, var2.getX()), 0, Math.min(var1, var2.getZ()));
        List<BlockPos> var5 = allPositions(param1, var4.getX(), var4.getZ());
        int var6 = Mth.nextInt(param1, 4, 8);

        for(int var7 = 0; var7 < var6; ++var7) {
            if (!var5.isEmpty()) {
                int var8 = param1.nextInt(var5.size());
                BlockPos var9 = var5.remove(var8);
                int var10 = var9.getX();
                int var11 = var9.getZ();
                Rotation var12 = Rotation.getRandom(param1);
                BlockPos var13 = StructureTemplate.transform(new BlockPos(5, 0, 6), Mirror.NONE, var12, BlockPos.ZERO).offset(var10, 0, var11);
                BoundingBox var14 = BoundingBox.createProper(var10, 0, var11, var13.getX(), 0, var13.getZ());
                if (!var14.intersects(var3)) {
                    addPiece(param0, var9, var12, param5, param1, param4, false, 0.8F);
                }
            }
        }

    }

    private static List<BlockPos> allPositions(Random param0, int param1, int param2) {
        List<BlockPos> var0 = Lists.newArrayList();
        var0.add(new BlockPos(param1 - 16 + Mth.nextInt(param0, 1, 8), 90, param2 + 16 + Mth.nextInt(param0, 1, 7)));
        var0.add(new BlockPos(param1 - 16 + Mth.nextInt(param0, 1, 8), 90, param2 + Mth.nextInt(param0, 1, 7)));
        var0.add(new BlockPos(param1 - 16 + Mth.nextInt(param0, 1, 8), 90, param2 - 16 + Mth.nextInt(param0, 4, 8)));
        var0.add(new BlockPos(param1 + Mth.nextInt(param0, 1, 7), 90, param2 + 16 + Mth.nextInt(param0, 1, 7)));
        var0.add(new BlockPos(param1 + Mth.nextInt(param0, 1, 7), 90, param2 - 16 + Mth.nextInt(param0, 4, 6)));
        var0.add(new BlockPos(param1 + 16 + Mth.nextInt(param0, 1, 7), 90, param2 + 16 + Mth.nextInt(param0, 3, 8)));
        var0.add(new BlockPos(param1 + 16 + Mth.nextInt(param0, 1, 7), 90, param2 + Mth.nextInt(param0, 1, 7)));
        var0.add(new BlockPos(param1 + 16 + Mth.nextInt(param0, 1, 7), 90, param2 - 16 + Mth.nextInt(param0, 4, 8)));
        return var0;
    }

    private static void addPiece(
        StructureManager param0,
        BlockPos param1,
        Rotation param2,
        List<StructurePiece> param3,
        Random param4,
        OceanRuinConfiguration param5,
        boolean param6,
        float param7
    ) {
        if (param5.biomeTemp == OceanRuinFeature.Type.WARM) {
            ResourceLocation var0 = param6 ? getBigWarmRuin(param4) : getSmallWarmRuin(param4);
            param3.add(new OceanRuinPieces.OceanRuinPiece(param0, var0, param1, param2, param7, param5.biomeTemp, param6));
        } else if (param5.biomeTemp == OceanRuinFeature.Type.COLD) {
            ResourceLocation[] var1 = param6 ? BIG_RUINS_BRICK : RUINS_BRICK;
            ResourceLocation[] var2 = param6 ? BIG_RUINS_CRACKED : RUINS_CRACKED;
            ResourceLocation[] var3 = param6 ? BIG_RUINS_MOSSY : RUINS_MOSSY;
            int var4 = param4.nextInt(var1.length);
            param3.add(new OceanRuinPieces.OceanRuinPiece(param0, var1[var4], param1, param2, param7, param5.biomeTemp, param6));
            param3.add(new OceanRuinPieces.OceanRuinPiece(param0, var2[var4], param1, param2, 0.7F, param5.biomeTemp, param6));
            param3.add(new OceanRuinPieces.OceanRuinPiece(param0, var3[var4], param1, param2, 0.5F, param5.biomeTemp, param6));
        }

    }

    public static class OceanRuinPiece extends TemplateStructurePiece {
        private final OceanRuinFeature.Type biomeType;
        private final float integrity;
        private final ResourceLocation templateLocation;
        private final Rotation rotation;
        private final boolean isLarge;

        public OceanRuinPiece(
            StructureManager param0, ResourceLocation param1, BlockPos param2, Rotation param3, float param4, OceanRuinFeature.Type param5, boolean param6
        ) {
            super(StructurePieceType.OCEAN_RUIN, 0);
            this.templateLocation = param1;
            this.templatePosition = param2;
            this.rotation = param3;
            this.integrity = param4;
            this.biomeType = param5;
            this.isLarge = param6;
            this.loadTemplate(param0);
        }

        public OceanRuinPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.OCEAN_RUIN, param1);
            this.templateLocation = new ResourceLocation(param1.getString("Template"));
            this.rotation = Rotation.valueOf(param1.getString("Rot"));
            this.integrity = param1.getFloat("Integrity");
            this.biomeType = OceanRuinFeature.Type.valueOf(param1.getString("BiomeType"));
            this.isLarge = param1.getBoolean("IsLarge");
            this.loadTemplate(param0);
        }

        private void loadTemplate(StructureManager param0) {
            StructureTemplate var0 = param0.getOrCreate(this.templateLocation);
            StructurePlaceSettings var1 = new StructurePlaceSettings()
                .setRotation(this.rotation)
                .setMirror(Mirror.NONE)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
            this.setup(var0, this.templatePosition, var1);
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.putString("Template", this.templateLocation.toString());
            param0.putString("Rot", this.rotation.name());
            param0.putFloat("Integrity", this.integrity);
            param0.putString("BiomeType", this.biomeType.toString());
            param0.putBoolean("IsLarge", this.isLarge);
        }

        @Override
        protected void handleDataMarker(String param0, BlockPos param1, LevelAccessor param2, Random param3, BoundingBox param4) {
            if ("chest".equals(param0)) {
                param2.setBlock(
                    param1,
                    Blocks.CHEST.defaultBlockState().setValue(ChestBlock.WATERLOGGED, Boolean.valueOf(param2.getFluidState(param1).is(FluidTags.WATER))),
                    2
                );
                BlockEntity var0 = param2.getBlockEntity(param1);
                if (var0 instanceof ChestBlockEntity) {
                    ((ChestBlockEntity)var0)
                        .setLootTable(this.isLarge ? BuiltInLootTables.UNDERWATER_RUIN_BIG : BuiltInLootTables.UNDERWATER_RUIN_SMALL, param3.nextLong());
                }
            } else if ("drowned".equals(param0)) {
                Drowned var1 = EntityType.DROWNED.create(param2.getLevel());
                var1.setPersistenceRequired();
                var1.moveTo(param1, 0.0F, 0.0F);
                var1.finalizeSpawn(param2, param2.getCurrentDifficultyAt(param1), MobSpawnType.STRUCTURE, null, null);
                param2.addFreshEntity(var1);
                if (param1.getY() > param2.getSeaLevel()) {
                    param2.setBlock(param1, Blocks.AIR.defaultBlockState(), 2);
                } else {
                    param2.setBlock(param1, Blocks.WATER.defaultBlockState(), 2);
                }
            }

        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.placeSettings.clearProcessors().addProcessor(new BlockRotProcessor(this.integrity)).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
            int var0 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.templatePosition.getX(), this.templatePosition.getZ());
            this.templatePosition = new BlockPos(this.templatePosition.getX(), var0, this.templatePosition.getZ());
            BlockPos var1 = StructureTemplate.transform(
                    new BlockPos(this.template.getSize().getX() - 1, 0, this.template.getSize().getZ() - 1), Mirror.NONE, this.rotation, BlockPos.ZERO
                )
                .offset(this.templatePosition);
            this.templatePosition = new BlockPos(
                this.templatePosition.getX(), this.getHeight(this.templatePosition, param0, var1), this.templatePosition.getZ()
            );
            return super.postProcess(param0, param1, param2, param3, param4, param5, param6);
        }

        private int getHeight(BlockPos param0, BlockGetter param1, BlockPos param2) {
            int var0 = param0.getY();
            int var1 = 512;
            int var2 = var0 - 1;
            int var3 = 0;

            for(BlockPos var4 : BlockPos.betweenClosed(param0, param2)) {
                int var5 = var4.getX();
                int var6 = var4.getZ();
                int var7 = param0.getY() - 1;
                BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos(var5, var7, var6);
                BlockState var9 = param1.getBlockState(var8);

                for(FluidState var10 = param1.getFluidState(var8);
                    (var9.isAir() || var10.is(FluidTags.WATER) || var9.getBlock().is(BlockTags.ICE)) && var7 > 1;
                    var10 = param1.getFluidState(var8)
                ) {
                    var8.set(var5, --var7, var6);
                    var9 = param1.getBlockState(var8);
                }

                var1 = Math.min(var1, var7);
                if (var7 < var2 - 2) {
                    ++var3;
                }
            }

            int var11 = Math.abs(param0.getX() - param2.getX());
            if (var2 - var1 > 2 && var3 > var11 - 2) {
                var0 = var1 + 1;
            }

            return var0;
        }
    }
}
