package net.minecraft.world.entity.item;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.DirectionalPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FallingBlockEntity extends Entity {
    private BlockState blockState = Blocks.SAND.defaultBlockState();
    public int time;
    public boolean dropItem = true;
    private boolean cancelDrop;
    private boolean hurtEntities;
    private int fallDamageMax = 40;
    private float fallDamageAmount = 2.0F;
    public CompoundTag blockData;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    public FallingBlockEntity(EntityType<? extends FallingBlockEntity> param0, Level param1) {
        super(param0, param1);
    }

    public FallingBlockEntity(Level param0, double param1, double param2, double param3, BlockState param4) {
        this(EntityType.FALLING_BLOCK, param0);
        this.blockState = param4;
        this.blocksBuilding = true;
        this.setPos(param1, param2 + (double)((1.0F - this.getBbHeight()) / 2.0F), param3);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = param1;
        this.yo = param2;
        this.zo = param3;
        this.setStartPos(new BlockPos(this));
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public void setStartPos(BlockPos param0) {
        this.entityData.set(DATA_START_POS, param0);
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    public boolean isPickable() {
        return !this.removed;
    }

    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.remove();
        } else {
            Block var0 = this.blockState.getBlock();
            if (this.time++ == 0) {
                BlockPos var1 = new BlockPos(this);
                if (this.level.getBlockState(var1).getBlock() == var0) {
                    this.level.removeBlock(var1, false);
                } else if (!this.level.isClientSide) {
                    this.remove();
                    return;
                }
            }

            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            if (!this.level.isClientSide) {
                BlockPos var2 = new BlockPos(this);
                boolean var3 = this.blockState.getBlock() instanceof ConcretePowderBlock;
                boolean var4 = var3 && this.level.getFluidState(var2).is(FluidTags.WATER);
                double var5 = this.getDeltaMovement().lengthSqr();
                if (var3 && var5 > 1.0) {
                    BlockHitResult var6 = this.level
                        .clip(
                            new ClipContext(
                                new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this
                            )
                        );
                    if (var6.getType() != HitResult.Type.MISS && this.level.getFluidState(var6.getBlockPos()).is(FluidTags.WATER)) {
                        var2 = var6.getBlockPos();
                        var4 = true;
                    }
                }

                if (this.onGround || var4) {
                    BlockState var7 = this.level.getBlockState(var2);
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
                    if (var7.getBlock() != Blocks.MOVING_PISTON) {
                        this.remove();
                        if (!this.cancelDrop) {
                            boolean var8 = var7.canBeReplaced(new DirectionalPlaceContext(this.level, var2, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                            boolean var9 = this.blockState.canSurvive(this.level, var2) && !FallingBlock.isFree(this.level.getBlockState(var2.below()));
                            if (var8 && var9) {
                                if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level.getFluidState(var2).getType() == Fluids.WATER) {
                                    this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
                                }

                                if (this.level.setBlock(var2, this.blockState, 3)) {
                                    if (var0 instanceof FallingBlock) {
                                        ((FallingBlock)var0).onLand(this.level, var2, this.blockState, var7);
                                    }

                                    if (this.blockData != null && var0 instanceof EntityBlock) {
                                        BlockEntity var10 = this.level.getBlockEntity(var2);
                                        if (var10 != null) {
                                            CompoundTag var11 = var10.save(new CompoundTag());

                                            for(String var12 : this.blockData.getAllKeys()) {
                                                Tag var13 = this.blockData.get(var12);
                                                if (!"x".equals(var12) && !"y".equals(var12) && !"z".equals(var12)) {
                                                    var11.put(var12, var13.copy());
                                                }
                                            }

                                            var10.load(var11);
                                            var10.setChanged();
                                        }
                                    }
                                } else if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                    this.spawnAtLocation(var0);
                                }
                            } else if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                this.spawnAtLocation(var0);
                            }
                        } else if (var0 instanceof FallingBlock) {
                            ((FallingBlock)var0).onBroken(this.level, var2);
                        }
                    }
                } else if (!this.level.isClientSide && (this.time > 100 && (var2.getY() < 1 || var2.getY() > 256) || this.time > 600)) {
                    if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                        this.spawnAtLocation(var0);
                    }

                    this.remove();
                }
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }
    }

    @Override
    public boolean causeFallDamage(float param0, float param1) {
        if (this.hurtEntities) {
            int var0 = Mth.ceil(param0 - 1.0F);
            if (var0 > 0) {
                List<Entity> var1 = Lists.newArrayList(this.level.getEntities(this, this.getBoundingBox()));
                boolean var2 = this.blockState.is(BlockTags.ANVIL);
                DamageSource var3 = var2 ? DamageSource.ANVIL : DamageSource.FALLING_BLOCK;

                for(Entity var4 : var1) {
                    var4.hurt(var3, (float)Math.min(Mth.floor((float)var0 * this.fallDamageAmount), this.fallDamageMax));
                }

                if (var2 && (double)this.random.nextFloat() < 0.05F + (double)var0 * 0.05) {
                    BlockState var5 = AnvilBlock.damage(this.blockState);
                    if (var5 == null) {
                        this.cancelDrop = true;
                    } else {
                        this.blockState = var5;
                    }
                }
            }
        }

        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        param0.put("BlockState", NbtUtils.writeBlockState(this.blockState));
        param0.putInt("Time", this.time);
        param0.putBoolean("DropItem", this.dropItem);
        param0.putBoolean("HurtEntities", this.hurtEntities);
        param0.putFloat("FallHurtAmount", this.fallDamageAmount);
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
            this.fallDamageAmount = param0.getFloat("FallHurtAmount");
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

    @OnlyIn(Dist.CLIENT)
    public Level getLevel() {
        return this.level;
    }

    public void setHurtsEntities(boolean param0) {
        this.hurtEntities = param0;
    }

    @OnlyIn(Dist.CLIENT)
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
}
