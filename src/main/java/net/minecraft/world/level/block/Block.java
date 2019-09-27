package net.minecraft.world.level.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
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
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Block implements ItemLike {
    protected static final Logger LOGGER = LogManager.getLogger();
    public static final IdMapper<BlockState> BLOCK_STATE_REGISTRY = new IdMapper<>();
    private static final Direction[] UPDATE_SHAPE_ORDER = new Direction[]{
        Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP
    };
    private static final LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder()
        .maximumSize(512L)
        .weakKeys()
        .build(new CacheLoader<VoxelShape, Boolean>() {
            public Boolean load(VoxelShape param0) {
                return !Shapes.joinIsNotEmpty(Shapes.block(), param0, BooleanOp.NOT_SAME);
            }
        });
    private static final VoxelShape RIGID_SUPPORT_SHAPE = Shapes.join(Shapes.block(), box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), BooleanOp.ONLY_FIRST);
    private static final VoxelShape CENTER_SUPPORT_SHAPE = box(7.0, 0.0, 7.0, 9.0, 10.0, 9.0);
    protected final int lightEmission;
    protected final float destroySpeed;
    protected final float explosionResistance;
    protected final boolean isTicking;
    protected final SoundType soundType;
    protected final Material material;
    protected final MaterialColor materialColor;
    private final float friction;
    protected final StateDefinition<Block, BlockState> stateDefinition;
    private BlockState defaultBlockState;
    protected final boolean hasCollision;
    private final boolean dynamicShape;
    private final boolean canOcclude;
    @Nullable
    private ResourceLocation drops;
    @Nullable
    private String descriptionId;
    @Nullable
    private Item item;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> var0 = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(200) {
            @Override
            protected void rehash(int param0) {
            }
        };
        var0.defaultReturnValue((byte)127);
        return var0;
    });

    public static int getId(@Nullable BlockState param0) {
        if (param0 == null) {
            return 0;
        } else {
            int var0 = BLOCK_STATE_REGISTRY.getId(param0);
            return var0 == -1 ? 0 : var0;
        }
    }

    public static BlockState stateById(int param0) {
        BlockState var0 = BLOCK_STATE_REGISTRY.byId(param0);
        return var0 == null ? Blocks.AIR.defaultBlockState() : var0;
    }

    public static Block byItem(@Nullable Item param0) {
        return param0 instanceof BlockItem ? ((BlockItem)param0).getBlock() : Blocks.AIR;
    }

    public static BlockState pushEntitiesUp(BlockState param0, BlockState param1, Level param2, BlockPos param3) {
        VoxelShape var0 = Shapes.joinUnoptimized(param0.getCollisionShape(param2, param3), param1.getCollisionShape(param2, param3), BooleanOp.ONLY_SECOND)
            .move((double)param3.getX(), (double)param3.getY(), (double)param3.getZ());

        for(Entity var2 : param2.getEntities(null, var0.bounds())) {
            double var3 = Shapes.collide(Direction.Axis.Y, var2.getBoundingBox().move(0.0, 1.0, 0.0), Stream.of(var0), -1.0);
            var2.teleportTo(var2.x, var2.y + 1.0 + var3, var2.z);
        }

        return param1;
    }

    public static VoxelShape box(double param0, double param1, double param2, double param3, double param4, double param5) {
        return Shapes.box(param0 / 16.0, param1 / 16.0, param2 / 16.0, param3 / 16.0, param4 / 16.0, param5 / 16.0);
    }

    @Deprecated
    public boolean isValidSpawn(BlockState param0, BlockGetter param1, BlockPos param2, EntityType<?> param3) {
        return param0.isFaceSturdy(param1, param2, Direction.UP) && this.lightEmission < 14;
    }

    @Deprecated
    public boolean isAir(BlockState param0) {
        return false;
    }

    @Deprecated
    public int getLightEmission(BlockState param0) {
        return this.lightEmission;
    }

    @Deprecated
    public Material getMaterial(BlockState param0) {
        return this.material;
    }

    @Deprecated
    public MaterialColor getMapColor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return this.materialColor;
    }

    @Deprecated
    public void updateNeighbourShapes(BlockState param0, LevelAccessor param1, BlockPos param2, int param3) {
        try (BlockPos.PooledMutableBlockPos var0 = BlockPos.PooledMutableBlockPos.acquire()) {
            for(Direction var1 : UPDATE_SHAPE_ORDER) {
                var0.set(param2).move(var1);
                BlockState var2 = param1.getBlockState(var0);
                BlockState var3 = var2.updateShape(var1.getOpposite(), param0, param1, var0, param2);
                updateOrDestroy(var2, var3, param1, var0, param3);
            }
        }

    }

    public boolean is(Tag<Block> param0) {
        return param0.contains(this);
    }

    public static BlockState updateFromNeighbourShapes(BlockState param0, LevelAccessor param1, BlockPos param2) {
        BlockState var0 = param0;
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(Direction var2 : UPDATE_SHAPE_ORDER) {
            var1.set(param2).move(var2);
            var0 = var0.updateShape(var2, param1.getBlockState(var1), param1, param2, var1);
        }

        return var0;
    }

    public static void updateOrDestroy(BlockState param0, BlockState param1, LevelAccessor param2, BlockPos param3, int param4) {
        if (param1 != param0) {
            if (param1.isAir()) {
                if (!param2.isClientSide()) {
                    param2.destroyBlock(param3, (param4 & 32) == 0);
                }
            } else {
                param2.setBlock(param3, param1, param4 & -33);
            }
        }

    }

    @Deprecated
    public void updateIndirectNeighbourShapes(BlockState param0, LevelAccessor param1, BlockPos param2, int param3) {
    }

    @Deprecated
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param0;
    }

    @Deprecated
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0;
    }

    @Deprecated
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0;
    }

    public Block(Block.Properties param0) {
        StateDefinition.Builder<Block, BlockState> var0 = new StateDefinition.Builder<>(this);
        this.createBlockStateDefinition(var0);
        this.material = param0.material;
        this.materialColor = param0.materialColor;
        this.hasCollision = param0.hasCollision;
        this.soundType = param0.soundType;
        this.lightEmission = param0.lightEmission;
        this.explosionResistance = param0.explosionResistance;
        this.destroySpeed = param0.destroyTime;
        this.isTicking = param0.isTicking;
        this.friction = param0.friction;
        this.dynamicShape = param0.dynamicShape;
        this.drops = param0.drops;
        this.canOcclude = param0.canOcclude;
        this.stateDefinition = var0.create(BlockState::new);
        this.registerDefaultState(this.stateDefinition.any());
    }

    public static boolean isExceptionForConnection(Block param0) {
        return param0 instanceof LeavesBlock
            || param0 == Blocks.BARRIER
            || param0 == Blocks.CARVED_PUMPKIN
            || param0 == Blocks.JACK_O_LANTERN
            || param0 == Blocks.MELON
            || param0 == Blocks.PUMPKIN;
    }

    @Deprecated
    public boolean isRedstoneConductor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.getMaterial().isSolidBlocking() && param0.isCollisionShapeFullBlock(param1, param2) && !param0.isSignalSource();
    }

    @Deprecated
    public boolean isViewBlocking(BlockState param0, BlockGetter param1, BlockPos param2) {
        return this.material.blocksMotion() && param0.isCollisionShapeFullBlock(param1, param2);
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
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
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
    public float getDestroySpeed(BlockState param0, BlockGetter param1, BlockPos param2) {
        return this.destroySpeed;
    }

    public boolean isRandomlyTicking(BlockState param0) {
        return this.isTicking;
    }

    public boolean isEntityBlock() {
        return this instanceof EntityBlock;
    }

    @Deprecated
    public boolean hasPostProcess(BlockState param0, BlockGetter param1, BlockPos param2) {
        return false;
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public boolean emissiveRendering(BlockState param0) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean shouldRenderFace(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        BlockPos var0 = param2.relative(param3);
        BlockState var1 = param1.getBlockState(var0);
        if (param0.skipRendering(var1, param3)) {
            return false;
        } else if (var1.canOcclude()) {
            Block.BlockStatePairKey var2 = new Block.BlockStatePairKey(param0, var1, param3);
            Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> var3 = OCCLUSION_CACHE.get();
            byte var4 = var3.getAndMoveToFirst(var2);
            if (var4 != 127) {
                return var4 != 0;
            } else {
                VoxelShape var5 = param0.getFaceOcclusionShape(param1, param2, param3);
                VoxelShape var6 = var1.getFaceOcclusionShape(param1, var0, param3.getOpposite());
                boolean var7 = Shapes.joinIsNotEmpty(var5, var6, BooleanOp.ONLY_FIRST);
                if (var3.size() == 200) {
                    var3.removeLastByte();
                }

                var3.putAndMoveToFirst(var2, (byte)(var7 ? 1 : 0));
                return var7;
            }
        } else {
            return true;
        }
    }

    @Deprecated
    public final boolean canOcclude(BlockState param0) {
        return this.canOcclude;
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public boolean skipRendering(BlockState param0, BlockState param1, Direction param2) {
        return false;
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
    public VoxelShape getOcclusionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.getShape(param1, param2);
    }

    @Deprecated
    public VoxelShape getInteractionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return Shapes.empty();
    }

    public static boolean canSupportRigidBlock(BlockGetter param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        return !var0.is(BlockTags.LEAVES)
            && !Shapes.joinIsNotEmpty(var0.getCollisionShape(param0, param1).getFaceShape(Direction.UP), RIGID_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
    }

    public static boolean canSupportCenter(LevelReader param0, BlockPos param1, Direction param2) {
        BlockState var0 = param0.getBlockState(param1);
        return !var0.is(BlockTags.LEAVES)
            && !Shapes.joinIsNotEmpty(var0.getCollisionShape(param0, param1).getFaceShape(param2), CENTER_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
    }

    public static boolean isFaceSturdy(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return !param0.is(BlockTags.LEAVES) && isFaceFull(param0.getCollisionShape(param1, param2), param3);
    }

    public static boolean isFaceFull(VoxelShape param0, Direction param1) {
        VoxelShape var0 = param0.getFaceShape(param1);
        return isShapeFullBlock(var0);
    }

    public static boolean isShapeFullBlock(VoxelShape param0) {
        return SHAPE_FULL_BLOCK_CACHE.getUnchecked(param0);
    }

    @Deprecated
    public final boolean isSolidRender(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.canOcclude() ? isShapeFullBlock(param0.getOcclusionShape(param1, param2)) : false;
    }

    public boolean propagatesSkylightDown(BlockState param0, BlockGetter param1, BlockPos param2) {
        return !isShapeFullBlock(param0.getShape(param1, param2)) && param0.getFluidState().isEmpty();
    }

    @Deprecated
    public int getLightBlock(BlockState param0, BlockGetter param1, BlockPos param2) {
        if (param0.isSolidRender(param1, param2)) {
            return param1.getMaxLightLevel();
        } else {
            return param0.propagatesSkylightDown(param1, param2) ? 0 : 1;
        }
    }

    @Deprecated
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return false;
    }

    @Deprecated
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        this.tick(param0, param1, param2, param3);
    }

    @Deprecated
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
    }

    public void destroy(LevelAccessor param0, BlockPos param1, BlockState param2) {
    }

    @Deprecated
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        DebugPackets.sendNeighborsUpdatePacket(param1, param2);
    }

    public int getTickDelay(LevelReader param0) {
        return 10;
    }

    @Nullable
    @Deprecated
    public MenuProvider getMenuProvider(BlockState param0, Level param1, BlockPos param2) {
        return null;
    }

    @Deprecated
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
    }

    @Deprecated
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (this.isEntityBlock() && param0.getBlock() != param3.getBlock()) {
            param1.removeBlockEntity(param2);
        }

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

    public ResourceLocation getLootTable() {
        if (this.drops == null) {
            ResourceLocation var0 = Registry.BLOCK.getKey(this);
            this.drops = new ResourceLocation(var0.getNamespace(), "blocks/" + var0.getPath());
        }

        return this.drops;
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

    public static List<ItemStack> getDrops(BlockState param0, ServerLevel param1, BlockPos param2, @Nullable BlockEntity param3) {
        LootContext.Builder var0 = new LootContext.Builder(param1)
            .withRandom(param1.random)
            .withParameter(LootContextParams.BLOCK_POS, param2)
            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, param3);
        return param0.getDrops(var0);
    }

    public static List<ItemStack> getDrops(
        BlockState param0, ServerLevel param1, BlockPos param2, @Nullable BlockEntity param3, @Nullable Entity param4, ItemStack param5
    ) {
        LootContext.Builder var0 = new LootContext.Builder(param1)
            .withRandom(param1.random)
            .withParameter(LootContextParams.BLOCK_POS, param2)
            .withParameter(LootContextParams.TOOL, param5)
            .withOptionalParameter(LootContextParams.THIS_ENTITY, param4)
            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, param3);
        return param0.getDrops(var0);
    }

    public static void dropResources(BlockState param0, LootContext.Builder param1) {
        ServerLevel var0 = param1.getLevel();
        BlockPos var1 = param1.getParameter(LootContextParams.BLOCK_POS);
        param0.getDrops(param1).forEach(param2 -> popResource(var0, var1, param2));
        param0.spawnAfterBreak(var0, var1, ItemStack.EMPTY);
    }

    public static void dropResources(BlockState param0, Level param1, BlockPos param2) {
        if (param1 instanceof ServerLevel) {
            getDrops(param0, (ServerLevel)param1, param2, null).forEach(param2x -> popResource(param1, param2, param2x));
        }

        param0.spawnAfterBreak(param1, param2, ItemStack.EMPTY);
    }

    public static void dropResources(BlockState param0, Level param1, BlockPos param2, @Nullable BlockEntity param3) {
        if (param1 instanceof ServerLevel) {
            getDrops(param0, (ServerLevel)param1, param2, param3).forEach(param2x -> popResource(param1, param2, param2x));
        }

        param0.spawnAfterBreak(param1, param2, ItemStack.EMPTY);
    }

    public static void dropResources(BlockState param0, Level param1, BlockPos param2, @Nullable BlockEntity param3, Entity param4, ItemStack param5) {
        if (param1 instanceof ServerLevel) {
            getDrops(param0, (ServerLevel)param1, param2, param3, param4, param5).forEach(param2x -> popResource(param1, param2, param2x));
        }

        param0.spawnAfterBreak(param1, param2, param5);
    }

    public static void popResource(Level param0, BlockPos param1, ItemStack param2) {
        if (!param0.isClientSide && !param2.isEmpty() && param0.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            float var0 = 0.5F;
            double var1 = (double)(param0.random.nextFloat() * 0.5F) + 0.25;
            double var2 = (double)(param0.random.nextFloat() * 0.5F) + 0.25;
            double var3 = (double)(param0.random.nextFloat() * 0.5F) + 0.25;
            ItemEntity var4 = new ItemEntity(param0, (double)param1.getX() + var1, (double)param1.getY() + var2, (double)param1.getZ() + var3, param2);
            var4.setDefaultPickUpDelay();
            param0.addFreshEntity(var4);
        }
    }

    protected void popExperience(Level param0, BlockPos param1, int param2) {
        if (!param0.isClientSide && param0.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            while(param2 > 0) {
                int var0 = ExperienceOrb.getExperienceValue(param2);
                param2 -= var0;
                param0.addFreshEntity(new ExperienceOrb(param0, (double)param1.getX() + 0.5, (double)param1.getY() + 0.5, (double)param1.getZ() + 0.5, var0));
            }
        }

    }

    public float getExplosionResistance() {
        return this.explosionResistance;
    }

    public void wasExploded(Level param0, BlockPos param1, Explosion param2) {
    }

    @Deprecated
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return true;
    }

    @Deprecated
    public boolean use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        return false;
    }

    public void stepOn(Level param0, BlockPos param1, Entity param2) {
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState();
    }

    @Deprecated
    public void attack(BlockState param0, Level param1, BlockPos param2, Player param3) {
    }

    @Deprecated
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return 0;
    }

    @Deprecated
    public boolean isSignalSource(BlockState param0) {
        return false;
    }

    @Deprecated
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
    }

    @Deprecated
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return 0;
    }

    public void playerDestroy(Level param0, Player param1, BlockPos param2, BlockState param3, @Nullable BlockEntity param4, ItemStack param5) {
        param1.awardStat(Stats.BLOCK_MINED.get(this));
        param1.causeFoodExhaustion(0.005F);
        dropResources(param3, param0, param2, param4, param1, param5);
    }

    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, @Nullable LivingEntity param3, ItemStack param4) {
    }

    public boolean isPossibleToRespawnInThis() {
        return !this.material.isSolid() && !this.material.isLiquid();
    }

    @OnlyIn(Dist.CLIENT)
    public Component getName() {
        return new TranslatableComponent(this.getDescriptionId());
    }

    public String getDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("block", Registry.BLOCK.getKey(this));
        }

        return this.descriptionId;
    }

    @Deprecated
    public boolean triggerEvent(BlockState param0, Level param1, BlockPos param2, int param3, int param4) {
        return false;
    }

    @Deprecated
    public PushReaction getPistonPushReaction(BlockState param0) {
        return this.material.getPushReaction();
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.isCollisionShapeFullBlock(param1, param2) ? 0.2F : 1.0F;
    }

    public void fallOn(Level param0, BlockPos param1, Entity param2, float param3) {
        param2.causeFallDamage(param3, 1.0F);
    }

    public void updateEntityAfterFallOn(BlockGetter param0, Entity param1) {
        param1.setDeltaMovement(param1.getDeltaMovement().multiply(1.0, 0.0, 1.0));
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        return new ItemStack(this);
    }

    public void fillItemCategory(CreativeModeTab param0, NonNullList<ItemStack> param1) {
        param1.add(new ItemStack(this));
    }

    @Deprecated
    public FluidState getFluidState(BlockState param0) {
        return Fluids.EMPTY.defaultFluidState();
    }

    public float getFriction() {
        return this.friction;
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public long getSeed(BlockState param0, BlockPos param1) {
        return Mth.getSeed(param1);
    }

    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Entity param3) {
    }

    public void playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        param0.levelEvent(param3, 2001, param1, getId(param2));
    }

    public void handleRain(Level param0, BlockPos param1) {
    }

    public boolean dropFromExplosion(Explosion param0) {
        return true;
    }

    @Deprecated
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return false;
    }

    @Deprecated
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return 0;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
    }

    public StateDefinition<Block, BlockState> getStateDefinition() {
        return this.stateDefinition;
    }

    protected final void registerDefaultState(BlockState param0) {
        this.defaultBlockState = param0;
    }

    public final BlockState defaultBlockState() {
        return this.defaultBlockState;
    }

    public Block.OffsetType getOffsetType() {
        return Block.OffsetType.NONE;
    }

    @Deprecated
    public Vec3 getOffset(BlockState param0, BlockGetter param1, BlockPos param2) {
        Block.OffsetType var0 = this.getOffsetType();
        if (var0 == Block.OffsetType.NONE) {
            return Vec3.ZERO;
        } else {
            long var1 = Mth.getSeed(param2.getX(), 0, param2.getZ());
            return new Vec3(
                ((double)((float)(var1 & 15L) / 15.0F) - 0.5) * 0.5,
                var0 == Block.OffsetType.XYZ ? ((double)((float)(var1 >> 4 & 15L) / 15.0F) - 1.0) * 0.2 : 0.0,
                ((double)((float)(var1 >> 8 & 15L) / 15.0F) - 0.5) * 0.5
            );
        }
    }

    public SoundType getSoundType(BlockState param0) {
        return this.soundType;
    }

    @Override
    public Item asItem() {
        if (this.item == null) {
            this.item = Item.byBlock(this);
        }

        return this.item;
    }

    public boolean hasDynamicShape() {
        return this.dynamicShape;
    }

    @Override
    public String toString() {
        return "Block{" + Registry.BLOCK.getKey(this) + "}";
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack param0, @Nullable BlockGetter param1, List<Component> param2, TooltipFlag param3) {
    }

    public static boolean equalsStone(Block param0) {
        return param0 == Blocks.STONE || param0 == Blocks.GRANITE || param0 == Blocks.DIORITE || param0 == Blocks.ANDESITE;
    }

    public static boolean equalsDirt(Block param0) {
        return param0 == Blocks.DIRT || param0 == Blocks.COARSE_DIRT || param0 == Blocks.PODZOL;
    }

    public static final class BlockStatePairKey {
        private final BlockState first;
        private final BlockState second;
        private final Direction direction;

        public BlockStatePairKey(BlockState param0, BlockState param1, Direction param2) {
            this.first = param0;
            this.second = param1;
            this.direction = param2;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (!(param0 instanceof Block.BlockStatePairKey)) {
                return false;
            } else {
                Block.BlockStatePairKey var0 = (Block.BlockStatePairKey)param0;
                return this.first == var0.first && this.second == var0.second && this.direction == var0.direction;
            }
        }

        @Override
        public int hashCode() {
            int var0 = this.first.hashCode();
            var0 = 31 * var0 + this.second.hashCode();
            return 31 * var0 + this.direction.hashCode();
        }
    }

    public static enum OffsetType {
        NONE,
        XZ,
        XYZ;
    }

    public static class Properties {
        private Material material;
        private MaterialColor materialColor;
        private boolean hasCollision = true;
        private SoundType soundType = SoundType.STONE;
        private int lightEmission;
        private float explosionResistance;
        private float destroyTime;
        private boolean isTicking;
        private float friction = 0.6F;
        private ResourceLocation drops;
        private boolean canOcclude = true;
        private boolean dynamicShape;

        private Properties(Material param0, MaterialColor param1) {
            this.material = param0;
            this.materialColor = param1;
        }

        public static Block.Properties of(Material param0) {
            return of(param0, param0.getColor());
        }

        public static Block.Properties of(Material param0, DyeColor param1) {
            return of(param0, param1.getMaterialColor());
        }

        public static Block.Properties of(Material param0, MaterialColor param1) {
            return new Block.Properties(param0, param1);
        }

        public static Block.Properties copy(Block param0) {
            Block.Properties var0 = new Block.Properties(param0.material, param0.materialColor);
            var0.material = param0.material;
            var0.destroyTime = param0.destroySpeed;
            var0.explosionResistance = param0.explosionResistance;
            var0.hasCollision = param0.hasCollision;
            var0.isTicking = param0.isTicking;
            var0.lightEmission = param0.lightEmission;
            var0.materialColor = param0.materialColor;
            var0.soundType = param0.soundType;
            var0.friction = param0.getFriction();
            var0.dynamicShape = param0.dynamicShape;
            var0.canOcclude = param0.canOcclude;
            return var0;
        }

        public Block.Properties noCollission() {
            this.hasCollision = false;
            this.canOcclude = false;
            return this;
        }

        public Block.Properties noOcclusion() {
            this.canOcclude = false;
            return this;
        }

        public Block.Properties friction(float param0) {
            this.friction = param0;
            return this;
        }

        protected Block.Properties sound(SoundType param0) {
            this.soundType = param0;
            return this;
        }

        protected Block.Properties lightLevel(int param0) {
            this.lightEmission = param0;
            return this;
        }

        public Block.Properties strength(float param0, float param1) {
            this.destroyTime = param0;
            this.explosionResistance = Math.max(0.0F, param1);
            return this;
        }

        protected Block.Properties instabreak() {
            return this.strength(0.0F);
        }

        protected Block.Properties strength(float param0) {
            this.strength(param0, param0);
            return this;
        }

        protected Block.Properties randomTicks() {
            this.isTicking = true;
            return this;
        }

        protected Block.Properties dynamicShape() {
            this.dynamicShape = true;
            return this;
        }

        protected Block.Properties noDrops() {
            this.drops = BuiltInLootTables.EMPTY;
            return this;
        }

        public Block.Properties dropsLike(Block param0) {
            this.drops = param0.getLootTable();
            return this;
        }
    }
}
