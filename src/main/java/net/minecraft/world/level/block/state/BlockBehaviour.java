package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BlockBehaviour {
    protected static final Direction[] UPDATE_SHAPE_ORDER = new Direction[]{
        Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP
    };
    protected final Material material;
    protected final boolean hasCollision;
    protected final float explosionResistance;
    protected final boolean isRandomlyTicking;
    protected final SoundType soundType;
    protected final float friction;
    protected final float speedFactor;
    protected final float jumpFactor;
    protected final boolean dynamicShape;
    protected final BlockBehaviour.Properties properties;
    @Nullable
    protected ResourceLocation drops;

    public BlockBehaviour(BlockBehaviour.Properties param0) {
        this.material = param0.material;
        this.hasCollision = param0.hasCollision;
        this.drops = param0.drops;
        this.explosionResistance = param0.explosionResistance;
        this.isRandomlyTicking = param0.isRandomlyTicking;
        this.soundType = param0.soundType;
        this.friction = param0.friction;
        this.speedFactor = param0.speedFactor;
        this.jumpFactor = param0.jumpFactor;
        this.dynamicShape = param0.dynamicShape;
        this.properties = param0;
    }

    @Deprecated
    public void updateIndirectNeighbourShapes(BlockState param0, LevelAccessor param1, BlockPos param2, int param3) {
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
    @OnlyIn(Dist.CLIENT)
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
        if (this.isEntityBlock() && !param0.is(param3.getBlock())) {
            param1.removeBlockEntity(param2);
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
    public PushReaction getPistonPushReaction(BlockState param0) {
        return this.material.getPushReaction();
    }

    @Deprecated
    public FluidState getFluidState(BlockState param0) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Deprecated
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return false;
    }

    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.NONE;
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
        return this.material.isReplaceable() && (param1.getItemInHand().isEmpty() || param1.getItemInHand().getItem() != this.asItem());
    }

    @Deprecated
    public boolean canBeReplaced(BlockState param0, Fluid param1) {
        return this.material.isReplaceable() || !this.material.isSolid();
    }

    @Deprecated
    public List<ItemStack> getDrops(BlockState param0, LootContext.Builder param1) {
        ResourceLocation var0 = this.getLootTable();
        if (var0 == BuiltInLootTables.EMPTY) {
            return Collections.emptyList();
        } else {
            LootContext var1 = param1.withParameter(LootContextParams.BLOCK_STATE, param0).create(LootContextParamSets.BLOCK);
            ServerLevel var2 = var1.getLevel();
            LootTable var3 = var2.getServer().getLootTables().get(var0);
            return var3.getRandomItems(var1);
        }
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
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
    @OnlyIn(Dist.CLIENT)
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
    public VoxelShape getVisualShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.getCollisionShape(param0, param1, param2, param3);
    }

    @Deprecated
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        this.tick(param0, param1, param2, param3);
    }

    @Deprecated
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
    }

    @Deprecated
    public float getDestroyProgress(BlockState param0, Player param1, BlockGetter param2, BlockPos param3) {
        float var0 = param0.getDestroySpeed(param2, param3);
        if (var0 == -1.0F) {
            return 0.0F;
        } else {
            int var1 = param1.canDestroy(param0) ? 30 : 100;
            return param1.getDestroySpeed(param0) / var0 / (float)var1;
        }
    }

    @Deprecated
    public void spawnAfterBreak(BlockState param0, Level param1, BlockPos param2, ItemStack param3) {
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

    public final boolean isEntityBlock() {
        return this instanceof EntityBlock;
    }

    public final ResourceLocation getLootTable() {
        if (this.drops == null) {
            ResourceLocation var0 = Registry.BLOCK.getKey(this.asBlock());
            this.drops = new ResourceLocation(var0.getNamespace(), "blocks/" + var0.getPath());
        }

        return this.drops;
    }

    @Deprecated
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
    }

    public abstract Item asItem();

    protected abstract Block asBlock();

    public MaterialColor defaultMaterialColor() {
        return this.properties.materialColor.apply(this.asBlock().defaultBlockState());
    }

    public abstract static class BlockStateBase extends AbstractStateHolder<Block, BlockState> implements StateHolder<BlockState> {
        private final int lightEmission;
        private final boolean useShapeForLightOcclusion;
        private final boolean isAir;
        private final Material material;
        private final MaterialColor materialColor;
        private final float destroySpeed;
        private final boolean canOcclude;
        private final BlockBehaviour.StatePredicate isRedstoneConductor;
        private final BlockBehaviour.StatePredicate isSuffocating;
        private final BlockBehaviour.StatePredicate isViewBlocking;
        private final BlockBehaviour.StatePredicate hasPostProcess;
        private final BlockBehaviour.StatePredicate emissiveRendering;
        @Nullable
        protected BlockBehaviour.BlockStateBase.Cache cache;

        protected BlockStateBase(Block param0, ImmutableMap<Property<?>, Comparable<?>> param1) {
            super(param0, param1);
            BlockBehaviour.Properties var0 = param0.properties;
            this.lightEmission = var0.lightEmission.applyAsInt(this.asState());
            this.useShapeForLightOcclusion = param0.useShapeForLightOcclusion(this.asState());
            this.isAir = var0.isAir;
            this.material = var0.material;
            this.materialColor = var0.materialColor.apply(this.asState());
            this.destroySpeed = var0.destroyTime;
            this.canOcclude = var0.canOcclude;
            this.isRedstoneConductor = var0.isRedstoneConductor;
            this.isSuffocating = var0.isSuffocating;
            this.isViewBlocking = var0.isViewBlocking;
            this.hasPostProcess = var0.hasPostProcess;
            this.emissiveRendering = var0.emissiveRendering;
        }

        public void initCache() {
            if (!this.getBlock().hasDynamicShape()) {
                this.cache = new BlockBehaviour.BlockStateBase.Cache(this.asState());
            }

        }

        public Block getBlock() {
            return this.owner;
        }

        public Material getMaterial() {
            return this.material;
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

        public MaterialColor getMapColor(BlockGetter param0, BlockPos param1) {
            return this.materialColor;
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

        @OnlyIn(Dist.CLIENT)
        public boolean emissiveRendering(BlockGetter param0, BlockPos param1) {
            return this.emissiveRendering.test(this.asState(), param0, param1);
        }

        @OnlyIn(Dist.CLIENT)
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
            return this.getBlock().getPistonPushReaction(this.asState());
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

        @OnlyIn(Dist.CLIENT)
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
            BlockBehaviour.OffsetType var0 = this.getBlock().getOffsetType();
            if (var0 == BlockBehaviour.OffsetType.NONE) {
                return Vec3.ZERO;
            } else {
                long var1 = Mth.getSeed(param1.getX(), 0, param1.getZ());
                return new Vec3(
                    ((double)((float)(var1 & 15L) / 15.0F) - 0.5) * 0.5,
                    var0 == BlockBehaviour.OffsetType.XYZ ? ((double)((float)(var1 >> 4 & 15L) / 15.0F) - 1.0) * 0.2 : 0.0,
                    ((double)((float)(var1 >> 8 & 15L) / 15.0F) - 0.5) * 0.5
                );
            }
        }

        public boolean triggerEvent(Level param0, BlockPos param1, int param2, int param3) {
            return this.getBlock().triggerEvent(this.asState(), param0, param1, param2, param3);
        }

        public void neighborChanged(Level param0, BlockPos param1, Block param2, BlockPos param3, boolean param4) {
            this.getBlock().neighborChanged(this.asState(), param0, param1, param2, param3, param4);
        }

        public final void updateNeighbourShapes(LevelAccessor param0, BlockPos param1, int param2) {
            this.getBlock();
            BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

            for(Direction var1 : BlockBehaviour.UPDATE_SHAPE_ORDER) {
                var0.setWithOffset(param1, var1);
                BlockState var2 = param0.getBlockState(var0);
                BlockState var3 = var2.updateShape(var1.getOpposite(), this.asState(), param0, var0, param1);
                Block.updateOrDestroy(var2, var3, param0, var0, param2);
            }

        }

        public void updateIndirectNeighbourShapes(LevelAccessor param0, BlockPos param1, int param2) {
            this.getBlock().updateIndirectNeighbourShapes(this.asState(), param0, param1, param2);
        }

        public void onPlace(Level param0, BlockPos param1, BlockState param2, boolean param3) {
            this.getBlock().onPlace(this.asState(), param0, param1, param2, param3);
        }

        public void onRemove(Level param0, BlockPos param1, BlockState param2, boolean param3) {
            this.getBlock().onRemove(this.asState(), param0, param1, param2, param3);
        }

        public void tick(ServerLevel param0, BlockPos param1, Random param2) {
            this.getBlock().tick(this.asState(), param0, param1, param2);
        }

        public void randomTick(ServerLevel param0, BlockPos param1, Random param2) {
            this.getBlock().randomTick(this.asState(), param0, param1, param2);
        }

        public void entityInside(Level param0, BlockPos param1, Entity param2) {
            this.getBlock().entityInside(this.asState(), param0, param1, param2);
        }

        public void spawnAfterBreak(Level param0, BlockPos param1, ItemStack param2) {
            this.getBlock().spawnAfterBreak(this.asState(), param0, param1, param2);
        }

        public List<ItemStack> getDrops(LootContext.Builder param0) {
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

        @OnlyIn(Dist.CLIENT)
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

        public boolean is(Tag<Block> param0) {
            return this.getBlock().is(param0);
        }

        public boolean is(Tag<Block> param0, Predicate<BlockBehaviour.BlockStateBase> param1) {
            return this.getBlock().is(param0) && param1.test(this);
        }

        public boolean is(Block param0) {
            return this.getBlock().is(param0);
        }

        public FluidState getFluidState() {
            return this.getBlock().getFluidState(this.asState());
        }

        public boolean isRandomlyTicking() {
            return this.getBlock().isRandomlyTicking(this.asState());
        }

        @OnlyIn(Dist.CLIENT)
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
            return this.cache != null ? this.cache.isFaceSturdy[param2.ordinal()] : Block.isFaceSturdy(this.asState(), param0, param1, param2);
        }

        public boolean isCollisionShapeFullBlock(BlockGetter param0, BlockPos param1) {
            return this.cache != null ? this.cache.isCollisionShapeFullBlock : Block.isShapeFullBlock(this.getCollisionShape(param0, param1));
        }

        protected abstract BlockState asState();

        static final class Cache {
            private static final Direction[] DIRECTIONS = Direction.values();
            protected final boolean solidRender;
            private final boolean propagatesSkylightDown;
            private final int lightBlock;
            @Nullable
            private final VoxelShape[] occlusionShapes;
            protected final VoxelShape collisionShape;
            protected final boolean largeCollisionShape;
            protected final boolean[] isFaceSturdy;
            protected final boolean isCollisionShapeFullBlock;

            private Cache(BlockState param0) {
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
                this.largeCollisionShape = Arrays.stream(Direction.Axis.values())
                    .anyMatch(param0x -> this.collisionShape.min(param0x) < 0.0 || this.collisionShape.max(param0x) > 1.0);
                this.isFaceSturdy = new boolean[6];

                for(Direction var3 : DIRECTIONS) {
                    this.isFaceSturdy[var3.ordinal()] = Block.isFaceSturdy(param0, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, var3);
                }

                this.isCollisionShapeFullBlock = Block.isShapeFullBlock(param0.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
            }
        }
    }

    public static enum OffsetType {
        NONE,
        XZ,
        XYZ;
    }

    public static class Properties {
        private Material material;
        private Function<BlockState, MaterialColor> materialColor;
        private boolean hasCollision = true;
        private SoundType soundType = SoundType.STONE;
        private ToIntFunction<BlockState> lightEmission = param0x -> 0;
        private float explosionResistance;
        private float destroyTime;
        private boolean isRandomlyTicking;
        private float friction = 0.6F;
        private float speedFactor = 1.0F;
        private float jumpFactor = 1.0F;
        private ResourceLocation drops;
        private boolean canOcclude = true;
        private boolean isAir;
        private BlockBehaviour.StateArgumentPredicate<EntityType<?>> isValidSpawn = (param0x, param1x, param2, param3) -> param0x.isFaceSturdy(
                    param1x, param2, Direction.UP
                )
                && param0x.getLightEmission() < 14;
        private BlockBehaviour.StatePredicate isRedstoneConductor = (param0x, param1x, param2) -> param0x.getMaterial().isSolidBlocking()
                && param0x.isCollisionShapeFullBlock(param1x, param2);
        private BlockBehaviour.StatePredicate isSuffocating = (param0x, param1x, param2) -> this.material.blocksMotion()
                && param0x.isCollisionShapeFullBlock(param1x, param2);
        private BlockBehaviour.StatePredicate isViewBlocking = this.isSuffocating;
        private BlockBehaviour.StatePredicate hasPostProcess = (param0x, param1x, param2) -> false;
        private BlockBehaviour.StatePredicate emissiveRendering = (param0x, param1x, param2) -> false;
        private boolean dynamicShape;

        private Properties(Material param0, MaterialColor param1) {
            this(param0, param1x -> param1);
        }

        private Properties(Material param0, Function<BlockState, MaterialColor> param1) {
            this.material = param0;
            this.materialColor = param1;
        }

        public static BlockBehaviour.Properties of(Material param0) {
            return of(param0, param0.getColor());
        }

        public static BlockBehaviour.Properties of(Material param0, DyeColor param1) {
            return of(param0, param1.getMaterialColor());
        }

        public static BlockBehaviour.Properties of(Material param0, MaterialColor param1) {
            return new BlockBehaviour.Properties(param0, param1);
        }

        public static BlockBehaviour.Properties of(Material param0, Function<BlockState, MaterialColor> param1) {
            return new BlockBehaviour.Properties(param0, param1);
        }

        public static BlockBehaviour.Properties copy(BlockBehaviour param0) {
            BlockBehaviour.Properties var0 = new BlockBehaviour.Properties(param0.material, param0.properties.materialColor);
            var0.material = param0.properties.material;
            var0.destroyTime = param0.properties.destroyTime;
            var0.explosionResistance = param0.properties.explosionResistance;
            var0.hasCollision = param0.properties.hasCollision;
            var0.isRandomlyTicking = param0.properties.isRandomlyTicking;
            var0.lightEmission = param0.properties.lightEmission;
            var0.materialColor = param0.properties.materialColor;
            var0.soundType = param0.properties.soundType;
            var0.friction = param0.properties.friction;
            var0.speedFactor = param0.properties.speedFactor;
            var0.dynamicShape = param0.properties.dynamicShape;
            var0.canOcclude = param0.properties.canOcclude;
            var0.isAir = param0.properties.isAir;
            return var0;
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
            this.destroyTime = param0;
            this.explosionResistance = Math.max(0.0F, param1);
            return this;
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

        public BlockBehaviour.Properties noDrops() {
            this.drops = BuiltInLootTables.EMPTY;
            return this;
        }

        public BlockBehaviour.Properties dropsLike(Block param0) {
            this.drops = param0.getLootTable();
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
    }

    public interface StateArgumentPredicate<A> {
        boolean test(BlockState var1, BlockGetter var2, BlockPos var3, A var4);
    }

    public interface StatePredicate {
        boolean test(BlockState var1, BlockGetter var2, BlockPos var3);
    }
}
