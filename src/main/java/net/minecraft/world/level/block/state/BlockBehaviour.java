package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockBehaviour implements FeatureElement {
    protected static final Direction[] UPDATE_SHAPE_ORDER = new Direction[]{
        Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP
    };
    protected final boolean hasCollision;
    protected final float explosionResistance;
    protected final boolean isRandomlyTicking;
    protected final SoundType soundType;
    protected final float friction;
    protected final float speedFactor;
    protected final float jumpFactor;
    protected final boolean dynamicShape;
    protected final FeatureFlagSet requiredFeatures;
    protected final BlockBehaviour.Properties properties;
    @Nullable
    protected ResourceLocation drops;

    public BlockBehaviour(BlockBehaviour.Properties param0) {
        this.hasCollision = param0.hasCollision;
        this.drops = param0.drops;
        this.explosionResistance = param0.explosionResistance;
        this.isRandomlyTicking = param0.isRandomlyTicking;
        this.soundType = param0.soundType;
        this.friction = param0.friction;
        this.speedFactor = param0.speedFactor;
        this.jumpFactor = param0.jumpFactor;
        this.dynamicShape = param0.dynamicShape;
        this.requiredFeatures = param0.requiredFeatures;
        this.properties = param0;
    }

    public BlockBehaviour.Properties properties() {
        return this.properties;
    }

    protected abstract MapCodec<? extends Block> codec();

    protected static <B extends Block> RecordCodecBuilder<B, BlockBehaviour.Properties> propertiesCodec() {
        return BlockBehaviour.Properties.CODEC.fieldOf("properties").forGetter(BlockBehaviour::properties);
    }

    public static <B extends Block> MapCodec<B> simpleCodec(Function<BlockBehaviour.Properties, B> param0) {
        return RecordCodecBuilder.mapCodec(param1 -> param1.group(propertiesCodec()).apply(param1, param0));
    }

    @Deprecated
    public void updateIndirectNeighbourShapes(BlockState param0, LevelAccessor param1, BlockPos param2, int param3, int param4) {
    }

    @Deprecated
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        switch(param3) {
            case LAND:
                return !param0.isCollisionShapeFullBlock(param1, param2);
            case WATER:
                return param1.getFluidState(param2).is(FluidTags.WATER);
            case AIR:
                return !param0.isCollisionShapeFullBlock(param1, param2);
            default:
                return false;
        }
    }

    @Deprecated
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param0;
    }

    @Deprecated
    public boolean skipRendering(BlockState param0, BlockState param1, Direction param2) {
        return false;
    }

    @Deprecated
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        DebugPackets.sendNeighborsUpdatePacket(param1, param2);
    }

    @Deprecated
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
    }

    @Deprecated
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param0.hasBlockEntity() && !param0.is(param3.getBlock())) {
            param1.removeBlockEntity(param2);
        }

    }

    @Deprecated
    public void onExplosionHit(BlockState param0, Level param1, BlockPos param2, Explosion param3, BiConsumer<ItemStack, BlockPos> param4) {
        if (!param0.isAir() && param3.getBlockInteraction() != Explosion.BlockInteraction.TRIGGER_BLOCK) {
            Block var0 = param0.getBlock();
            boolean var1 = param3.getIndirectSourceEntity() instanceof Player;
            if (var0.dropFromExplosion(param3) && param1 instanceof ServerLevel var2) {
                BlockEntity var3 = param0.hasBlockEntity() ? param1.getBlockEntity(param2) : null;
                LootParams.Builder var4 = new LootParams.Builder(var2)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(param2))
                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, var3)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, param3.getDirectSourceEntity());
                if (param3.getBlockInteraction() == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
                    var4.withParameter(LootContextParams.EXPLOSION_RADIUS, param3.radius());
                }

                param0.spawnAfterBreak(var2, param2, ItemStack.EMPTY, var1);
                param0.getDrops(var4).forEach(param2x -> param4.accept(param2x, param2));
            }

            param1.setBlock(param2, Blocks.AIR.defaultBlockState(), 3);
            var0.wasExploded(param1, param2, param3);
        }
    }

    @Deprecated
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        return InteractionResult.PASS;
    }

    @Deprecated
    public boolean triggerEvent(BlockState param0, Level param1, BlockPos param2, int param3, int param4) {
        return false;
    }

    @Deprecated
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Deprecated
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return false;
    }

    @Deprecated
    public boolean isSignalSource(BlockState param0) {
        return false;
    }

    @Deprecated
    public FluidState getFluidState(BlockState param0) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Deprecated
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return false;
    }

    public float getMaxHorizontalOffset() {
        return 0.25F;
    }

    public float getMaxVerticalOffset() {
        return 0.2F;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    @Deprecated
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0;
    }

    @Deprecated
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0;
    }

    @Deprecated
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        return param0.canBeReplaced() && (param1.getItemInHand().isEmpty() || !param1.getItemInHand().is(this.asItem()));
    }

    @Deprecated
    public boolean canBeReplaced(BlockState param0, Fluid param1) {
        return param0.canBeReplaced() || !param0.isSolid();
    }

    @Deprecated
    public List<ItemStack> getDrops(BlockState param0, LootParams.Builder param1) {
        ResourceLocation var0 = this.getLootTable();
        if (var0 == BuiltInLootTables.EMPTY) {
            return Collections.emptyList();
        } else {
            LootParams var1 = param1.withParameter(LootContextParams.BLOCK_STATE, param0).create(LootContextParamSets.BLOCK);
            ServerLevel var2 = var1.getLevel();
            LootTable var3 = var2.getServer().getLootData().getLootTable(var0);
            return var3.getRandomItems(var1);
        }
    }

    @Deprecated
    public long getSeed(BlockState param0, BlockPos param1) {
        return Mth.getSeed(param1);
    }

    @Deprecated
    public VoxelShape getOcclusionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.getShape(param1, param2);
    }

    @Deprecated
    public VoxelShape getBlockSupportShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return this.getCollisionShape(param0, param1, param2, CollisionContext.empty());
    }

    @Deprecated
    public VoxelShape getInteractionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return Shapes.empty();
    }

    @Deprecated
    public int getLightBlock(BlockState param0, BlockGetter param1, BlockPos param2) {
        if (param0.isSolidRender(param1, param2)) {
            return param1.getMaxLightLevel();
        } else {
            return param0.propagatesSkylightDown(param1, param2) ? 0 : 1;
        }
    }

    @Nullable
    @Deprecated
    public MenuProvider getMenuProvider(BlockState param0, Level param1, BlockPos param2) {
        return null;
    }

    @Deprecated
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return true;
    }

    @Deprecated
    public float getShadeBrightness(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.isCollisionShapeFullBlock(param1, param2) ? 0.2F : 1.0F;
    }

    @Deprecated
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return 0;
    }

    @Deprecated
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return Shapes.block();
    }

    @Deprecated
    public VoxelShape getCollisionShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.hasCollision ? param0.getShape(param1, param2) : Shapes.empty();
    }

    @Deprecated
    public boolean isCollisionShapeFullBlock(BlockState param0, BlockGetter param1, BlockPos param2) {
        return Block.isShapeFullBlock(param0.getCollisionShape(param1, param2));
    }

    @Deprecated
    public boolean isOcclusionShapeFullBlock(BlockState param0, BlockGetter param1, BlockPos param2) {
        return Block.isShapeFullBlock(param0.getOcclusionShape(param1, param2));
    }

    @Deprecated
    public VoxelShape getVisualShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.getCollisionShape(param0, param1, param2, param3);
    }

    @Deprecated
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
    }

    @Deprecated
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
    }

    @Deprecated
    public float getDestroyProgress(BlockState param0, Player param1, BlockGetter param2, BlockPos param3) {
        float var0 = param0.getDestroySpeed(param2, param3);
        if (var0 == -1.0F) {
            return 0.0F;
        } else {
            int var1 = param1.hasCorrectToolForDrops(param0) ? 30 : 100;
            return param1.getDestroySpeed(param0) / var0 / (float)var1;
        }
    }

    @Deprecated
    public void spawnAfterBreak(BlockState param0, ServerLevel param1, BlockPos param2, ItemStack param3, boolean param4) {
    }

    @Deprecated
    public void attack(BlockState param0, Level param1, BlockPos param2, Player param3) {
    }

    @Deprecated
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return 0;
    }

    @Deprecated
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
    }

    @Deprecated
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return 0;
    }

    public final ResourceLocation getLootTable() {
        if (this.drops == null) {
            ResourceLocation var0 = BuiltInRegistries.BLOCK.getKey(this.asBlock());
            this.drops = var0.withPrefix("blocks/");
        }

        return this.drops;
    }

    @Deprecated
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
    }

    public abstract Item asItem();

    protected abstract Block asBlock();

    public MapColor defaultMapColor() {
        return this.properties.mapColor.apply(this.asBlock().defaultBlockState());
    }

    public float defaultDestroyTime() {
        return this.properties.destroyTime;
    }

    public abstract static class BlockStateBase extends StateHolder<Block, BlockState> {
        private final int lightEmission;
        private final boolean useShapeForLightOcclusion;
        private final boolean isAir;
        private final boolean ignitedByLava;
        @Deprecated
        private final boolean liquid;
        @Deprecated
        private boolean legacySolid;
        private final PushReaction pushReaction;
        private final MapColor mapColor;
        private final float destroySpeed;
        private final boolean requiresCorrectToolForDrops;
        private final boolean canOcclude;
        private final BlockBehaviour.StatePredicate isRedstoneConductor;
        private final BlockBehaviour.StatePredicate isSuffocating;
        private final BlockBehaviour.StatePredicate isViewBlocking;
        private final BlockBehaviour.StatePredicate hasPostProcess;
        private final BlockBehaviour.StatePredicate emissiveRendering;
        private final Optional<BlockBehaviour.OffsetFunction> offsetFunction;
        private final boolean spawnTerrainParticles;
        private final NoteBlockInstrument instrument;
        private final boolean replaceable;
        @Nullable
        protected BlockBehaviour.BlockStateBase.Cache cache;
        private FluidState fluidState = Fluids.EMPTY.defaultFluidState();
        private boolean isRandomlyTicking;

        protected BlockStateBase(Block param0, ImmutableMap<Property<?>, Comparable<?>> param1, MapCodec<BlockState> param2) {
            super(param0, param1, param2);
            BlockBehaviour.Properties var0 = param0.properties;
            this.lightEmission = var0.lightEmission.applyAsInt(this.asState());
            this.useShapeForLightOcclusion = param0.useShapeForLightOcclusion(this.asState());
            this.isAir = var0.isAir;
            this.ignitedByLava = var0.ignitedByLava;
            this.liquid = var0.liquid;
            this.pushReaction = var0.pushReaction;
            this.mapColor = var0.mapColor.apply(this.asState());
            this.destroySpeed = var0.destroyTime;
            this.requiresCorrectToolForDrops = var0.requiresCorrectToolForDrops;
            this.canOcclude = var0.canOcclude;
            this.isRedstoneConductor = var0.isRedstoneConductor;
            this.isSuffocating = var0.isSuffocating;
            this.isViewBlocking = var0.isViewBlocking;
            this.hasPostProcess = var0.hasPostProcess;
            this.emissiveRendering = var0.emissiveRendering;
            this.offsetFunction = var0.offsetFunction;
            this.spawnTerrainParticles = var0.spawnTerrainParticles;
            this.instrument = var0.instrument;
            this.replaceable = var0.replaceable;
        }

        private boolean calculateSolid() {
            if (this.owner.properties.forceSolidOn) {
                return true;
            } else if (this.owner.properties.forceSolidOff) {
                return false;
            } else if (this.cache == null) {
                return false;
            } else {
                VoxelShape var0 = this.cache.collisionShape;
                if (var0.isEmpty()) {
                    return false;
                } else {
                    AABB var1 = var0.bounds();
                    if (var1.getSize() >= 0.7291666666666666) {
                        return true;
                    } else {
                        return var1.getYsize() >= 1.0;
                    }
                }
            }
        }

        public void initCache() {
            this.fluidState = this.owner.getFluidState(this.asState());
            this.isRandomlyTicking = this.owner.isRandomlyTicking(this.asState());
            if (!this.getBlock().hasDynamicShape()) {
                this.cache = new BlockBehaviour.BlockStateBase.Cache(this.asState());
            }

            this.legacySolid = this.calculateSolid();
        }

        public Block getBlock() {
            return this.owner;
        }

        public Holder<Block> getBlockHolder() {
            return this.owner.builtInRegistryHolder();
        }

        @Deprecated
        public boolean blocksMotion() {
            Block var0 = this.getBlock();
            return var0 != Blocks.COBWEB && var0 != Blocks.BAMBOO_SAPLING && this.isSolid();
        }

        @Deprecated
        public boolean isSolid() {
            return this.legacySolid;
        }

        public boolean isValidSpawn(BlockGetter param0, BlockPos param1, EntityType<?> param2) {
            return this.getBlock().properties.isValidSpawn.test(this.asState(), param0, param1, param2);
        }

        public boolean propagatesSkylightDown(BlockGetter param0, BlockPos param1) {
            return this.cache != null ? this.cache.propagatesSkylightDown : this.getBlock().propagatesSkylightDown(this.asState(), param0, param1);
        }

        public int getLightBlock(BlockGetter param0, BlockPos param1) {
            return this.cache != null ? this.cache.lightBlock : this.getBlock().getLightBlock(this.asState(), param0, param1);
        }

        public VoxelShape getFaceOcclusionShape(BlockGetter param0, BlockPos param1, Direction param2) {
            return this.cache != null && this.cache.occlusionShapes != null
                ? this.cache.occlusionShapes[param2.ordinal()]
                : Shapes.getFaceShape(this.getOcclusionShape(param0, param1), param2);
        }

        public VoxelShape getOcclusionShape(BlockGetter param0, BlockPos param1) {
            return this.getBlock().getOcclusionShape(this.asState(), param0, param1);
        }

        public boolean hasLargeCollisionShape() {
            return this.cache == null || this.cache.largeCollisionShape;
        }

        public boolean useShapeForLightOcclusion() {
            return this.useShapeForLightOcclusion;
        }

        public int getLightEmission() {
            return this.lightEmission;
        }

        public boolean isAir() {
            return this.isAir;
        }

        public boolean ignitedByLava() {
            return this.ignitedByLava;
        }

        @Deprecated
        public boolean liquid() {
            return this.liquid;
        }

        public MapColor getMapColor(BlockGetter param0, BlockPos param1) {
            return this.mapColor;
        }

        public BlockState rotate(Rotation param0) {
            return this.getBlock().rotate(this.asState(), param0);
        }

        public BlockState mirror(Mirror param0) {
            return this.getBlock().mirror(this.asState(), param0);
        }

        public RenderShape getRenderShape() {
            return this.getBlock().getRenderShape(this.asState());
        }

        public boolean emissiveRendering(BlockGetter param0, BlockPos param1) {
            return this.emissiveRendering.test(this.asState(), param0, param1);
        }

        public float getShadeBrightness(BlockGetter param0, BlockPos param1) {
            return this.getBlock().getShadeBrightness(this.asState(), param0, param1);
        }

        public boolean isRedstoneConductor(BlockGetter param0, BlockPos param1) {
            return this.isRedstoneConductor.test(this.asState(), param0, param1);
        }

        public boolean isSignalSource() {
            return this.getBlock().isSignalSource(this.asState());
        }

        public int getSignal(BlockGetter param0, BlockPos param1, Direction param2) {
            return this.getBlock().getSignal(this.asState(), param0, param1, param2);
        }

        public boolean hasAnalogOutputSignal() {
            return this.getBlock().hasAnalogOutputSignal(this.asState());
        }

        public int getAnalogOutputSignal(Level param0, BlockPos param1) {
            return this.getBlock().getAnalogOutputSignal(this.asState(), param0, param1);
        }

        public float getDestroySpeed(BlockGetter param0, BlockPos param1) {
            return this.destroySpeed;
        }

        public float getDestroyProgress(Player param0, BlockGetter param1, BlockPos param2) {
            return this.getBlock().getDestroyProgress(this.asState(), param0, param1, param2);
        }

        public int getDirectSignal(BlockGetter param0, BlockPos param1, Direction param2) {
            return this.getBlock().getDirectSignal(this.asState(), param0, param1, param2);
        }

        public PushReaction getPistonPushReaction() {
            return this.pushReaction;
        }

        public boolean isSolidRender(BlockGetter param0, BlockPos param1) {
            if (this.cache != null) {
                return this.cache.solidRender;
            } else {
                BlockState var0 = this.asState();
                return var0.canOcclude() ? Block.isShapeFullBlock(var0.getOcclusionShape(param0, param1)) : false;
            }
        }

        public boolean canOcclude() {
            return this.canOcclude;
        }

        public boolean skipRendering(BlockState param0, Direction param1) {
            return this.getBlock().skipRendering(this.asState(), param0, param1);
        }

        public VoxelShape getShape(BlockGetter param0, BlockPos param1) {
            return this.getShape(param0, param1, CollisionContext.empty());
        }

        public VoxelShape getShape(BlockGetter param0, BlockPos param1, CollisionContext param2) {
            return this.getBlock().getShape(this.asState(), param0, param1, param2);
        }

        public VoxelShape getCollisionShape(BlockGetter param0, BlockPos param1) {
            return this.cache != null ? this.cache.collisionShape : this.getCollisionShape(param0, param1, CollisionContext.empty());
        }

        public VoxelShape getCollisionShape(BlockGetter param0, BlockPos param1, CollisionContext param2) {
            return this.getBlock().getCollisionShape(this.asState(), param0, param1, param2);
        }

        public VoxelShape getBlockSupportShape(BlockGetter param0, BlockPos param1) {
            return this.getBlock().getBlockSupportShape(this.asState(), param0, param1);
        }

        public VoxelShape getVisualShape(BlockGetter param0, BlockPos param1, CollisionContext param2) {
            return this.getBlock().getVisualShape(this.asState(), param0, param1, param2);
        }

        public VoxelShape getInteractionShape(BlockGetter param0, BlockPos param1) {
            return this.getBlock().getInteractionShape(this.asState(), param0, param1);
        }

        public final boolean entityCanStandOn(BlockGetter param0, BlockPos param1, Entity param2) {
            return this.entityCanStandOnFace(param0, param1, param2, Direction.UP);
        }

        public final boolean entityCanStandOnFace(BlockGetter param0, BlockPos param1, Entity param2, Direction param3) {
            return Block.isFaceFull(this.getCollisionShape(param0, param1, CollisionContext.of(param2)), param3);
        }

        public Vec3 getOffset(BlockGetter param0, BlockPos param1) {
            return this.offsetFunction.<Vec3>map(param2 -> param2.evaluate(this.asState(), param0, param1)).orElse(Vec3.ZERO);
        }

        public boolean hasOffsetFunction() {
            return this.offsetFunction.isPresent();
        }

        public boolean triggerEvent(Level param0, BlockPos param1, int param2, int param3) {
            return this.getBlock().triggerEvent(this.asState(), param0, param1, param2, param3);
        }

        @Deprecated
        public void neighborChanged(Level param0, BlockPos param1, Block param2, BlockPos param3, boolean param4) {
            this.getBlock().neighborChanged(this.asState(), param0, param1, param2, param3, param4);
        }

        public final void updateNeighbourShapes(LevelAccessor param0, BlockPos param1, int param2) {
            this.updateNeighbourShapes(param0, param1, param2, 512);
        }

        public final void updateNeighbourShapes(LevelAccessor param0, BlockPos param1, int param2, int param3) {
            BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

            for(Direction var1 : BlockBehaviour.UPDATE_SHAPE_ORDER) {
                var0.setWithOffset(param1, var1);
                param0.neighborShapeChanged(var1.getOpposite(), this.asState(), var0, param1, param2, param3);
            }

        }

        public final void updateIndirectNeighbourShapes(LevelAccessor param0, BlockPos param1, int param2) {
            this.updateIndirectNeighbourShapes(param0, param1, param2, 512);
        }

        public void updateIndirectNeighbourShapes(LevelAccessor param0, BlockPos param1, int param2, int param3) {
            this.getBlock().updateIndirectNeighbourShapes(this.asState(), param0, param1, param2, param3);
        }

        public void onPlace(Level param0, BlockPos param1, BlockState param2, boolean param3) {
            this.getBlock().onPlace(this.asState(), param0, param1, param2, param3);
        }

        public void onRemove(Level param0, BlockPos param1, BlockState param2, boolean param3) {
            this.getBlock().onRemove(this.asState(), param0, param1, param2, param3);
        }

        public void onExplosionHit(Level param0, BlockPos param1, Explosion param2, BiConsumer<ItemStack, BlockPos> param3) {
            this.getBlock().onExplosionHit(this.asState(), param0, param1, param2, param3);
        }

        public void tick(ServerLevel param0, BlockPos param1, RandomSource param2) {
            this.getBlock().tick(this.asState(), param0, param1, param2);
        }

        public void randomTick(ServerLevel param0, BlockPos param1, RandomSource param2) {
            this.getBlock().randomTick(this.asState(), param0, param1, param2);
        }

        public void entityInside(Level param0, BlockPos param1, Entity param2) {
            this.getBlock().entityInside(this.asState(), param0, param1, param2);
        }

        public void spawnAfterBreak(ServerLevel param0, BlockPos param1, ItemStack param2, boolean param3) {
            this.getBlock().spawnAfterBreak(this.asState(), param0, param1, param2, param3);
        }

        public List<ItemStack> getDrops(LootParams.Builder param0) {
            return this.getBlock().getDrops(this.asState(), param0);
        }

        public InteractionResult use(Level param0, Player param1, InteractionHand param2, BlockHitResult param3) {
            return this.getBlock().use(this.asState(), param0, param3.getBlockPos(), param1, param2, param3);
        }

        public void attack(Level param0, BlockPos param1, Player param2) {
            this.getBlock().attack(this.asState(), param0, param1, param2);
        }

        public boolean isSuffocating(BlockGetter param0, BlockPos param1) {
            return this.isSuffocating.test(this.asState(), param0, param1);
        }

        public boolean isViewBlocking(BlockGetter param0, BlockPos param1) {
            return this.isViewBlocking.test(this.asState(), param0, param1);
        }

        public BlockState updateShape(Direction param0, BlockState param1, LevelAccessor param2, BlockPos param3, BlockPos param4) {
            return this.getBlock().updateShape(this.asState(), param0, param1, param2, param3, param4);
        }

        public boolean isPathfindable(BlockGetter param0, BlockPos param1, PathComputationType param2) {
            return this.getBlock().isPathfindable(this.asState(), param0, param1, param2);
        }

        public boolean canBeReplaced(BlockPlaceContext param0) {
            return this.getBlock().canBeReplaced(this.asState(), param0);
        }

        public boolean canBeReplaced(Fluid param0) {
            return this.getBlock().canBeReplaced(this.asState(), param0);
        }

        public boolean canBeReplaced() {
            return this.replaceable;
        }

        public boolean canSurvive(LevelReader param0, BlockPos param1) {
            return this.getBlock().canSurvive(this.asState(), param0, param1);
        }

        public boolean hasPostProcess(BlockGetter param0, BlockPos param1) {
            return this.hasPostProcess.test(this.asState(), param0, param1);
        }

        @Nullable
        public MenuProvider getMenuProvider(Level param0, BlockPos param1) {
            return this.getBlock().getMenuProvider(this.asState(), param0, param1);
        }

        public boolean is(TagKey<Block> param0) {
            return this.getBlock().builtInRegistryHolder().is(param0);
        }

        public boolean is(TagKey<Block> param0, Predicate<BlockBehaviour.BlockStateBase> param1) {
            return this.is(param0) && param1.test(this);
        }

        public boolean is(HolderSet<Block> param0) {
            return param0.contains(this.getBlock().builtInRegistryHolder());
        }

        public boolean is(Holder<Block> param0) {
            return this.is(param0.value());
        }

        public Stream<TagKey<Block>> getTags() {
            return this.getBlock().builtInRegistryHolder().tags();
        }

        public boolean hasBlockEntity() {
            return this.getBlock() instanceof EntityBlock;
        }

        @Nullable
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockEntityType<T> param1) {
            return this.getBlock() instanceof EntityBlock ? ((EntityBlock)this.getBlock()).getTicker(param0, this.asState(), param1) : null;
        }

        public boolean is(Block param0) {
            return this.getBlock() == param0;
        }

        public boolean is(ResourceKey<Block> param0) {
            return this.getBlock().builtInRegistryHolder().is(param0);
        }

        public FluidState getFluidState() {
            return this.fluidState;
        }

        public boolean isRandomlyTicking() {
            return this.isRandomlyTicking;
        }

        public long getSeed(BlockPos param0) {
            return this.getBlock().getSeed(this.asState(), param0);
        }

        public SoundType getSoundType() {
            return this.getBlock().getSoundType(this.asState());
        }

        public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
            this.getBlock().onProjectileHit(param0, param1, param2, param3);
        }

        public boolean isFaceSturdy(BlockGetter param0, BlockPos param1, Direction param2) {
            return this.isFaceSturdy(param0, param1, param2, SupportType.FULL);
        }

        public boolean isFaceSturdy(BlockGetter param0, BlockPos param1, Direction param2, SupportType param3) {
            return this.cache != null ? this.cache.isFaceSturdy(param2, param3) : param3.isSupporting(this.asState(), param0, param1, param2);
        }

        public boolean isCollisionShapeFullBlock(BlockGetter param0, BlockPos param1) {
            return this.cache != null ? this.cache.isCollisionShapeFullBlock : this.getBlock().isCollisionShapeFullBlock(this.asState(), param0, param1);
        }

        protected abstract BlockState asState();

        public boolean requiresCorrectToolForDrops() {
            return this.requiresCorrectToolForDrops;
        }

        public boolean shouldSpawnTerrainParticles() {
            return this.spawnTerrainParticles;
        }

        public NoteBlockInstrument instrument() {
            return this.instrument;
        }

        static final class Cache {
            private static final Direction[] DIRECTIONS = Direction.values();
            private static final int SUPPORT_TYPE_COUNT = SupportType.values().length;
            protected final boolean solidRender;
            final boolean propagatesSkylightDown;
            final int lightBlock;
            @Nullable
            final VoxelShape[] occlusionShapes;
            protected final VoxelShape collisionShape;
            protected final boolean largeCollisionShape;
            private final boolean[] faceSturdy;
            protected final boolean isCollisionShapeFullBlock;

            Cache(BlockState param0) {
                Block var0 = param0.getBlock();
                this.solidRender = param0.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                this.propagatesSkylightDown = var0.propagatesSkylightDown(param0, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                this.lightBlock = var0.getLightBlock(param0, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                if (!param0.canOcclude()) {
                    this.occlusionShapes = null;
                } else {
                    this.occlusionShapes = new VoxelShape[DIRECTIONS.length];
                    VoxelShape var1 = var0.getOcclusionShape(param0, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

                    for(Direction var2 : DIRECTIONS) {
                        this.occlusionShapes[var2.ordinal()] = Shapes.getFaceShape(var1, var2);
                    }
                }

                this.collisionShape = var0.getCollisionShape(param0, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
                if (!this.collisionShape.isEmpty() && param0.hasOffsetFunction()) {
                    throw new IllegalStateException(
                        String.format(
                            Locale.ROOT,
                            "%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.",
                            BuiltInRegistries.BLOCK.getKey(var0)
                        )
                    );
                } else {
                    this.largeCollisionShape = Arrays.stream(Direction.Axis.values())
                        .anyMatch(param0x -> this.collisionShape.min(param0x) < 0.0 || this.collisionShape.max(param0x) > 1.0);
                    this.faceSturdy = new boolean[DIRECTIONS.length * SUPPORT_TYPE_COUNT];

                    for(Direction var3 : DIRECTIONS) {
                        for(SupportType var4 : SupportType.values()) {
                            this.faceSturdy[getFaceSupportIndex(var3, var4)] = var4.isSupporting(param0, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, var3);
                        }
                    }

                    this.isCollisionShapeFullBlock = Block.isShapeFullBlock(param0.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
                }
            }

            public boolean isFaceSturdy(Direction param0, SupportType param1) {
                return this.faceSturdy[getFaceSupportIndex(param0, param1)];
            }

            private static int getFaceSupportIndex(Direction param0, SupportType param1) {
                return param0.ordinal() * SUPPORT_TYPE_COUNT + param1.ordinal();
            }
        }
    }

    public interface OffsetFunction {
        Vec3 evaluate(BlockState var1, BlockGetter var2, BlockPos var3);
    }

    public static enum OffsetType {
        NONE,
        XZ,
        XYZ;
    }

    public static class Properties {
        public static final Codec<BlockBehaviour.Properties> CODEC = Codec.unit(() -> of());
        Function<BlockState, MapColor> mapColor = param0 -> MapColor.NONE;
        boolean hasCollision = true;
        SoundType soundType = SoundType.STONE;
        ToIntFunction<BlockState> lightEmission = param0 -> 0;
        float explosionResistance;
        float destroyTime;
        boolean requiresCorrectToolForDrops;
        boolean isRandomlyTicking;
        float friction = 0.6F;
        float speedFactor = 1.0F;
        float jumpFactor = 1.0F;
        ResourceLocation drops;
        boolean canOcclude = true;
        boolean isAir;
        boolean ignitedByLava;
        @Deprecated
        boolean liquid;
        @Deprecated
        boolean forceSolidOff;
        boolean forceSolidOn;
        PushReaction pushReaction = PushReaction.NORMAL;
        boolean spawnTerrainParticles = true;
        NoteBlockInstrument instrument = NoteBlockInstrument.HARP;
        boolean replaceable;
        BlockBehaviour.StateArgumentPredicate<EntityType<?>> isValidSpawn = (param0, param1, param2, param3) -> param0.isFaceSturdy(
                    param1, param2, Direction.UP
                )
                && param0.getLightEmission() < 14;
        BlockBehaviour.StatePredicate isRedstoneConductor = (param0, param1, param2) -> param0.isCollisionShapeFullBlock(param1, param2);
        BlockBehaviour.StatePredicate isSuffocating = (param0, param1, param2) -> param0.blocksMotion() && param0.isCollisionShapeFullBlock(param1, param2);
        BlockBehaviour.StatePredicate isViewBlocking = this.isSuffocating;
        BlockBehaviour.StatePredicate hasPostProcess = (param0, param1, param2) -> false;
        BlockBehaviour.StatePredicate emissiveRendering = (param0, param1, param2) -> false;
        boolean dynamicShape;
        FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
        Optional<BlockBehaviour.OffsetFunction> offsetFunction = Optional.empty();

        private Properties() {
        }

        public static BlockBehaviour.Properties of() {
            return new BlockBehaviour.Properties();
        }

        public static BlockBehaviour.Properties ofFullCopy(BlockBehaviour param0) {
            BlockBehaviour.Properties var0 = ofLegacyCopy(param0);
            BlockBehaviour.Properties var1 = param0.properties;
            var0.jumpFactor = var1.jumpFactor;
            var0.isRedstoneConductor = var1.isRedstoneConductor;
            var0.isValidSpawn = var1.isValidSpawn;
            var0.hasPostProcess = var1.hasPostProcess;
            var0.isSuffocating = var1.isSuffocating;
            var0.isViewBlocking = var1.isViewBlocking;
            var0.drops = var1.drops;
            return var0;
        }

        @Deprecated
        public static BlockBehaviour.Properties ofLegacyCopy(BlockBehaviour param0) {
            BlockBehaviour.Properties var0 = new BlockBehaviour.Properties();
            BlockBehaviour.Properties var1 = param0.properties;
            var0.destroyTime = var1.destroyTime;
            var0.explosionResistance = var1.explosionResistance;
            var0.hasCollision = var1.hasCollision;
            var0.isRandomlyTicking = var1.isRandomlyTicking;
            var0.lightEmission = var1.lightEmission;
            var0.mapColor = var1.mapColor;
            var0.soundType = var1.soundType;
            var0.friction = var1.friction;
            var0.speedFactor = var1.speedFactor;
            var0.dynamicShape = var1.dynamicShape;
            var0.canOcclude = var1.canOcclude;
            var0.isAir = var1.isAir;
            var0.ignitedByLava = var1.ignitedByLava;
            var0.liquid = var1.liquid;
            var0.forceSolidOff = var1.forceSolidOff;
            var0.forceSolidOn = var1.forceSolidOn;
            var0.pushReaction = var1.pushReaction;
            var0.requiresCorrectToolForDrops = var1.requiresCorrectToolForDrops;
            var0.offsetFunction = var1.offsetFunction;
            var0.spawnTerrainParticles = var1.spawnTerrainParticles;
            var0.requiredFeatures = var1.requiredFeatures;
            var0.emissiveRendering = var1.emissiveRendering;
            var0.instrument = var1.instrument;
            var0.replaceable = var1.replaceable;
            return var0;
        }

        public BlockBehaviour.Properties mapColor(DyeColor param0) {
            this.mapColor = param1 -> param0.getMapColor();
            return this;
        }

        public BlockBehaviour.Properties mapColor(MapColor param0) {
            this.mapColor = param1 -> param0;
            return this;
        }

        public BlockBehaviour.Properties mapColor(Function<BlockState, MapColor> param0) {
            this.mapColor = param0;
            return this;
        }

        public BlockBehaviour.Properties noCollission() {
            this.hasCollision = false;
            this.canOcclude = false;
            return this;
        }

        public BlockBehaviour.Properties noOcclusion() {
            this.canOcclude = false;
            return this;
        }

        public BlockBehaviour.Properties friction(float param0) {
            this.friction = param0;
            return this;
        }

        public BlockBehaviour.Properties speedFactor(float param0) {
            this.speedFactor = param0;
            return this;
        }

        public BlockBehaviour.Properties jumpFactor(float param0) {
            this.jumpFactor = param0;
            return this;
        }

        public BlockBehaviour.Properties sound(SoundType param0) {
            this.soundType = param0;
            return this;
        }

        public BlockBehaviour.Properties lightLevel(ToIntFunction<BlockState> param0) {
            this.lightEmission = param0;
            return this;
        }

        public BlockBehaviour.Properties strength(float param0, float param1) {
            return this.destroyTime(param0).explosionResistance(param1);
        }

        public BlockBehaviour.Properties instabreak() {
            return this.strength(0.0F);
        }

        public BlockBehaviour.Properties strength(float param0) {
            this.strength(param0, param0);
            return this;
        }

        public BlockBehaviour.Properties randomTicks() {
            this.isRandomlyTicking = true;
            return this;
        }

        public BlockBehaviour.Properties dynamicShape() {
            this.dynamicShape = true;
            return this;
        }

        public BlockBehaviour.Properties noLootTable() {
            this.drops = BuiltInLootTables.EMPTY;
            return this;
        }

        public BlockBehaviour.Properties dropsLike(Block param0) {
            this.drops = param0.getLootTable();
            return this;
        }

        public BlockBehaviour.Properties ignitedByLava() {
            this.ignitedByLava = true;
            return this;
        }

        public BlockBehaviour.Properties liquid() {
            this.liquid = true;
            return this;
        }

        public BlockBehaviour.Properties forceSolidOn() {
            this.forceSolidOn = true;
            return this;
        }

        @Deprecated
        public BlockBehaviour.Properties forceSolidOff() {
            this.forceSolidOff = true;
            return this;
        }

        public BlockBehaviour.Properties pushReaction(PushReaction param0) {
            this.pushReaction = param0;
            return this;
        }

        public BlockBehaviour.Properties air() {
            this.isAir = true;
            return this;
        }

        public BlockBehaviour.Properties isValidSpawn(BlockBehaviour.StateArgumentPredicate<EntityType<?>> param0) {
            this.isValidSpawn = param0;
            return this;
        }

        public BlockBehaviour.Properties isRedstoneConductor(BlockBehaviour.StatePredicate param0) {
            this.isRedstoneConductor = param0;
            return this;
        }

        public BlockBehaviour.Properties isSuffocating(BlockBehaviour.StatePredicate param0) {
            this.isSuffocating = param0;
            return this;
        }

        public BlockBehaviour.Properties isViewBlocking(BlockBehaviour.StatePredicate param0) {
            this.isViewBlocking = param0;
            return this;
        }

        public BlockBehaviour.Properties hasPostProcess(BlockBehaviour.StatePredicate param0) {
            this.hasPostProcess = param0;
            return this;
        }

        public BlockBehaviour.Properties emissiveRendering(BlockBehaviour.StatePredicate param0) {
            this.emissiveRendering = param0;
            return this;
        }

        public BlockBehaviour.Properties requiresCorrectToolForDrops() {
            this.requiresCorrectToolForDrops = true;
            return this;
        }

        public BlockBehaviour.Properties destroyTime(float param0) {
            this.destroyTime = param0;
            return this;
        }

        public BlockBehaviour.Properties explosionResistance(float param0) {
            this.explosionResistance = Math.max(0.0F, param0);
            return this;
        }

        public BlockBehaviour.Properties offsetType(BlockBehaviour.OffsetType param0) {
            switch(param0) {
                case XYZ:
                    this.offsetFunction = Optional.of((param0x, param1, param2) -> {
                        Block var0 = param0x.getBlock();
                        long var1x = Mth.getSeed(param2.getX(), 0, param2.getZ());
                        double var2 = ((double)((float)(var1x >> 4 & 15L) / 15.0F) - 1.0) * (double)var0.getMaxVerticalOffset();
                        float var3 = var0.getMaxHorizontalOffset();
                        double var4 = Mth.clamp(((double)((float)(var1x & 15L) / 15.0F) - 0.5) * 0.5, (double)(-var3), (double)var3);
                        double var5 = Mth.clamp(((double)((float)(var1x >> 8 & 15L) / 15.0F) - 0.5) * 0.5, (double)(-var3), (double)var3);
                        return new Vec3(var4, var2, var5);
                    });
                    break;
                case XZ:
                    this.offsetFunction = Optional.of((param0x, param1, param2) -> {
                        Block var0 = param0x.getBlock();
                        long var1x = Mth.getSeed(param2.getX(), 0, param2.getZ());
                        float var2 = var0.getMaxHorizontalOffset();
                        double var3 = Mth.clamp(((double)((float)(var1x & 15L) / 15.0F) - 0.5) * 0.5, (double)(-var2), (double)var2);
                        double var4 = Mth.clamp(((double)((float)(var1x >> 8 & 15L) / 15.0F) - 0.5) * 0.5, (double)(-var2), (double)var2);
                        return new Vec3(var3, 0.0, var4);
                    });
                    break;
                default:
                    this.offsetFunction = Optional.empty();
            }

            return this;
        }

        public BlockBehaviour.Properties noTerrainParticles() {
            this.spawnTerrainParticles = false;
            return this;
        }

        public BlockBehaviour.Properties requiredFeatures(FeatureFlag... param0) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(param0);
            return this;
        }

        public BlockBehaviour.Properties instrument(NoteBlockInstrument param0) {
            this.instrument = param0;
            return this;
        }

        public BlockBehaviour.Properties replaceable() {
            this.replaceable = true;
            return this;
        }
    }

    public interface StateArgumentPredicate<A> {
        boolean test(BlockState var1, BlockGetter var2, BlockPos var3, A var4);
    }

    public interface StatePredicate {
        boolean test(BlockState var1, BlockGetter var2, BlockPos var3);
    }
}
