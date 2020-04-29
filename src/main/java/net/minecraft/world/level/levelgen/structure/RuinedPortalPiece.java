package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalPiece extends TemplateStructurePiece {
    private final ResourceLocation templateLocation;
    private final Rotation rotation;
    private final Mirror mirror;
    private final RuinedPortalPiece.VerticalPlacement verticalPlacement;
    private final RuinedPortalPiece.Properties properties;

    public RuinedPortalPiece(
        BlockPos param0,
        RuinedPortalPiece.VerticalPlacement param1,
        RuinedPortalPiece.Properties param2,
        ResourceLocation param3,
        StructureTemplate param4,
        Rotation param5,
        Mirror param6,
        BlockPos param7
    ) {
        super(StructurePieceType.RUINED_PORTAL, 0);
        this.templatePosition = param0;
        this.templateLocation = param3;
        this.rotation = param5;
        this.mirror = param6;
        this.verticalPlacement = param1;
        this.properties = param2;
        this.loadTemplate(param4, param7);
    }

    public RuinedPortalPiece(StructureManager param0, CompoundTag param1) {
        super(StructurePieceType.RUINED_PORTAL, param1);
        this.templateLocation = new ResourceLocation(param1.getString("Template"));
        this.rotation = Rotation.valueOf(param1.getString("Rotation"));
        this.mirror = Mirror.valueOf(param1.getString("Mirror"));
        this.verticalPlacement = RuinedPortalPiece.VerticalPlacement.byName(param1.getString("VerticalPlacement"));
        this.properties = new RuinedPortalPiece.Properties(new Dynamic<>(NbtOps.INSTANCE, param1.get("Properties")));
        StructureTemplate var0 = param0.getOrCreate(this.templateLocation);
        this.loadTemplate(var0, new BlockPos(var0.getSize().getX() / 2, 0, var0.getSize().getZ() / 2));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putString("Template", this.templateLocation.toString());
        param0.putString("Rotation", this.rotation.name());
        param0.putString("Mirror", this.mirror.name());
        param0.putString("VerticalPlacement", this.verticalPlacement.getName());
        param0.put("Properties", this.properties.serialize(NbtOps.INSTANCE));
    }

    private void loadTemplate(StructureTemplate param0, BlockPos param1) {
        BlockIgnoreProcessor var0 = this.properties.airPocket ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
        List<ProcessorRule> var1 = Lists.newArrayList();
        var1.add(getBlockReplaceRule(Blocks.GOLD_BLOCK, 0.3F, Blocks.AIR));
        var1.add(this.getLavaProcessorRule());
        if (!this.properties.cold) {
            var1.add(getBlockReplaceRule(Blocks.NETHERRACK, 0.07F, Blocks.MAGMA_BLOCK));
        }

        StructurePlaceSettings var2 = new StructurePlaceSettings()
            .setRotation(this.rotation)
            .setMirror(this.mirror)
            .setRotationPivot(param1)
            .addProcessor(var0)
            .addProcessor(new RuleProcessor(var1))
            .addProcessor(new BlockAgeProcessor(this.properties.mossiness));
        if (this.properties.replaceWithBlackstone) {
            var2.addProcessor(new BlackstoneReplaceProcessor());
        }

        this.setup(param0, this.templatePosition, var2);
    }

    private ProcessorRule getLavaProcessorRule() {
        if (this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR) {
            return getBlockReplaceRule(Blocks.LAVA, Blocks.MAGMA_BLOCK);
        } else {
            return this.properties.cold ? getBlockReplaceRule(Blocks.LAVA, Blocks.NETHERRACK) : getBlockReplaceRule(Blocks.LAVA, 0.2F, Blocks.MAGMA_BLOCK);
        }
    }

    @Override
    public boolean postProcess(
        LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
    ) {
        if (!param4.isInside(this.templatePosition)) {
            return true;
        } else {
            param4.expand(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
            boolean var0 = super.postProcess(param0, param1, param2, param3, param4, param5, param6);
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

            return var0;
        }
    }

    @Override
    protected void handleDataMarker(String param0, BlockPos param1, LevelAccessor param2, Random param3, BoundingBox param4) {
    }

    private void maybeAddVines(Random param0, LevelAccessor param1, BlockPos param2) {
        BlockState var0 = param1.getBlockState(param2);
        if (!var0.isAir() && !var0.is(Blocks.VINE)) {
            Direction var1 = Direction.Plane.HORIZONTAL.getRandomDirection(param0);
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
        for(int var0 = this.boundingBox.x0 + 1; var0 < this.boundingBox.x1; ++var0) {
            for(int var1 = this.boundingBox.z0 + 1; var1 < this.boundingBox.z1; ++var1) {
                BlockPos var2 = new BlockPos(var0, this.boundingBox.y0, var1);
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
        Vec3i var1 = this.boundingBox.getCenter();
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
                        int var16 = var0 ? var15 : Math.min(this.boundingBox.y0, var15);
                        var9.set(var10, var16, var11);
                        if (Math.abs(var16 - this.boundingBox.y0) <= 3 && this.canBlockBeReplacedByNetherrackOrMagma(param1, var9)) {
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
        public boolean cold;
        public float mossiness = 0.2F;
        public boolean airPocket;
        public boolean overgrown;
        public boolean vines;
        public boolean replaceWithBlackstone;

        public Properties() {
        }

        public <T> Properties(Dynamic<T> param0) {
            this.cold = param0.get("Cold").asBoolean(false);
            this.mossiness = param0.get("Mossiness").asFloat(0.2F);
            this.airPocket = param0.get("AirPocket").asBoolean(false);
            this.overgrown = param0.get("Overgrown").asBoolean(false);
            this.vines = param0.get("Vines").asBoolean(false);
            this.replaceWithBlackstone = param0.get("ReplaceWithBlackstone").asBoolean(false);
        }

        public <T> T serialize(DynamicOps<T> param0) {
            return param0.createMap(
                ImmutableMap.<T, T>builder()
                    .put(param0.createString("Cold"), param0.createBoolean(this.cold))
                    .put(param0.createString("Mossiness"), param0.createFloat(this.mossiness))
                    .put(param0.createString("AirPocket"), param0.createBoolean(this.airPocket))
                    .put(param0.createString("Overgrown"), param0.createBoolean(this.overgrown))
                    .put(param0.createString("Vines"), param0.createBoolean(this.vines))
                    .put(param0.createString("ReplaceWithBlackstone"), param0.createBoolean(this.replaceWithBlackstone))
                    .build()
            );
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
