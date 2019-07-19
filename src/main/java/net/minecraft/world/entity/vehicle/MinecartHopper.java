package net.minecraft.world.entity.vehicle;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MinecartHopper extends AbstractMinecartContainer implements Hopper {
    private boolean enabled = true;
    private int cooldownTime = -1;
    private final BlockPos lastPosition = BlockPos.ZERO;

    public MinecartHopper(EntityType<? extends MinecartHopper> param0, Level param1) {
        super(param0, param1);
    }

    public MinecartHopper(Level param0, double param1, double param2, double param3) {
        super(EntityType.HOPPER_MINECART, param1, param2, param3, param0);
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.HOPPER;
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.HOPPER.defaultBlockState();
    }

    @Override
    public int getDefaultDisplayOffset() {
        return 1;
    }

    @Override
    public int getContainerSize() {
        return 5;
    }

    @Override
    public void activateMinecart(int param0, int param1, int param2, boolean param3) {
        boolean var0 = !param3;
        if (var0 != this.isEnabled()) {
            this.setEnabled(var0);
        }

    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean param0) {
        this.enabled = param0;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public double getLevelX() {
        return this.x;
    }

    @Override
    public double getLevelY() {
        return this.y + 0.5;
    }

    @Override
    public double getLevelZ() {
        return this.z;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide && this.isAlive() && this.isEnabled()) {
            BlockPos var0 = new BlockPos(this);
            if (var0.equals(this.lastPosition)) {
                --this.cooldownTime;
            } else {
                this.setCooldown(0);
            }

            if (!this.isOnCooldown()) {
                this.setCooldown(0);
                if (this.suckInItems()) {
                    this.setCooldown(4);
                    this.setChanged();
                }
            }
        }

    }

    public boolean suckInItems() {
        if (HopperBlockEntity.suckInItems(this)) {
            return true;
        } else {
            List<ItemEntity> var0 = this.level
                .getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.25, 0.0, 0.25), EntitySelector.ENTITY_STILL_ALIVE);
            if (!var0.isEmpty()) {
                HopperBlockEntity.addItem(this, var0.get(0));
            }

            return false;
        }
    }

    @Override
    public void destroy(DamageSource param0) {
        super.destroy(param0);
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.spawnAtLocation(Blocks.HOPPER);
        }

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("TransferCooldown", this.cooldownTime);
        param0.putBoolean("Enabled", this.enabled);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.cooldownTime = param0.getInt("TransferCooldown");
        this.enabled = param0.contains("Enabled") ? param0.getBoolean("Enabled") : true;
    }

    public void setCooldown(int param0) {
        this.cooldownTime = param0;
    }

    public boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    @Override
    public AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return new HopperMenu(param0, param1, this);
    }
}
