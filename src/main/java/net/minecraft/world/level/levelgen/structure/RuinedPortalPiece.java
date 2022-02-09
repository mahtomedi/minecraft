package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.LavaSubmergedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

public class RuinedPortalPiece extends TemplateStructurePiece {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float PROBABILITY_OF_GOLD_GONE = 0.3F;
    private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_NETHERRACK = 0.07F;
    private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_LAVA = 0.2F;
    private static final float DEFAULT_MOSSINESS = 0.2F;
    private final RuinedPortalPiece.VerticalPlacement verticalPlacement;
    private final RuinedPortalPiece.Properties properties;

    public RuinedPortalPiece(
        StructureManager param0,
        BlockPos param1,
        RuinedPortalPiece.VerticalPlacement param2,
        RuinedPortalPiece.Properties param3,
        ResourceLocation param4,
        StructureTemplate param5,
        Rotation param6,
        Mirror param7,
        BlockPos param8
    ) {
        super(StructurePieceType.RUINED_PORTAL, 0, param0, param4, param4.toString(), makeSettings(param7, param6, param2, param8, param3), param1);
        this.verticalPlacement = param2;
        this.properties = param3;
    }

    public RuinedPortalPiece(StructureManager param0, CompoundTag param1) {
        super(StructurePieceType.RUINED_PORTAL, param1, param0, param2 -> makeSettings(param0, param1, param2));
        this.verticalPlacement = RuinedPortalPiece.VerticalPlacement.byName(param1.getString("VerticalPlacement"));
        this.properties = RuinedPortalPiece.Properties.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, param1.get("Properties"))).getOrThrow(true, LOGGER::error);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
        super.addAdditionalSaveData(param0, param1);
        param1.putString("Rotation", this.placeSettings.getRotation().name());
        param1.putString("Mirror", this.placeSettings.getMirror().name());
        param1.putString("VerticalPlacement", this.verticalPlacement.getName());
        RuinedPortalPiece.Properties.CODEC
            .encodeStart(NbtOps.INSTANCE, this.properties)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1x -> param1.put("Properties", param1x));
    }

    private static StructurePlaceSettings makeSettings(StructureManager param0, CompoundTag param1, ResourceLocation param2) {
        StructureTemplate var0 = param0.getOrCreate(param2);
        BlockPos var1 = new BlockPos(var0.getSize().getX() / 2, 0, var0.getSize().getZ() / 2);
        return makeSettings(
            Mirror.valueOf(param1.getString("Mirror")),
            Rotation.valueOf(param1.getString("Rotation")),
            RuinedPortalPiece.VerticalPlacement.byName(param1.getString("VerticalPlacement")),
            var1,
            RuinedPortalPiece.Properties.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, param1.get("Properties"))).getOrThrow(true, LOGGER::error)
        );
    }

    private static StructurePlaceSettings makeSettings(
        Mirror param0, Rotation param1, RuinedPortalPiece.VerticalPlacement param2, BlockPos param3, RuinedPortalPiece.Properties param4
    ) {
        BlockIgnoreProcessor var0 = param4.airPocket ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
        List<ProcessorRule> var1 = Lists.newArrayList();
        var1.add(getBlockReplaceRule(Blocks.GOLD_BLOCK, 0.3F, Blocks.AIR));
        var1.add(getLavaProcessorRule(param2, param4));
        if (!param4.cold) {
            var1.add(getBlockReplaceRule(Blocks.NETHERRACK, 0.07F, Blocks.MAGMA_BLOCK));
        }

        StructurePlaceSettings var2 = new StructurePlaceSettings()
            .setRotation(param1)
            .setMirror(param0)
            .setRotationPivot(param3)
            .addProcessor(var0)
            .addProcessor(new RuleProcessor(var1))
            .addProcessor(new BlockAgeProcessor(param4.mossiness))
            .addProcessor(new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE))
            .addProcessor(new LavaSubmergedBlockProcessor());
        if (param4.replaceWithBlackstone) {
            var2.addProcessor(BlackstoneReplaceProcessor.INSTANCE);
        }

        return var2;
    }

    private static ProcessorRule getLavaProcessorRule(RuinedPortalPiece.VerticalPlacement param0, RuinedPortalPiece.Properties param1) {
        if (param0 == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR) {
            return getBlockReplaceRule(Blocks.LAVA, Blocks.MAGMA_BLOCK);
        } else {
            return param1.cold ? getBlockReplaceRule(Blocks.LAVA, Blocks.NETHERRACK) : getBlockReplaceRule(Blocks.LAVA, 0.2F, Blocks.MAGMA_BLOCK);
        }
    }

    @Override
    public void postProcess(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
    ) {
        BoundingBox var0 = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
        if (param4.isInside(var0.getCenter())) {
            param4.encapsulate(var0);
            super.postProcess(param0, param1, param2, param3, param4, param5, param6);
            this.spreadNetherrack(param3, param0);
            this.addNetherrackDripColumnsBelowPortal(param3, param0);
            if (this.properties.vines || this.properties.overgrown) {
                BlockPos.betweenClosedStream(this.getBoundingBox()).forEach(param2x -> {
                    if (this.properties.vines) {
                        this.maybeAddVines(param3, param0, param2x);
                    }

                    if (this.properties.overgrown) {
                        this.maybeAddLeavesAbove(param3, param0, param2x);
                    }

                });
            }

        }
    }

    @Override
    protected void handleDataMarker(String param0, BlockPos param1, ServerLevelAccessor param2, Random param3, BoundingBox param4) {
    }

    private void maybeAddVines(Random param0, LevelAccessor param1, BlockPos param2) {
        BlockState var0 = param1.getBlockState(param2);
        if (!var0.isAir() && !var0.is(Blocks.VINE)) {
            Direction var1 = getRandomHorizontalDirection(param0);
            BlockPos var2 = param2.relative(var1);
            BlockState var3 = param1.getBlockState(var2);
            if (var3.isAir()) {
                if (Block.isFaceFull(var0.getCollisionShape(param1, param2), var1)) {
                    BooleanProperty var4 = VineBlock.getPropertyForFace(var1.getOpposite());
                    param1.setBlock(var2, Blocks.VINE.defaultBlockState().setValue(var4, Boolean.valueOf(true)), 3);
                }
            }
        }
    }

    private void maybeAddLeavesAbove(Random param0, LevelAccessor param1, BlockPos param2) {
        if (param0.nextFloat() < 0.5F && param1.getBlockState(param2).is(Blocks.NETHERRACK) && param1.getBlockState(param2.above()).isAir()) {
            param1.setBlock(param2.above(), Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, Boolean.valueOf(true)), 3);
        }

    }

    private void addNetherrackDripColumnsBelowPortal(Random param0, LevelAccessor param1) {
        for(int var0 = this.boundingBox.minX() + 1; var0 < this.boundingBox.maxX(); ++var0) {
            for(int var1 = this.boundingBox.minZ() + 1; var1 < this.boundingBox.maxZ(); ++var1) {
                BlockPos var2 = new BlockPos(var0, this.boundingBox.minY(), var1);
                if (param1.getBlockState(var2).is(Blocks.NETHERRACK)) {
                    this.addNetherrackDripColumn(param0, param1, var2.below());
                }
            }
        }

    }

    private void addNetherrackDripColumn(Random param0, LevelAccessor param1, BlockPos param2) {
        BlockPos.MutableBlockPos var0 = param2.mutable();
        this.placeNetherrackOrMagma(param0, param1, var0);
        int var1 = 8;

        while(var1 > 0 && param0.nextFloat() < 0.5F) {
            var0.move(Direction.DOWN);
            --var1;
            this.placeNetherrackOrMagma(param0, param1, var0);
        }

    }

    private void spreadNetherrack(Random param0, LevelAccessor param1) {
        boolean var0 = this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE
            || this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
        BlockPos var1 = this.boundingBox.getCenter();
        int var2 = var1.getX();
        int var3 = var1.getZ();
        float[] var4 = new float[]{1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.9F, 0.9F, 0.8F, 0.7F, 0.6F, 0.4F, 0.2F};
        int var5 = var4.length;
        int var6 = (this.boundingBox.getXSpan() + this.boundingBox.getZSpan()) / 2;
        int var7 = param0.nextInt(Math.max(1, 8 - var6 / 2));
        int var8 = 3;
        BlockPos.MutableBlockPos var9 = BlockPos.ZERO.mutable();

        for(int var10 = var2 - var5; var10 <= var2 + var5; ++var10) {
            for(int var11 = var3 - var5; var11 <= var3 + var5; ++var11) {
                int var12 = Math.abs(var10 - var2) + Math.abs(var11 - var3);
                int var13 = Math.max(0, var12 + var7);
                if (var13 < var5) {
                    float var14 = var4[var13];
                    if (param0.nextDouble() < (double)var14) {
                        int var15 = getSurfaceY(param1, var10, var11, this.verticalPlacement);
                        int var16 = var0 ? var15 : Math.min(this.boundingBox.minY(), var15);
                        var9.set(var10, var16, var11);
                        if (Math.abs(var16 - this.boundingBox.minY()) <= 3 && this.canBlockBeReplacedByNetherrackOrMagma(param1, var9)) {
                            this.placeNetherrackOrMagma(param0, param1, var9);
                            if (this.properties.overgrown) {
                                this.maybeAddLeavesAbove(param0, param1, var9);
                            }

                            this.addNetherrackDripColumn(param0, param1, var9.below());
                        }
                    }
                }
            }
        }

    }

    private boolean canBlockBeReplacedByNetherrackOrMagma(LevelAccessor param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        return !var0.is(Blocks.AIR)
            && !var0.is(Blocks.OBSIDIAN)
            && !var0.is(BlockTags.FEATURES_CANNOT_REPLACE)
            && (this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_NETHER || !var0.is(Blocks.LAVA));
    }

    private void placeNetherrackOrMagma(Random param0, LevelAccessor param1, BlockPos param2) {
        if (!this.properties.cold && param0.nextFloat() < 0.07F) {
            param1.setBlock(param2, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        } else {
            param1.setBlock(param2, Blocks.NETHERRACK.defaultBlockState(), 3);
        }

    }

    private static int getSurfaceY(LevelAccessor param0, int param1, int param2, RuinedPortalPiece.VerticalPlacement param3) {
        return param0.getHeight(getHeightMapType(param3), param1, param2) - 1;
    }

    public static Heightmap.Types getHeightMapType(RuinedPortalPiece.VerticalPlacement param0) {
        return param0 == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
    }

    private static ProcessorRule getBlockReplaceRule(Block param0, float param1, Block param2) {
        return new ProcessorRule(new RandomBlockMatchTest(param0, param1), AlwaysTrueTest.INSTANCE, param2.defaultBlockState());
    }

    private static ProcessorRule getBlockReplaceRule(Block param0, Block param1) {
        return new ProcessorRule(new BlockMatchTest(param0), AlwaysTrueTest.INSTANCE, param1.defaultBlockState());
    }

    public static class Properties {
        public static final Codec<RuinedPortalPiece.Properties> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.BOOL.fieldOf("cold").forGetter(param0x -> param0x.cold),
                        Codec.FLOAT.fieldOf("mossiness").forGetter(param0x -> param0x.mossiness),
                        Codec.BOOL.fieldOf("air_pocket").forGetter(param0x -> param0x.airPocket),
                        Codec.BOOL.fieldOf("overgrown").forGetter(param0x -> param0x.overgrown),
                        Codec.BOOL.fieldOf("vines").forGetter(param0x -> param0x.vines),
                        Codec.BOOL.fieldOf("replace_with_blackstone").forGetter(param0x -> param0x.replaceWithBlackstone)
                    )
                    .apply(param0, RuinedPortalPiece.Properties::new)
        );
        public boolean cold;
        public float mossiness = 0.2F;
        public boolean airPocket;
        public boolean overgrown;
        public boolean vines;
        public boolean replaceWithBlackstone;

        public Properties() {
        }

        public Properties(boolean param0, float param1, boolean param2, boolean param3, boolean param4, boolean param5) {
            this.cold = param0;
            this.mossiness = param1;
            this.airPocket = param2;
            this.overgrown = param3;
            this.vines = param4;
            this.replaceWithBlackstone = param5;
        }
    }

    public static enum VerticalPlacement {
        ON_LAND_SURFACE("on_land_surface"),
        PARTLY_BURIED("partly_buried"),
        ON_OCEAN_FLOOR("on_ocean_floor"),
        IN_MOUNTAIN("in_mountain"),
        UNDERGROUND("underground"),
        IN_NETHER("in_nether");

        private static final Map<String, RuinedPortalPiece.VerticalPlacement> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(RuinedPortalPiece.VerticalPlacement::getName, param0 -> param0));
        private final String name;

        private VerticalPlacement(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        public static RuinedPortalPiece.VerticalPlacement byName(String param0) {
            return BY_NAME.get(param0);
        }
    }
}
