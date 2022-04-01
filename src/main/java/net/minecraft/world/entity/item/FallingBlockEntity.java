package net.minecraft.world.entity.item;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ConfiguredStructureTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.CarriedBlocks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.GenericItemBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class FallingBlockEntity extends Entity {
    public static final Vec3i[] OFFSETS = new Vec3i[]{
        new Vec3i(0, 0, 0),
        new Vec3i(1, 0, 0),
        new Vec3i(0, 0, 1),
        new Vec3i(0, 0, -1),
        new Vec3i(-1, 0, 0),
        new Vec3i(1, 0, -1),
        new Vec3i(1, 0, 1),
        new Vec3i(-1, 0, 1),
        new Vec3i(-1, 0, -1)
    };
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Predicate<Entity> HIT_PREDICATE = EntitySelector.NO_SPECTATORS.and(param0 -> !(param0 instanceof FallingBlockEntity));
    private BlockState blockState = Blocks.SAND.defaultBlockState();
    public int time;
    public boolean dropItem = true;
    private boolean cancelDrop;
    private boolean hurtEntities;
    private int fallDamageMax = 40;
    private float fallDamagePerDistance;
    @Nullable
    public CompoundTag blockData;
    private boolean farFromStart;
    @Nullable
    private Direction craftDirection = null;
    @Nullable
    private Player thrownBy = null;
    private boolean smelted;
    private float spin;
    private float prevSpin;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    public FallingBlockEntity(EntityType<? extends FallingBlockEntity> param0, Level param1) {
        super(param0, param1);
    }

    public FallingBlockEntity(Level param0, double param1, double param2, double param3, BlockState param4) {
        this(EntityType.FALLING_BLOCK, param0);
        this.blockState = param4;
        this.blocksBuilding = true;
        this.setPos(param1, param2, param3);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = param1;
        this.yo = param2;
        this.zo = param3;
        this.setStartPos(this.blockPosition());
    }

    public FallingBlockEntity(ServerPlayer param0, BlockState param1, Direction param2) {
        this(param0.level, param0.getX(), param0.getY() + (double)param0.getEyeHeight(), param0.getZ(), param1);
        this.thrownBy = param0;
        if (this.blockState.hasProperty(BlockStateProperties.FACING)) {
            this.blockState = this.blockState.setValue(BlockStateProperties.FACING, Direction.getRandom(this.level.random));
        }

        if (this.blockState.is(Blocks.END_PORTAL_FRAME)) {
            Level var1 = this.level;
            if (var1 instanceof ServerLevel var0) {
                BlockPos var1x = param0.blockPosition();
                BlockPos var2 = var0.findNearestMapFeature(ConfiguredStructureTags.EYE_OF_ENDER_LOCATED, var1x, 100, false);
                if (var2 != null) {
                    BlockPos var3 = var2.subtract(var1x);
                    Direction var4 = Direction.getNearest((float)var3.getX(), (float)var3.getY(), (float)var3.getZ());
                    this.blockState = this.blockState.setValue(EndPortalFrameBlock.FACING, var4);
                }
            }
        }

        this.craftDirection = param2;
    }

    public static FallingBlockEntity fall(Level param0, BlockPos param1, BlockState param2) {
        return fall(param0, param1, param2, Vec3.ZERO);
    }

    public static FallingBlockEntity fall(Level param0, BlockPos param1, BlockState param2, Vec3 param3) {
        boolean var0 = param2.hasProperty(BlockStateProperties.WATERLOGGED);
        FallingBlockEntity var1 = new FallingBlockEntity(
            param0,
            (double)param1.getX() + 0.5,
            (double)param1.getY(),
            (double)param1.getZ() + 0.5,
            var0 ? param2.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)) : param2
        );
        var1.setDeltaMovement(param3);
        if (param3.lengthSqr() > 0.0) {
            var1.craftDirection = Direction.getNearest(param3.x, 0.0, param3.z);
        }

        param0.setBlock(param1, var0 ? param2.getFluidState().createLegacyBlock() : Blocks.AIR.defaultBlockState(), 3);
        param0.addFreshEntity(var1);
        return var1;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    public void setStartPos(BlockPos param0) {
        this.entityData.set(DATA_START_POS, param0);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    public float getSpin(float param0) {
        return Mth.lerp(param0, this.prevSpin, this.spin);
    }

    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.discard();
        } else {
            Block var0 = this.blockState.getBlock();
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 var1 = this.position().subtract(this.xo, this.yo, this.zo);
            this.prevSpin = this.spin;
            this.spin = (float)((double)this.spin + var1.length() * 50.0);
            double var2 = this.getDeltaMovement().y < 0.0 ? 3.0 : 2.0;
            if (!this.farFromStart && !this.getStartPos().closerThan(new BlockPos(this.position()), var2)) {
                this.farFromStart = true;
            }

            if (this.farFromStart && !this.level.isClientSide()) {
                List<Entity> var3 = this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()), HIT_PREDICATE);
                var3.removeIf(param0 -> !this.hitEntity(param0));
                if (!var3.isEmpty()) {
                    this.setDeltaMovement(this.getDeltaMovement().reverse().scale(0.1));
                    this.hasImpulse = true;
                }
            }

            ++this.time;
            if (!this.level.isClientSide) {
                BlockPos var4 = this.blockPosition();
                FluidState var5 = this.level.getFluidState(var4);
                boolean var6 = this.blockState.getBlock() instanceof ConcretePowderBlock;
                boolean var7 = var6 && var5.is(FluidTags.WATER);
                double var8 = this.getDeltaMovement().lengthSqr();
                if (var6 && var8 > 1.0) {
                    BlockHitResult var9 = this.level
                        .clip(
                            new ClipContext(
                                new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this
                            )
                        );
                    if (var9.getType() != HitResult.Type.MISS && this.level.getFluidState(var9.getBlockPos()).is(FluidTags.WATER)) {
                        var4 = var9.getBlockPos();
                        var7 = true;
                    }
                }

                if (!this.smelted && var5.is(FluidTags.LAVA)) {
                    BlockState var10 = this.trySmelt(this.blockState);
                    if (var10 != null) {
                        this.blockState = var10;
                        this.smelted = true;
                    }
                }

                if (this.onGround || var7) {
                    BlockState var11 = this.level.getBlockState(var4);
                    if (!var11.isAir() && !var11.isCollisionShapeFullBlock(this.level, var4) && this.getY() - (double)var4.getY() > 0.8) {
                        var4 = var4.above();
                        var11 = this.level.getBlockState(var4);
                    }

                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
                    if (!var11.is(Blocks.MOVING_PISTON) && !this.cancelDrop) {
                        boolean var12 = var11.canBeReplaced(new DirectionalPlaceContext(this.level, var4, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                        boolean var13 = FallingBlock.isFree(this.level.getBlockState(var4.below())) && (!var6 || !var7);
                        boolean var14 = this.blockState.canSurvive(this.level, var4) && !var13;
                        if (var12 && !var13) {
                            if (!var14 && !this.blockState.is(Blocks.GENERIC_ITEM_BLOCK)) {
                                BlockState var15 = GenericItemBlock.wrap(this.blockState);
                                if (var15 != null && var15.canSurvive(this.level, var4)) {
                                    this.blockState = var15;
                                    var14 = true;
                                }
                            } else if (this.blockState.is(Blocks.GENERIC_ITEM_BLOCK)) {
                                BlockState var16 = GenericItemBlock.unwrap(this.blockState);
                                if (var16 != null && var16.canSurvive(this.level, var4)) {
                                    this.blockState = var16;
                                }
                            }
                        }

                        if (var12 && var14) {
                            if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && var5.getType() == Fluids.WATER) {
                                this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
                            }

                            if (var0 instanceof BedBlock) {
                                BlockPos var17 = var4.relative(BedBlock.getConnectedDirection(this.blockState));
                                if (this.level.getBlockState(var17).isAir()) {
                                    BedPart var18 = this.blockState.getValue(BedBlock.PART);

                                    var18 = switch(var18) {
                                        case HEAD -> BedPart.FOOT;
                                        case FOOT -> BedPart.HEAD;
                                    };
                                    this.level.setBlock(var17, this.blockState.setValue(BedBlock.PART, var18), 3);
                                }
                            }

                            BlockState var19 = Block.updateFromNeighbourShapes(this.blockState, this.level, var4);
                            if (this.level.setBlockAndUpdate(var4, var19)) {
                                ((ServerLevel)this.level)
                                    .getChunkSource()
                                    .chunkMap
                                    .broadcast(this, new ClientboundBlockUpdatePacket(var4, this.level.getBlockState(var4)));
                                if (this.thrownBy != null && this.blockState.is(BlockTags.FRAGILE) && this.level.random.nextBoolean()) {
                                    this.level.destroyBlock(var4, false, this.thrownBy);
                                    ((ServerLevel)this.level)
                                        .getChunkSource()
                                        .chunkMap
                                        .broadcast(this, new ClientboundBlockUpdatePacket(var4, this.level.getBlockState(var4)));
                                    this.discard();
                                    return;
                                }

                                this.discard();
                                if (var0 instanceof Fallable) {
                                    ((Fallable)var0).onLand(this.level, var4, this.blockState, var11, this);
                                }

                                if (this.blockData != null && this.blockState.hasBlockEntity()) {
                                    BlockEntity var20 = this.level.getBlockEntity(var4);
                                    if (var20 != null) {
                                        CompoundTag var21 = var20.saveWithoutMetadata();

                                        for(String var22 : this.blockData.getAllKeys()) {
                                            var21.put(var22, this.blockData.get(var22).copy());
                                        }

                                        try {
                                            var20.load(var21);
                                        } catch (Exception var25) {
                                            LOGGER.error("Failed to load block entity from falling block", (Throwable)var25);
                                        }

                                        var20.setChanged();
                                    }
                                }

                                boolean var24 = false;
                                if (!this.smelted && this.level.getBlockState(var4.below()).is(Blocks.CRAFTING_TABLE)) {
                                    this.craftDirection = this.craftDirection == null
                                        ? Direction.Plane.HORIZONTAL.getRandomDirection(this.level.random)
                                        : this.craftDirection;

                                    for(Vec3i var25 : OFFSETS) {
                                        if (this.tryCraft(var4.offset(var25), this.craftDirection)) {
                                            var24 = true;
                                            break;
                                        }
                                    }
                                }

                                if (!var24 && this.blockState.is(Blocks.CRAFTING_TABLE)) {
                                    BlockPos var26 = var4.below();
                                    this.craftDirection = this.craftDirection == null
                                        ? Direction.Plane.HORIZONTAL.getRandomDirection(this.level.random)
                                        : this.craftDirection;

                                    for(Vec3i var27 : OFFSETS) {
                                        if (this.tryCraft(var26.offset(var27), this.craftDirection)) {
                                            var24 = true;
                                            break;
                                        }
                                    }

                                    if (!var24) {
                                        for(Direction var28 : Direction.Plane.HORIZONTAL) {
                                            BlockPos var29 = var26.relative(var28);
                                            if (this.level.getBlockState(var29).isAir()) {
                                                for(Vec3i var30 : OFFSETS) {
                                                    if (this.tryCraft(var29.offset(var30), this.craftDirection)) {
                                                        var24 = true;
                                                        break;
                                                    }
                                                }
                                            }

                                            if (var24) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (!this.level.isClientSide
                    && (this.time > 100 && (var4.getY() <= this.level.getMinBuildHeight() || var4.getY() > this.level.getMaxBuildHeight()) || this.time > 600)) {
                    if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                        this.spawnAtLocation(var0);
                    }

                    this.discard();
                }
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }
    }

    @Nullable
    private BlockState trySmelt(BlockState param0) {
        ItemStack var0 = CarriedBlocks.getItemStackFromBlock(param0);
        if (var0.isEmpty()) {
            return null;
        } else {
            SimpleContainer var1 = new SimpleContainer(var0);
            Optional<SmeltingRecipe> var2 = this.level.getServer().getRecipeManager().getRecipeFor(RecipeType.SMELTING, var1, this.level);
            return !var2.isEmpty() && var2.get().getResultItem().getCount() <= 1
                ? CarriedBlocks.getBlockFromItemStack(var2.get().getResultItem()).orElse(null)
                : null;
        }
    }

    private boolean tryCraft(BlockPos param0, Direction param1) {
        Vec3i var0 = param1.getOpposite().getNormal();
        Vec3i var1 = param1.getOpposite().getClockWise().getNormal();
        int var2 = 0;
        CraftingContainer var3 = new CraftingContainer(null, 3, 3);

        for(int var4 = -1; var4 <= 1; ++var4) {
            for(int var5 = -1; var5 <= 1; ++var5) {
                BlockPos var6 = param0.offset(var0.getX() * var4 + var1.getX() * var5, 0, var0.getZ() * var4 + var1.getZ() * var5);
                BlockState var7 = this.level.getBlockState(var6);
                ItemStack var8 = CarriedBlocks.getItemStackFromBlock(var7);
                if (!var8.isEmpty()) {
                    var3.setItem(var2, var8);
                }

                ++var2;
            }
        }

        Optional<CraftingRecipe> var9 = this.level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, var3, this.level);
        Optional<BlockState> var10 = var9.flatMap(param0x -> CarriedBlocks.getBlockFromItemStack(param0x.getResultItem()));
        var10.ifPresent(param1x -> {
            for(BlockPos var0x : BlockPos.betweenClosed(param0.offset(-1, 0, -1), param0.offset(1, 0, 1))) {
                if (!CarriedBlocks.getItemStackFromBlock(this.level.getBlockState(var0x)).isEmpty()) {
                    this.level.setBlock(var0x, param1x, 3);
                }
            }

        });
        return var9.isPresent();
    }

    private boolean hitEntity(Entity param0) {
        DamageSource var0 = this.thrownBy != null ? DamageSource.thrown(this, this.thrownBy) : DamageSource.FALLING_BLOCK;
        if (this.getDeltaMovement().lengthSqr() > 0.125) {
            if (param0 instanceof Skeleton var1 && CarriedBlocks.getItemStackFromBlock(this.blockState).is(Items.SPYGLASS)) {
                var1.hurt(var0, 0.0F);
                if (var1.getSpyglassesInSockets() < 2) {
                    var1.addSpyglassIntoEyeSocket();
                }

                this.remove(Entity.RemovalReason.KILLED);
                return true;
            }

            if (param0 instanceof LivingEntity var2
                && var2.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                && (this.blockState.is(Blocks.CARVED_PUMPKIN) || this.blockState.is(Blocks.BARREL) || param0 instanceof Player)) {
                var2.hurt(var0, param0 instanceof Player ? 0.125F : 0.0F);
                ItemStack var3 = CarriedBlocks.getItemStackFromBlock(this.blockState);
                var2.setItemSlot(EquipmentSlot.HEAD, var3);
                if (this.blockState.is(Blocks.BARREL)) {
                    this.level.playSound(null, param0, SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                this.remove(Entity.RemovalReason.KILLED);
                return true;
            }
        }

        if ((this.blockState.is(Blocks.CACTUS) || this.blockState.is(Blocks.POINTED_DRIPSTONE)) && param0 instanceof Sheep var4) {
            if (var4.readyForShearing()) {
                var4.shear(SoundSource.NEUTRAL);
            }
        } else if (this.blockState.is(BlockTags.WOOL) && param0 instanceof Sheep var5) {
            var5.hurt(var0, 0.0F);
            if (var5.readyForShearing()) {
                var5.shear(SoundSource.NEUTRAL);
            }

            if (this.blockState.is(Blocks.WHITE_WOOL)) {
                var5.setColor(DyeColor.WHITE);
            } else if (this.blockState.is(Blocks.ORANGE_WOOL)) {
                var5.setColor(DyeColor.ORANGE);
            } else if (this.blockState.is(Blocks.MAGENTA_WOOL)) {
                var5.setColor(DyeColor.MAGENTA);
            } else if (this.blockState.is(Blocks.LIGHT_BLUE_WOOL)) {
                var5.setColor(DyeColor.LIGHT_BLUE);
            } else if (this.blockState.is(Blocks.YELLOW_WOOL)) {
                var5.setColor(DyeColor.YELLOW);
            } else if (this.blockState.is(Blocks.LIME_WOOL)) {
                var5.setColor(DyeColor.LIME);
            } else if (this.blockState.is(Blocks.PINK_WOOL)) {
                var5.setColor(DyeColor.PINK);
            } else if (this.blockState.is(Blocks.GRAY_WOOL)) {
                var5.setColor(DyeColor.GRAY);
            } else if (this.blockState.is(Blocks.LIGHT_GRAY_WOOL)) {
                var5.setColor(DyeColor.LIGHT_GRAY);
            } else if (this.blockState.is(Blocks.CYAN_WOOL)) {
                var5.setColor(DyeColor.CYAN);
            } else if (this.blockState.is(Blocks.PURPLE_WOOL)) {
                var5.setColor(DyeColor.PURPLE);
            } else if (this.blockState.is(Blocks.BLUE_WOOL)) {
                var5.setColor(DyeColor.BLUE);
            } else if (this.blockState.is(Blocks.BROWN_WOOL)) {
                var5.setColor(DyeColor.BROWN);
            } else if (this.blockState.is(Blocks.GREEN_WOOL)) {
                var5.setColor(DyeColor.GREEN);
            } else if (this.blockState.is(Blocks.RED_WOOL)) {
                var5.setColor(DyeColor.RED);
            } else if (this.blockState.is(Blocks.BLACK_WOOL)) {
                var5.setColor(DyeColor.BLACK);
            }

            var5.setSheared(false);
            this.remove(Entity.RemovalReason.KILLED);
            return true;
        }

        float var6 = 10.0F;
        float var7 = this.blockState.getBlock().defaultDestroyTime();
        float var8 = (float)Math.ceil(this.getDeltaMovement().length() * 10.0 * (double)var7);
        if (var8 > 0.0F && (param0 instanceof LivingEntity || param0 instanceof EndCrystal)) {
            param0.hurt(var0, var8);
            param0.setDeltaMovement(
                param0.getDeltaMovement().add(this.getDeltaMovement().scale(0.5 * (double)Mth.sqrt(this.blockState.getBlock().getExplosionResistance())))
            );
            if (param0.isOnGround()) {
                param0.setDeltaMovement(param0.getDeltaMovement().add(0.0, 0.5, 0.0));
            }

            return true;
        } else {
            return false;
        }
    }

    public void callOnBrokenAfterFall(Block param0, BlockPos param1) {
        if (param0 instanceof Fallable) {
            ((Fallable)param0).onBrokenAfterFall(this.level, param1, this);
        }

    }

    @Override
    public boolean causeFallDamage(float param0, float param1, DamageSource param2) {
        if (!this.hurtEntities) {
            return false;
        } else {
            int var0 = Mth.ceil(param0 - 1.0F);
            if (var0 < 0) {
                return false;
            } else {
                Predicate<Entity> var2;
                DamageSource var3;
                if (this.blockState.getBlock() instanceof Fallable var1) {
                    var2 = var1.getHurtsEntitySelector();
                    var3 = var1.getFallDamageSource();
                } else {
                    var2 = EntitySelector.NO_SPECTATORS;
                    var3 = DamageSource.FALLING_BLOCK;
                }

                float var6 = (float)Math.min(Mth.floor((float)var0 * this.fallDamagePerDistance), this.fallDamageMax);
                this.level.getEntities(this, this.getBoundingBox(), var2).forEach(param2x -> param2x.hurt(var3, var6));
                boolean var7 = this.blockState.is(BlockTags.ANVIL);
                if (var7 && var6 > 0.0F && this.random.nextFloat() < 0.05F + (float)var0 * 0.05F) {
                    BlockState var8 = AnvilBlock.damage(this.blockState);
                    if (var8 == null) {
                        this.cancelDrop = true;
                    } else {
                        this.blockState = var8;
                    }
                }

                return false;
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        param0.put("BlockState", NbtUtils.writeBlockState(this.blockState));
        param0.putInt("Time", this.time);
        param0.putBoolean("DropItem", this.dropItem);
        param0.putBoolean("HurtEntities", this.hurtEntities);
        param0.putFloat("FallHurtAmount", this.fallDamagePerDistance);
        param0.putInt("FallHurtMax", this.fallDamageMax);
        if (this.blockData != null) {
            param0.put("TileEntityData", this.blockData);
        }

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        this.blockState = NbtUtils.readBlockState(param0.getCompound("BlockState"));
        this.time = param0.getInt("Time");
        if (param0.contains("HurtEntities", 99)) {
            this.hurtEntities = param0.getBoolean("HurtEntities");
            this.fallDamagePerDistance = param0.getFloat("FallHurtAmount");
            this.fallDamageMax = param0.getInt("FallHurtMax");
        } else if (this.blockState.is(BlockTags.ANVIL)) {
            this.hurtEntities = true;
        }

        if (param0.contains("DropItem", 99)) {
            this.dropItem = param0.getBoolean("DropItem");
        }

        if (param0.contains("TileEntityData", 10)) {
            this.blockData = param0.getCompound("TileEntityData");
        }

        if (this.blockState.isAir()) {
            this.blockState = Blocks.SAND.defaultBlockState();
        }

    }

    public void setHurtsEntities(float param0, int param1) {
        this.hurtEntities = true;
        this.fallDamagePerDistance = param0;
        this.fallDamageMax = param1;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory param0) {
        super.fillCrashReportCategory(param0);
        param0.setDetail("Immitating BlockState", this.blockState.toString());
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket param0) {
        super.recreateFromPacket(param0);
        this.blockState = Block.stateById(param0.getData());
        this.blocksBuilding = true;
        double var0 = param0.getX();
        double var1 = param0.getY();
        double var2 = param0.getZ();
        this.setPos(var0, var1, var2);
        this.setStartPos(this.blockPosition());
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
