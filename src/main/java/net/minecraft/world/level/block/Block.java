package net.minecraft.world.level.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Block extends BlockBehaviour implements ItemLike {
    protected static final Logger LOGGER = LogManager.getLogger();
    public static final IdMapper<BlockState> BLOCK_STATE_REGISTRY = new IdMapper<>();
    private static final LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder()
        .maximumSize(512L)
        .weakKeys()
        .build(new CacheLoader<VoxelShape, Boolean>() {
            public Boolean load(VoxelShape param0) {
                return !Shapes.joinIsNotEmpty(Shapes.block(), param0, BooleanOp.NOT_SAME);
            }
        });
    protected final StateDefinition<Block, BlockState> stateDefinition;
    private BlockState defaultBlockState;
    @Nullable
    private String descriptionId;
    @Nullable
    private Item item;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> var0 = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(2048, 0.25F) {
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
            var2.teleportTo(var2.getX(), var2.getY() + 1.0 + var3, var2.getZ());
        }

        return param1;
    }

    public static VoxelShape box(double param0, double param1, double param2, double param3, double param4, double param5) {
        return Shapes.box(param0 / 16.0, param1 / 16.0, param2 / 16.0, param3 / 16.0, param4 / 16.0, param5 / 16.0);
    }

    public static BlockState updateFromNeighbourShapes(BlockState param0, LevelAccessor param1, BlockPos param2) {
        BlockState var0 = param0;
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(Direction var2 : UPDATE_SHAPE_ORDER) {
            var1.setWithOffset(param2, var2);
            var0 = var0.updateShape(var2, param1.getBlockState(var1), param1, param2, var1);
        }

        return var0;
    }

    public static void updateOrDestroy(BlockState param0, BlockState param1, LevelAccessor param2, BlockPos param3, int param4) {
        updateOrDestroy(param0, param1, param2, param3, param4, 512);
    }

    public static void updateOrDestroy(BlockState param0, BlockState param1, LevelAccessor param2, BlockPos param3, int param4, int param5) {
        if (param1 != param0) {
            if (param1.isAir()) {
                if (!param2.isClientSide()) {
                    param2.destroyBlock(param3, (param4 & 32) == 0, null, param5);
                }
            } else {
                param2.setBlock(param3, param1, param4 & -33, param5);
            }
        }

    }

    public Block(BlockBehaviour.Properties param0) {
        super(param0);
        StateDefinition.Builder<Block, BlockState> var0 = new StateDefinition.Builder<>(this);
        this.createBlockStateDefinition(var0);
        this.stateDefinition = var0.create(Block::defaultBlockState, BlockState::new);
        this.registerDefaultState(this.stateDefinition.any());
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            String var1 = this.getClass().getSimpleName();
            if (!var1.endsWith("Block")) {
                LOGGER.error("Block classes should end with Block and {} doesn't.", var1);
            }
        }

    }

    public static boolean isExceptionForConnection(BlockState param0) {
        return param0.getBlock() instanceof LeavesBlock
            || param0.is(Blocks.BARRIER)
            || param0.is(Blocks.CARVED_PUMPKIN)
            || param0.is(Blocks.JACK_O_LANTERN)
            || param0.is(Blocks.MELON)
            || param0.is(Blocks.PUMPKIN)
            || param0.is(BlockTags.SHULKER_BOXES);
    }

    public boolean isRandomlyTicking(BlockState param0) {
        return this.isRandomlyTicking;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean shouldRenderFace(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3, BlockPos param4) {
        BlockState var0 = param1.getBlockState(param4);
        if (param0.skipRendering(var0, param3)) {
            return false;
        } else if (var0.canOcclude()) {
            Block.BlockStatePairKey var1 = new Block.BlockStatePairKey(param0, var0, param3);
            Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> var2 = OCCLUSION_CACHE.get();
            byte var3 = var2.getAndMoveToFirst(var1);
            if (var3 != 127) {
                return var3 != 0;
            } else {
                VoxelShape var4 = param0.getFaceOcclusionShape(param1, param2, param3);
                if (var4.isEmpty()) {
                    return true;
                } else {
                    VoxelShape var5 = var0.getFaceOcclusionShape(param1, param4, param3.getOpposite());
                    boolean var6 = Shapes.joinIsNotEmpty(var4, var5, BooleanOp.ONLY_FIRST);
                    if (var2.size() == 2048) {
                        var2.removeLastByte();
                    }

                    var2.putAndMoveToFirst(var1, (byte)(var6 ? 1 : 0));
                    return var6;
                }
            }
        } else {
            return true;
        }
    }

    public static boolean canSupportRigidBlock(BlockGetter param0, BlockPos param1) {
        return param0.getBlockState(param1).isFaceSturdy(param0, param1, Direction.UP, SupportType.RIGID);
    }

    public static boolean canSupportCenter(LevelReader param0, BlockPos param1, Direction param2) {
        BlockState var0 = param0.getBlockState(param1);
        return param2 == Direction.DOWN && var0.is(BlockTags.UNSTABLE_BOTTOM_CENTER) ? false : var0.isFaceSturdy(param0, param1, param2, SupportType.CENTER);
    }

    public static boolean isFaceFull(VoxelShape param0, Direction param1) {
        VoxelShape var0 = param0.getFaceShape(param1);
        return isShapeFullBlock(var0);
    }

    public static boolean isShapeFullBlock(VoxelShape param0) {
        return SHAPE_FULL_BLOCK_CACHE.getUnchecked(param0);
    }

    public boolean propagatesSkylightDown(BlockState param0, BlockGetter param1, BlockPos param2) {
        return !isShapeFullBlock(param0.getShape(param1, param2)) && param0.getFluidState().isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
    }

    public void destroy(LevelAccessor param0, BlockPos param1, BlockState param2) {
    }

    public static List<ItemStack> getDrops(BlockState param0, ServerLevel param1, BlockPos param2, @Nullable BlockEntity param3) {
        LootContext.Builder var0 = new LootContext.Builder(param1)
            .withRandom(param1.random)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(param2))
            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, param3);
        return param0.getDrops(var0);
    }

    public static List<ItemStack> getDrops(
        BlockState param0, ServerLevel param1, BlockPos param2, @Nullable BlockEntity param3, @Nullable Entity param4, ItemStack param5
    ) {
        LootContext.Builder var0 = new LootContext.Builder(param1)
            .withRandom(param1.random)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(param2))
            .withParameter(LootContextParams.TOOL, param5)
            .withOptionalParameter(LootContextParams.THIS_ENTITY, param4)
            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, param3);
        return param0.getDrops(var0);
    }

    public static void dropResources(BlockState param0, Level param1, BlockPos param2) {
        if (param1 instanceof ServerLevel) {
            getDrops(param0, (ServerLevel)param1, param2, null).forEach(param2x -> popResource(param1, param2, param2x));
            param0.spawnAfterBreak((ServerLevel)param1, param2, ItemStack.EMPTY);
        }

    }

    public static void dropResources(BlockState param0, LevelAccessor param1, BlockPos param2, @Nullable BlockEntity param3) {
        if (param1 instanceof ServerLevel) {
            getDrops(param0, (ServerLevel)param1, param2, param3).forEach(param2x -> popResource((ServerLevel)param1, param2, param2x));
            param0.spawnAfterBreak((ServerLevel)param1, param2, ItemStack.EMPTY);
        }

    }

    public static void dropResources(BlockState param0, Level param1, BlockPos param2, @Nullable BlockEntity param3, Entity param4, ItemStack param5) {
        if (param1 instanceof ServerLevel) {
            getDrops(param0, (ServerLevel)param1, param2, param3, param4, param5).forEach(param2x -> popResource(param1, param2, param2x));
            param0.spawnAfterBreak((ServerLevel)param1, param2, param5);
        }

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

    protected void popExperience(ServerLevel param0, BlockPos param1, int param2) {
        if (param0.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            ExperienceOrb.award(param0, Vec3.atCenterOf(param1), param2);
        }

    }

    public float getExplosionResistance() {
        return this.explosionResistance;
    }

    public void wasExploded(Level param0, BlockPos param1, Explosion param2) {
    }

    public void stepOn(Level param0, BlockPos param1, Entity param2) {
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState();
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
    public MutableComponent getName() {
        return new TranslatableComponent(this.getDescriptionId());
    }

    public String getDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("block", Registry.BLOCK.getKey(this));
        }

        return this.descriptionId;
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

    public float getFriction() {
        return this.friction;
    }

    public float getSpeedFactor() {
        return this.speedFactor;
    }

    public float getJumpFactor() {
        return this.jumpFactor;
    }

    public void playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        param0.levelEvent(param3, 2001, param1, getId(param2));
        if (param2.is(BlockTags.GUARDED_BY_PIGLINS)) {
            PiglinAi.angerNearbyPiglins(param3, false);
        }

    }

    public void handlePrecipitation(BlockState param0, Level param1, BlockPos param2, Biome.Precipitation param3) {
    }

    public boolean dropFromExplosion(Explosion param0) {
        return true;
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

    @Override
    protected Block asBlock() {
        return this;
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
}
