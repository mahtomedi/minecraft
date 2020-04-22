package net.minecraft.world.entity.decoration;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemFrame extends HangingEntity {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_ROTATION = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.INT);
    private float dropChance = 1.0F;
    private boolean fixed;

    public ItemFrame(EntityType<? extends ItemFrame> param0, Level param1) {
        super(param0, param1);
    }

    public ItemFrame(Level param0, BlockPos param1, Direction param2) {
        super(EntityType.ITEM_FRAME, param0, param1);
        this.setDirection(param2);
    }

    @Override
    protected float getEyeHeight(Pose param0, EntityDimensions param1) {
        return 0.0F;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
        this.getEntityData().define(DATA_ROTATION, 0);
    }

    @Override
    protected void setDirection(Direction param0) {
        Validate.notNull(param0);
        this.direction = param0;
        if (param0.getAxis().isHorizontal()) {
            this.xRot = 0.0F;
            this.yRot = (float)(this.direction.get2DDataValue() * 90);
        } else {
            this.xRot = (float)(-90 * param0.getAxisDirection().getStep());
            this.yRot = 0.0F;
        }

        this.xRotO = this.xRot;
        this.yRotO = this.yRot;
        this.recalculateBoundingBox();
    }

    @Override
    protected void recalculateBoundingBox() {
        if (this.direction != null) {
            double var0 = 0.46875;
            double var1 = (double)this.pos.getX() + 0.5 - (double)this.direction.getStepX() * 0.46875;
            double var2 = (double)this.pos.getY() + 0.5 - (double)this.direction.getStepY() * 0.46875;
            double var3 = (double)this.pos.getZ() + 0.5 - (double)this.direction.getStepZ() * 0.46875;
            this.setPosRaw(var1, var2, var3);
            double var4 = (double)this.getWidth();
            double var5 = (double)this.getHeight();
            double var6 = (double)this.getWidth();
            Direction.Axis var7 = this.direction.getAxis();
            switch(var7) {
                case X:
                    var4 = 1.0;
                    break;
                case Y:
                    var5 = 1.0;
                    break;
                case Z:
                    var6 = 1.0;
            }

            var4 /= 32.0;
            var5 /= 32.0;
            var6 /= 32.0;
            this.setBoundingBox(new AABB(var1 - var4, var2 - var5, var3 - var6, var1 + var4, var2 + var5, var3 + var6));
        }
    }

    @Override
    public boolean survives() {
        if (this.fixed) {
            return true;
        } else if (!this.level.noCollision(this)) {
            return false;
        } else {
            BlockState var0 = this.level.getBlockState(this.pos.relative(this.direction.getOpposite()));
            return var0.getMaterial().isSolid() || this.direction.getAxis().isHorizontal() && DiodeBlock.isDiode(var0)
                ? this.level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty()
                : false;
        }
    }

    @Override
    public void move(MoverType param0, Vec3 param1) {
        if (!this.fixed) {
            super.move(param0, param1);
        }

    }

    @Override
    public void push(double param0, double param1, double param2) {
        if (!this.fixed) {
            super.push(param0, param1, param2);
        }

    }

    @Override
    public float getPickRadius() {
        return 0.0F;
    }

    @Override
    public void kill() {
        this.removeFramedMap(this.getItem());
        super.kill();
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.fixed) {
            return param0 != DamageSource.OUT_OF_WORLD && !param0.isCreativePlayer() ? false : super.hurt(param0, param1);
        } else if (this.isInvulnerableTo(param0)) {
            return false;
        } else if (!param0.isExplosion() && !this.getItem().isEmpty()) {
            if (!this.level.isClientSide) {
                this.dropItem(param0.getEntity(), false);
                this.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F, 1.0F);
            }

            return true;
        } else {
            return super.hurt(param0, param1);
        }
    }

    @Override
    public int getWidth() {
        return 12;
    }

    @Override
    public int getHeight() {
        return 12;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        double var0 = 16.0;
        var0 *= 64.0 * getViewScale();
        return param0 < var0 * var0;
    }

    @Override
    public void dropItem(@Nullable Entity param0) {
        this.playSound(SoundEvents.ITEM_FRAME_BREAK, 1.0F, 1.0F);
        this.dropItem(param0, true);
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
    }

    private void dropItem(@Nullable Entity param0, boolean param1) {
        if (!this.fixed) {
            if (!this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                if (param0 == null) {
                    this.removeFramedMap(this.getItem());
                }

            } else {
                ItemStack var0 = this.getItem();
                this.setItem(ItemStack.EMPTY);
                if (param0 instanceof Player) {
                    Player var1 = (Player)param0;
                    if (var1.abilities.instabuild) {
                        this.removeFramedMap(var0);
                        return;
                    }
                }

                if (param1) {
                    this.spawnAtLocation(Items.ITEM_FRAME);
                }

                if (!var0.isEmpty()) {
                    var0 = var0.copy();
                    this.removeFramedMap(var0);
                    if (this.random.nextFloat() < this.dropChance) {
                        this.spawnAtLocation(var0);
                    }
                }

            }
        }
    }

    private void removeFramedMap(ItemStack param0) {
        if (param0.getItem() == Items.FILLED_MAP) {
            MapItemSavedData var0 = MapItem.getOrCreateSavedData(param0, this.level);
            var0.removedFromFrame(this.pos, this.getId());
            var0.setDirty(true);
        }

        param0.setEntityRepresentation(null);
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack param0) {
        this.setItem(param0, true);
    }

    public void setItem(ItemStack param0, boolean param1) {
        if (!param0.isEmpty()) {
            param0 = param0.copy();
            param0.setCount(1);
            param0.setEntityRepresentation(this);
        }

        this.getEntityData().set(DATA_ITEM, param0);
        if (!param0.isEmpty()) {
            this.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0F, 1.0F);
        }

        if (param1 && this.pos != null) {
            this.level.updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }

    }

    @Override
    public boolean setSlot(int param0, ItemStack param1) {
        if (param0 == 0) {
            this.setItem(param1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (param0.equals(DATA_ITEM)) {
            ItemStack var0 = this.getItem();
            if (!var0.isEmpty() && var0.getFrame() != this) {
                var0.setEntityRepresentation(this);
            }
        }

    }

    public int getRotation() {
        return this.getEntityData().get(DATA_ROTATION);
    }

    public void setRotation(int param0) {
        this.setRotation(param0, true);
    }

    private void setRotation(int param0, boolean param1) {
        this.getEntityData().set(DATA_ROTATION, param0 % 8);
        if (param1 && this.pos != null) {
            this.level.updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        if (!this.getItem().isEmpty()) {
            param0.put("Item", this.getItem().save(new CompoundTag()));
            param0.putByte("ItemRotation", (byte)this.getRotation());
            param0.putFloat("ItemDropChance", this.dropChance);
        }

        param0.putByte("Facing", (byte)this.direction.get3DDataValue());
        param0.putBoolean("Invisible", this.isInvisible());
        param0.putBoolean("Fixed", this.fixed);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        CompoundTag var0 = param0.getCompound("Item");
        if (var0 != null && !var0.isEmpty()) {
            ItemStack var1 = ItemStack.of(var0);
            if (var1.isEmpty()) {
                LOGGER.warn("Unable to load item from: {}", var0);
            }

            ItemStack var2 = this.getItem();
            if (!var2.isEmpty() && !ItemStack.matches(var1, var2)) {
                this.removeFramedMap(var2);
            }

            this.setItem(var1, false);
            this.setRotation(param0.getByte("ItemRotation"), false);
            if (param0.contains("ItemDropChance", 99)) {
                this.dropChance = param0.getFloat("ItemDropChance");
            }
        }

        this.setDirection(Direction.from3DDataValue(param0.getByte("Facing")));
        this.setInvisible(param0.getBoolean("Invisible"));
        this.fixed = param0.getBoolean("Fixed");
    }

    @Override
    public boolean interact(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        boolean var1 = !this.getItem().isEmpty();
        boolean var2 = !var0.isEmpty();
        if (this.level.isClientSide) {
            return var1 || var2;
        } else {
            if (this.fixed || var1) {
                this.playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
                this.setRotation(this.getRotation() + 1);
            } else if (var2 && !this.removed) {
                this.setItem(var0);
                if (!param0.abilities.instabuild) {
                    var0.shrink(1);
                }
            }

            return true;
        }
    }

    public int getAnalogOutput() {
        return this.getItem().isEmpty() ? 0 : this.getRotation() % 8 + 1;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, this.getType(), this.direction.get3DDataValue(), this.getPos());
    }
}
