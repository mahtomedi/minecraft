package net.minecraft.world.entity.projectile;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
    value = Dist.CLIENT,
    _interface = ItemSupplier.class
)
public class EyeOfEnder extends Entity implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(EyeOfEnder.class, EntityDataSerializers.ITEM_STACK);
    private double tx;
    private double ty;
    private double tz;
    private int life;
    private boolean surviveAfterDeath;

    public EyeOfEnder(EntityType<? extends EyeOfEnder> param0, Level param1) {
        super(param0, param1);
    }

    public EyeOfEnder(Level param0, double param1, double param2, double param3) {
        this(EntityType.EYE_OF_ENDER, param0);
        this.life = 0;
        this.setPos(param1, param2, param3);
    }

    public void setItem(ItemStack param0) {
        if (param0.getItem() != Items.ENDER_EYE || param0.hasTag()) {
            this.getEntityData().set(DATA_ITEM_STACK, Util.make(param0.copy(), param0x -> param0x.setCount(1)));
        }

    }

    private ItemStack getItemRaw() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    @Override
    public ItemStack getItem() {
        ItemStack var0 = this.getItemRaw();
        return var0.isEmpty() ? new ItemStack(Items.ENDER_EYE) : var0;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        double var0 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(var0)) {
            var0 = 4.0;
        }

        var0 *= 64.0;
        return param0 < var0 * var0;
    }

    public void signalTo(BlockPos param0) {
        double var0 = (double)param0.getX();
        int var1 = param0.getY();
        double var2 = (double)param0.getZ();
        double var3 = var0 - this.x;
        double var4 = var2 - this.z;
        float var5 = Mth.sqrt(var3 * var3 + var4 * var4);
        if (var5 > 12.0F) {
            this.tx = this.x + var3 / (double)var5 * 12.0;
            this.tz = this.z + var4 / (double)var5 * 12.0;
            this.ty = this.y + 8.0;
        } else {
            this.tx = var0;
            this.ty = (double)var1;
            this.tz = var2;
        }

        this.life = 0;
        this.surviveAfterDeath = this.random.nextInt(5) > 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void lerpMotion(double param0, double param1, double param2) {
        this.setDeltaMovement(param0, param1, param2);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float var0 = Mth.sqrt(param0 * param0 + param2 * param2);
            this.yRot = (float)(Mth.atan2(param0, param2) * 180.0F / (float)Math.PI);
            this.xRot = (float)(Mth.atan2(param1, (double)var0) * 180.0F / (float)Math.PI);
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }

    }

    @Override
    public void tick() {
        super.tick();
        Vec3 var0 = this.getDeltaMovement();
        this.x += var0.x;
        this.y += var0.y;
        this.z += var0.z;
        float var1 = Mth.sqrt(getHorizontalDistanceSqr(var0));
        this.yRot = (float)(Mth.atan2(var0.x, var0.z) * 180.0F / (float)Math.PI);
        this.xRot = (float)(Mth.atan2(var0.y, (double)var1) * 180.0F / (float)Math.PI);

        while(this.xRot - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }

        while(this.xRot - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }

        while(this.yRot - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while(this.yRot - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        this.xRot = Mth.lerp(0.2F, this.xRotO, this.xRot);
        this.yRot = Mth.lerp(0.2F, this.yRotO, this.yRot);
        if (!this.level.isClientSide) {
            double var2 = this.tx - this.x;
            double var3 = this.tz - this.z;
            float var4 = (float)Math.sqrt(var2 * var2 + var3 * var3);
            float var5 = (float)Mth.atan2(var3, var2);
            double var6 = Mth.lerp(0.0025, (double)var1, (double)var4);
            double var7 = var0.y;
            if (var4 < 1.0F) {
                var6 *= 0.8;
                var7 *= 0.8;
            }

            int var8 = this.y < this.ty ? 1 : -1;
            var0 = new Vec3(Math.cos((double)var5) * var6, var7 + ((double)var8 - var7) * 0.015F, Math.sin((double)var5) * var6);
            this.setDeltaMovement(var0);
        }

        float var9 = 0.25F;
        if (this.isInWater()) {
            for(int var10 = 0; var10 < 4; ++var10) {
                this.level.addParticle(ParticleTypes.BUBBLE, this.x - var0.x * 0.25, this.y - var0.y * 0.25, this.z - var0.z * 0.25, var0.x, var0.y, var0.z);
            }
        } else {
            this.level
                .addParticle(
                    ParticleTypes.PORTAL,
                    this.x - var0.x * 0.25 + this.random.nextDouble() * 0.6 - 0.3,
                    this.y - var0.y * 0.25 - 0.5,
                    this.z - var0.z * 0.25 + this.random.nextDouble() * 0.6 - 0.3,
                    var0.x,
                    var0.y,
                    var0.z
                );
        }

        if (!this.level.isClientSide) {
            this.setPos(this.x, this.y, this.z);
            ++this.life;
            if (this.life > 80 && !this.level.isClientSide) {
                this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
                this.remove();
                if (this.surviveAfterDeath) {
                    this.level.addFreshEntity(new ItemEntity(this.level, this.x, this.y, this.z, this.getItem()));
                } else {
                    this.level.levelEvent(2003, new BlockPos(this), 0);
                }
            }
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        ItemStack var0 = this.getItemRaw();
        if (!var0.isEmpty()) {
            param0.put("Item", var0.save(new CompoundTag()));
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        ItemStack var0 = ItemStack.of(param0.getCompound("Item"));
        this.setItem(var0);
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getLightColor() {
        return 15728880;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
