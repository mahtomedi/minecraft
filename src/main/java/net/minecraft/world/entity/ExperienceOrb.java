package net.minecraft.world.entity;

import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ExperienceOrb extends Entity {
    public int tickCount;
    public int age;
    public int throwTime;
    private int health = 5;
    private int value;
    private Player followingPlayer;
    private int followingTime;

    public ExperienceOrb(Level param0, double param1, double param2, double param3, int param4) {
        this(EntityType.EXPERIENCE_ORB, param0);
        this.setPos(param1, param2, param3);
        this.yRot = (float)(this.random.nextDouble() * 360.0);
        this.setDeltaMovement(
            (this.random.nextDouble() * 0.2F - 0.1F) * 2.0, this.random.nextDouble() * 0.2 * 2.0, (this.random.nextDouble() * 0.2F - 0.1F) * 2.0
        );
        this.value = param4;
    }

    public ExperienceOrb(EntityType<? extends ExperienceOrb> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getLightColor() {
        float var0 = 0.5F;
        var0 = Mth.clamp(var0, 0.0F, 1.0F);
        int var1 = super.getLightColor();
        int var2 = var1 & 0xFF;
        int var3 = var1 >> 16 & 0xFF;
        var2 += (int)(var0 * 15.0F * 16.0F);
        if (var2 > 240) {
            var2 = 240;
        }

        return var2 | var3 << 16;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.throwTime > 0) {
            --this.throwTime;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.isUnderLiquid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        } else if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
        }

        if (this.level.getFluidState(new BlockPos(this)).is(FluidTags.LAVA)) {
            this.setDeltaMovement(
                (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F),
                0.2F,
                (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F)
            );
            this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
        }

        if (!this.level.noCollision(this.getBoundingBox())) {
            this.checkInBlock(this.x, (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.z);
        }

        double var0 = 8.0;
        if (this.followingTime < this.tickCount - 20 + this.getId() % 100) {
            if (this.followingPlayer == null || this.followingPlayer.distanceToSqr(this) > 64.0) {
                this.followingPlayer = this.level.getNearestPlayer(this, 8.0);
            }

            this.followingTime = this.tickCount;
        }

        if (this.followingPlayer != null && this.followingPlayer.isSpectator()) {
            this.followingPlayer = null;
        }

        if (this.followingPlayer != null) {
            Vec3 var1 = new Vec3(
                this.followingPlayer.x - this.x,
                this.followingPlayer.y + (double)this.followingPlayer.getEyeHeight() / 2.0 - this.y,
                this.followingPlayer.z - this.z
            );
            double var2 = var1.lengthSqr();
            if (var2 < 64.0) {
                double var3 = 1.0 - Math.sqrt(var2) / 8.0;
                this.setDeltaMovement(this.getDeltaMovement().add(var1.normalize().scale(var3 * var3 * 0.1)));
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        float var4 = 0.98F;
        if (this.onGround) {
            var4 = this.level.getBlockState(new BlockPos(this.x, this.getBoundingBox().minY - 1.0, this.z)).getBlock().getFriction() * 0.98F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply((double)var4, 0.98, (double)var4));
        if (this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, -0.9, 1.0));
        }

        ++this.tickCount;
        ++this.age;
        if (this.age >= 6000) {
            this.remove();
        }

    }

    private void setUnderwaterMovement() {
        Vec3 var0 = this.getDeltaMovement();
        this.setDeltaMovement(var0.x * 0.99F, Math.min(var0.y + 5.0E-4F, 0.06F), var0.z * 0.99F);
    }

    @Override
    protected void doWaterSplashEffect() {
    }

    @Override
    protected void burn(int param0) {
        this.hurt(DamageSource.IN_FIRE, (float)param0);
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            this.markHurt();
            this.health = (int)((float)this.health - param1);
            if (this.health <= 0) {
                this.remove();
            }

            return false;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        param0.putShort("Health", (short)this.health);
        param0.putShort("Age", (short)this.age);
        param0.putShort("Value", (short)this.value);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        this.health = param0.getShort("Health");
        this.age = param0.getShort("Age");
        this.value = param0.getShort("Value");
    }

    @Override
    public void playerTouch(Player param0) {
        if (!this.level.isClientSide) {
            if (this.throwTime == 0 && param0.takeXpDelay == 0) {
                param0.takeXpDelay = 2;
                param0.take(this, 1);
                Entry<EquipmentSlot, ItemStack> var0 = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, param0);
                if (var0 != null) {
                    ItemStack var1 = var0.getValue();
                    if (!var1.isEmpty() && var1.isDamaged()) {
                        int var2 = Math.min(this.xpToDurability(this.value), var1.getDamageValue());
                        this.value -= this.durabilityToXp(var2);
                        var1.setDamageValue(var1.getDamageValue() - var2);
                    }
                }

                if (this.value > 0) {
                    param0.giveExperiencePoints(this.value);
                }

                this.remove();
            }

        }
    }

    private int durabilityToXp(int param0) {
        return param0 / 2;
    }

    private int xpToDurability(int param0) {
        return param0 * 2;
    }

    public int getValue() {
        return this.value;
    }

    @OnlyIn(Dist.CLIENT)
    public int getIcon() {
        if (this.value >= 2477) {
            return 10;
        } else if (this.value >= 1237) {
            return 9;
        } else if (this.value >= 617) {
            return 8;
        } else if (this.value >= 307) {
            return 7;
        } else if (this.value >= 149) {
            return 6;
        } else if (this.value >= 73) {
            return 5;
        } else if (this.value >= 37) {
            return 4;
        } else if (this.value >= 17) {
            return 3;
        } else if (this.value >= 7) {
            return 2;
        } else {
            return this.value >= 3 ? 1 : 0;
        }
    }

    public static int getExperienceValue(int param0) {
        if (param0 >= 2477) {
            return 2477;
        } else if (param0 >= 1237) {
            return 1237;
        } else if (param0 >= 617) {
            return 617;
        } else if (param0 >= 307) {
            return 307;
        } else if (param0 >= 149) {
            return 149;
        } else if (param0 >= 73) {
            return 73;
        } else if (param0 >= 37) {
            return 37;
        } else if (param0 >= 17) {
            return 17;
        } else if (param0 >= 7) {
            return 7;
        } else {
            return param0 >= 3 ? 3 : 1;
        }
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddExperienceOrbPacket(this);
    }
}
