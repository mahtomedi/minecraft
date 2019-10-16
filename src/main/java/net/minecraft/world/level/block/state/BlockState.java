package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockState extends AbstractStateHolder<Block, BlockState> implements StateHolder<BlockState> {
    @Nullable
    private BlockState.Cache cache;
    private final int lightEmission;
    private final boolean useShapeForLightOcclusion;

    public BlockState(Block param0, ImmutableMap<Property<?>, Comparable<?>> param1) {
        super(param0, param1);
        this.lightEmission = param0.getLightEmission(this);
        this.useShapeForLightOcclusion = param0.useShapeForLightOcclusion(this);
    }

    public void initCache() {
        if (!this.getBlock().hasDynamicShape()) {
            this.cache = new BlockState.Cache(this);
        }

    }

    public Block getBlock() {
        return this.owner;
    }

    public Material getMaterial() {
        return this.getBlock().getMaterial(this);
    }

    public boolean isValidSpawn(BlockGetter param0, BlockPos param1, EntityType<?> param2) {
        return this.getBlock().isValidSpawn(this, param0, param1, param2);
    }

    public boolean propagatesSkylightDown(BlockGetter param0, BlockPos param1) {
        return this.cache != null ? this.cache.propagatesSkylightDown : this.getBlock().propagatesSkylightDown(this, param0, param1);
    }

    public int getLightBlock(BlockGetter param0, BlockPos param1) {
        return this.cache != null ? this.cache.lightBlock : this.getBlock().getLightBlock(this, param0, param1);
    }

    public VoxelShape getFaceOcclusionShape(BlockGetter param0, BlockPos param1, Direction param2) {
        return this.cache != null && this.cache.occlusionShapes != null
            ? this.cache.occlusionShapes[param2.ordinal()]
            : Shapes.getFaceShape(this.getOcclusionShape(param0, param1), param2);
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
        return this.getBlock().isAir(this);
    }

    public MaterialColor getMapColor(BlockGetter param0, BlockPos param1) {
        return this.getBlock().getMapColor(this, param0, param1);
    }

    public BlockState rotate(Rotation param0) {
        return this.getBlock().rotate(this, param0);
    }

    public BlockState mirror(Mirror param0) {
        return this.getBlock().mirror(this, param0);
    }

    public RenderShape getRenderShape() {
        return this.getBlock().getRenderShape(this);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean emissiveRendering() {
        return this.getBlock().emissiveRendering(this);
    }

    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockGetter param0, BlockPos param1) {
        return this.getBlock().getShadeBrightness(this, param0, param1);
    }

    public boolean isRedstoneConductor(BlockGetter param0, BlockPos param1) {
        return this.getBlock().isRedstoneConductor(this, param0, param1);
    }

    public boolean isSignalSource() {
        return this.getBlock().isSignalSource(this);
    }

    public int getSignal(BlockGetter param0, BlockPos param1, Direction param2) {
        return this.getBlock().getSignal(this, param0, param1, param2);
    }

    public boolean hasAnalogOutputSignal() {
        return this.getBlock().hasAnalogOutputSignal(this);
    }

    public int getAnalogOutputSignal(Level param0, BlockPos param1) {
        return this.getBlock().getAnalogOutputSignal(this, param0, param1);
    }

    public float getDestroySpeed(BlockGetter param0, BlockPos param1) {
        return this.getBlock().getDestroySpeed(this, param0, param1);
    }

    public float getDestroyProgress(Player param0, BlockGetter param1, BlockPos param2) {
        return this.getBlock().getDestroyProgress(this, param0, param1, param2);
    }

    public int getDirectSignal(BlockGetter param0, BlockPos param1, Direction param2) {
        return this.getBlock().getDirectSignal(this, param0, param1, param2);
    }

    public PushReaction getPistonPushReaction() {
        return this.getBlock().getPistonPushReaction(this);
    }

    public boolean isSolidRender(BlockGetter param0, BlockPos param1) {
        return this.cache != null ? this.cache.solidRender : this.getBlock().isSolidRender(this, param0, param1);
    }

    public boolean canOcclude() {
        return this.cache != null ? this.cache.canOcclude : this.getBlock().canOcclude(this);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean skipRendering(BlockState param0, Direction param1) {
        return this.getBlock().skipRendering(this, param0, param1);
    }

    public VoxelShape getShape(BlockGetter param0, BlockPos param1) {
        return this.getShape(param0, param1, CollisionContext.empty());
    }

    public VoxelShape getShape(BlockGetter param0, BlockPos param1, CollisionContext param2) {
        return this.getBlock().getShape(this, param0, param1, param2);
    }

    public VoxelShape getCollisionShape(BlockGetter param0, BlockPos param1) {
        return this.cache != null ? this.cache.collisionShape : this.getCollisionShape(param0, param1, CollisionContext.empty());
    }

    public VoxelShape getCollisionShape(BlockGetter param0, BlockPos param1, CollisionContext param2) {
        return this.getBlock().getCollisionShape(this, param0, param1, param2);
    }

    public VoxelShape getOcclusionShape(BlockGetter param0, BlockPos param1) {
        return this.getBlock().getOcclusionShape(this, param0, param1);
    }

    public VoxelShape getInteractionShape(BlockGetter param0, BlockPos param1) {
        return this.getBlock().getInteractionShape(this, param0, param1);
    }

    public final boolean entityCanStandOn(BlockGetter param0, BlockPos param1, Entity param2) {
        return Block.isFaceFull(this.getCollisionShape(param0, param1, CollisionContext.of(param2)), Direction.UP);
    }

    public Vec3 getOffset(BlockGetter param0, BlockPos param1) {
        return this.getBlock().getOffset(this, param0, param1);
    }

    public boolean triggerEvent(Level param0, BlockPos param1, int param2, int param3) {
        return this.getBlock().triggerEvent(this, param0, param1, param2, param3);
    }

    public void neighborChanged(Level param0, BlockPos param1, Block param2, BlockPos param3, boolean param4) {
        this.getBlock().neighborChanged(this, param0, param1, param2, param3, param4);
    }

    public void updateNeighbourShapes(LevelAccessor param0, BlockPos param1, int param2) {
        this.getBlock().updateNeighbourShapes(this, param0, param1, param2);
    }

    public void updateIndirectNeighbourShapes(LevelAccessor param0, BlockPos param1, int param2) {
        this.getBlock().updateIndirectNeighbourShapes(this, param0, param1, param2);
    }

    public void onPlace(Level param0, BlockPos param1, BlockState param2, boolean param3) {
        this.getBlock().onPlace(this, param0, param1, param2, param3);
    }

    public void onRemove(Level param0, BlockPos param1, BlockState param2, boolean param3) {
        this.getBlock().onRemove(this, param0, param1, param2, param3);
    }

    public void tick(ServerLevel param0, BlockPos param1, Random param2) {
        this.getBlock().tick(this, param0, param1, param2);
    }

    public void randomTick(ServerLevel param0, BlockPos param1, Random param2) {
        this.getBlock().randomTick(this, param0, param1, param2);
    }

    public void entityInside(Level param0, BlockPos param1, Entity param2) {
        this.getBlock().entityInside(this, param0, param1, param2);
    }

    public void spawnAfterBreak(Level param0, BlockPos param1, ItemStack param2) {
        this.getBlock().spawnAfterBreak(this, param0, param1, param2);
    }

    public List<ItemStack> getDrops(LootContext.Builder param0) {
        return this.getBlock().getDrops(this, param0);
    }

    public InteractionResult use(Level param0, Player param1, InteractionHand param2, BlockHitResult param3) {
        return this.getBlock().use(this, param0, param3.getBlockPos(), param1, param2, param3);
    }

    public void attack(Level param0, BlockPos param1, Player param2) {
        this.getBlock().attack(this, param0, param1, param2);
    }

    public boolean isViewBlocking(BlockGetter param0, BlockPos param1) {
        return this.getBlock().isViewBlocking(this, param0, param1);
    }

    public BlockState updateShape(Direction param0, BlockState param1, LevelAccessor param2, BlockPos param3, BlockPos param4) {
        return this.getBlock().updateShape(this, param0, param1, param2, param3, param4);
    }

    public boolean isPathfindable(BlockGetter param0, BlockPos param1, PathComputationType param2) {
        return this.getBlock().isPathfindable(this, param0, param1, param2);
    }

    public boolean canBeReplaced(BlockPlaceContext param0) {
        return this.getBlock().canBeReplaced(this, param0);
    }

    public boolean canBeReplaced(Fluid param0) {
        return this.getBlock().canBeReplaced(this, param0);
    }

    public boolean canSurvive(LevelReader param0, BlockPos param1) {
        return this.getBlock().canSurvive(this, param0, param1);
    }

    public boolean hasPostProcess(BlockGetter param0, BlockPos param1) {
        return this.getBlock().hasPostProcess(this, param0, param1);
    }

    @Nullable
    public MenuProvider getMenuProvider(Level param0, BlockPos param1) {
        return this.getBlock().getMenuProvider(this, param0, param1);
    }

    public boolean is(Tag<Block> param0) {
        return this.getBlock().is(param0);
    }

    public FluidState getFluidState() {
        return this.getBlock().getFluidState(this);
    }

    public boolean isRandomlyTicking() {
        return this.getBlock().isRandomlyTicking(this);
    }

    @OnlyIn(Dist.CLIENT)
    public long getSeed(BlockPos param0) {
        return this.getBlock().getSeed(this, param0);
    }

    public SoundType getSoundType() {
        return this.getBlock().getSoundType(this);
    }

    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Entity param3) {
        this.getBlock().onProjectileHit(param0, param1, param2, param3);
    }

    public boolean isFaceSturdy(BlockGetter param0, BlockPos param1, Direction param2) {
        return this.cache != null ? this.cache.isFaceSturdy[param2.ordinal()] : Block.isFaceSturdy(this, param0, param1, param2);
    }

    public boolean isCollisionShapeFullBlock(BlockGetter param0, BlockPos param1) {
        return this.cache != null ? this.cache.isCollisionShapeFullBlock : Block.isShapeFullBlock(this.getCollisionShape(param0, param1));
    }

    public static <T> Dynamic<T> serialize(DynamicOps<T> param0, BlockState param1) {
        ImmutableMap<Property<?>, Comparable<?>> var0 = param1.getValues();
        T var1;
        if (var0.isEmpty()) {
            var1 = param0.createMap(ImmutableMap.of(param0.createString("Name"), param0.createString(Registry.BLOCK.getKey(param1.getBlock()).toString())));
        } else {
            var1 = param0.createMap(
                ImmutableMap.of(
                    param0.createString("Name"),
                    param0.createString(Registry.BLOCK.getKey(param1.getBlock()).toString()),
                    param0.createString("Properties"),
                    param0.createMap(
                        var0.entrySet()
                            .stream()
                            .map(
                                param1x -> Pair.of(
                                        param0.createString(param1x.getKey().getName()),
                                        param0.createString(StateHolder.getName(param1x.getKey(), param1x.getValue()))
                                    )
                            )
                            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                    )
                )
            );
        }

        return new Dynamic<>(param0, var1);
    }

    public static <T> BlockState deserialize(Dynamic<T> param0) {
        Block var0 = Registry.BLOCK.get(new ResourceLocation(param0.getElement("Name").flatMap(param0.getOps()::getStringValue).orElse("minecraft:air")));
        Map<String, String> var1 = param0.get("Properties").asMap(param0x -> param0x.asString(""), param0x -> param0x.asString(""));
        BlockState var2 = var0.defaultBlockState();
        StateDefinition<Block, BlockState> var3 = var0.getStateDefinition();

        for(Entry<String, String> var4 : var1.entrySet()) {
            String var5 = var4.getKey();
            Property<?> var6 = var3.getProperty(var5);
            if (var6 != null) {
                var2 = StateHolder.setValueHelper(var2, var6, var5, param0.toString(), var4.getValue());
            }
        }

        return var2;
    }

    static final class Cache {
        private static final Direction[] DIRECTIONS = Direction.values();
        private final boolean canOcclude;
        private final boolean solidRender;
        private final boolean propagatesSkylightDown;
        private final int lightBlock;
        private final VoxelShape[] occlusionShapes;
        private final VoxelShape collisionShape;
        private final boolean largeCollisionShape;
        private final boolean[] isFaceSturdy;
        private final boolean isCollisionShapeFullBlock;

        private Cache(BlockState param0) {
            Block var0 = param0.getBlock();
            this.canOcclude = var0.canOcclude(param0);
            this.solidRender = var0.isSolidRender(param0, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
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
