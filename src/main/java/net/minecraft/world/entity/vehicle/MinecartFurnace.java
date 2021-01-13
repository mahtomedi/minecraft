package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartFurnace extends AbstractMinecart {
    private static final EntityDataAccessor<Boolean> DATA_ID_FUEL = SynchedEntityData.defineId(MinecartFurnace.class, EntityDataSerializers.BOOLEAN);
    private int fuel;
    public double xPush;
    public double zPush;
    private static final Ingredient INGREDIENT = Ingredient.of(Items.COAL, Items.CHARCOAL);

    public MinecartFurnace(EntityType<? extends MinecartFurnace> param0, Level param1) {
        super(param0, param1);
    }

    public MinecartFurnace(Level param0, double param1, double param2, double param3) {
        super(EntityType.FURNACE_MINECART, param0, param1, param2, param3);
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.FURNACE;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_FUEL, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide()) {
            if (this.fuel > 0) {
                --this.fuel;
            }

            if (this.fuel <= 0) {
                this.xPush = 0.0;
                this.zPush = 0.0;
            }

            this.setHasFuel(this.fuel > 0);
        }

        if (this.hasFuel() && this.random.nextInt(4) == 0) {
            this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8, this.getZ(), 0.0, 0.0, 0.0);
        }

    }

    @Override
    protected double getMaxSpeed() {
        return 0.2;
    }

    @Override
    public void destroy(DamageSource param0) {
        super.destroy(param0);
        if (!param0.isExplosion() && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.spawnAtLocation(Blocks.FURNACE);
        }

    }

    @Override
    protected void moveAlongTrack(BlockPos param0, BlockState param1) {
        double var0 = 1.0E-4;
        double var1 = 0.001;
        super.moveAlongTrack(param0, param1);
        Vec3 var2 = this.getDeltaMovement();
        double var3 = getHorizontalDistanceSqr(var2);
        double var4 = this.xPush * this.xPush + this.zPush * this.zPush;
        if (var4 > 1.0E-4 && var3 > 0.001) {
            double var5 = (double)Mth.sqrt(var3);
            double var6 = (double)Mth.sqrt(var4);
            this.xPush = var2.x / var5 * var6;
            this.zPush = var2.z / var5 * var6;
        }

    }

    @Override
    protected void applyNaturalSlowdown() {
        double var0 = this.xPush * this.xPush + this.zPush * this.zPush;
        if (var0 > 1.0E-7) {
            var0 = (double)Mth.sqrt(var0);
            this.xPush /= var0;
            this.zPush /= var0;
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.8, 0.0, 0.8).add(this.xPush, 0.0, this.zPush));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.98, 0.0, 0.98));
        }

        super.applyNaturalSlowdown();
    }

    @Override
    public InteractionResult interact(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (INGREDIENT.test(var0) && this.fuel + 3600 <= 32000) {
            if (!param0.abilities.instabuild) {
                var0.shrink(1);
            }

            this.fuel += 3600;
        }

        if (this.fuel > 0) {
            this.xPush = this.getX() - param0.getX();
            this.zPush = this.getZ() - param0.getZ();
        }

        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putDouble("PushX", this.xPush);
        param0.putDouble("PushZ", this.zPush);
        param0.putShort("Fuel", (short)this.fuel);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.xPush = param0.getDouble("PushX");
        this.zPush = param0.getDouble("PushZ");
        this.fuel = param0.getShort("Fuel");
    }

    protected boolean hasFuel() {
        return this.entityData.get(DATA_ID_FUEL);
    }

    protected void setHasFuel(boolean param0) {
        this.entityData.set(DATA_ID_FUEL, param0);
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH).setValue(FurnaceBlock.LIT, Boolean.valueOf(this.hasFuel()));
    }
}
