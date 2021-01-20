package net.minecraft.world.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AreaEffectCloud extends Entity {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
    private Potion potion = Potions.EMPTY;
    private final List<MobEffectInstance> effects = Lists.newArrayList();
    private final Map<Entity, Integer> victims = Maps.newHashMap();
    private int duration = 600;
    private int waitTime = 20;
    private int reapplicationDelay = 20;
    private boolean fixedColor;
    private int durationOnUse;
    private float radiusOnUse;
    private float radiusPerTick;
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;

    public AreaEffectCloud(EntityType<? extends AreaEffectCloud> param0, Level param1) {
        super(param0, param1);
        this.noPhysics = true;
        this.setRadius(3.0F);
    }

    public AreaEffectCloud(Level param0, double param1, double param2, double param3) {
        this(EntityType.AREA_EFFECT_CLOUD, param0);
        this.setPos(param1, param2, param3);
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_COLOR, 0);
        this.getEntityData().define(DATA_RADIUS, 0.5F);
        this.getEntityData().define(DATA_WAITING, false);
        this.getEntityData().define(DATA_PARTICLE, ParticleTypes.ENTITY_EFFECT);
    }

    public void setRadius(float param0) {
        if (!this.level.isClientSide) {
            this.getEntityData().set(DATA_RADIUS, param0);
        }

    }

    @Override
    public void refreshDimensions() {
        double var0 = this.getX();
        double var1 = this.getY();
        double var2 = this.getZ();
        super.refreshDimensions();
        this.setPos(var0, var1, var2);
    }

    public float getRadius() {
        return this.getEntityData().get(DATA_RADIUS);
    }

    public void setPotion(Potion param0) {
        this.potion = param0;
        if (!this.fixedColor) {
            this.updateColor();
        }

    }

    private void updateColor() {
        if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
            this.getEntityData().set(DATA_COLOR, 0);
        } else {
            this.getEntityData().set(DATA_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
        }

    }

    public void addEffect(MobEffectInstance param0) {
        this.effects.add(param0);
        if (!this.fixedColor) {
            this.updateColor();
        }

    }

    public int getColor() {
        return this.getEntityData().get(DATA_COLOR);
    }

    public void setFixedColor(int param0) {
        this.fixedColor = true;
        this.getEntityData().set(DATA_COLOR, param0);
    }

    public ParticleOptions getParticle() {
        return this.getEntityData().get(DATA_PARTICLE);
    }

    public void setParticle(ParticleOptions param0) {
        this.getEntityData().set(DATA_PARTICLE, param0);
    }

    protected void setWaiting(boolean param0) {
        this.getEntityData().set(DATA_WAITING, param0);
    }

    public boolean isWaiting() {
        return this.getEntityData().get(DATA_WAITING);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int param0) {
        this.duration = param0;
    }

    @Override
    public void tick() {
        super.tick();
        boolean var0 = this.isWaiting();
        float var1 = this.getRadius();
        if (this.level.isClientSide) {
            ParticleOptions var2 = this.getParticle();
            if (var0) {
                if (this.random.nextBoolean()) {
                    for(int var3 = 0; var3 < 2; ++var3) {
                        float var4 = this.random.nextFloat() * (float) (Math.PI * 2);
                        float var5 = Mth.sqrt(this.random.nextFloat()) * 0.2F;
                        float var6 = Mth.cos(var4) * var5;
                        float var7 = Mth.sin(var4) * var5;
                        if (var2.getType() == ParticleTypes.ENTITY_EFFECT) {
                            int var8 = this.random.nextBoolean() ? 16777215 : this.getColor();
                            int var9 = var8 >> 16 & 0xFF;
                            int var10 = var8 >> 8 & 0xFF;
                            int var11 = var8 & 0xFF;
                            this.level
                                .addAlwaysVisibleParticle(
                                    var2,
                                    this.getX() + (double)var6,
                                    this.getY(),
                                    this.getZ() + (double)var7,
                                    (double)((float)var9 / 255.0F),
                                    (double)((float)var10 / 255.0F),
                                    (double)((float)var11 / 255.0F)
                                );
                        } else {
                            this.level.addAlwaysVisibleParticle(var2, this.getX() + (double)var6, this.getY(), this.getZ() + (double)var7, 0.0, 0.0, 0.0);
                        }
                    }
                }
            } else {
                float var12 = (float) Math.PI * var1 * var1;

                for(int var13 = 0; (float)var13 < var12; ++var13) {
                    float var14 = this.random.nextFloat() * (float) (Math.PI * 2);
                    float var15 = Mth.sqrt(this.random.nextFloat()) * var1;
                    float var16 = Mth.cos(var14) * var15;
                    float var17 = Mth.sin(var14) * var15;
                    if (var2.getType() == ParticleTypes.ENTITY_EFFECT) {
                        int var18 = this.getColor();
                        int var19 = var18 >> 16 & 0xFF;
                        int var20 = var18 >> 8 & 0xFF;
                        int var21 = var18 & 0xFF;
                        this.level
                            .addAlwaysVisibleParticle(
                                var2,
                                this.getX() + (double)var16,
                                this.getY(),
                                this.getZ() + (double)var17,
                                (double)((float)var19 / 255.0F),
                                (double)((float)var20 / 255.0F),
                                (double)((float)var21 / 255.0F)
                            );
                    } else {
                        this.level
                            .addAlwaysVisibleParticle(
                                var2,
                                this.getX() + (double)var16,
                                this.getY(),
                                this.getZ() + (double)var17,
                                (0.5 - this.random.nextDouble()) * 0.15,
                                0.01F,
                                (0.5 - this.random.nextDouble()) * 0.15
                            );
                    }
                }
            }
        } else {
            if (this.tickCount >= this.waitTime + this.duration) {
                this.discard();
                return;
            }

            boolean var22 = this.tickCount < this.waitTime;
            if (var0 != var22) {
                this.setWaiting(var22);
            }

            if (var22) {
                return;
            }

            if (this.radiusPerTick != 0.0F) {
                var1 += this.radiusPerTick;
                if (var1 < 0.5F) {
                    this.discard();
                    return;
                }

                this.setRadius(var1);
            }

            if (this.tickCount % 5 == 0) {
                this.victims.entrySet().removeIf(param0 -> this.tickCount >= param0.getValue());
                List<MobEffectInstance> var23 = Lists.newArrayList();

                for(MobEffectInstance var24 : this.potion.getEffects()) {
                    var23.add(new MobEffectInstance(var24.getEffect(), var24.getDuration() / 4, var24.getAmplifier(), var24.isAmbient(), var24.isVisible()));
                }

                var23.addAll(this.effects);
                if (var23.isEmpty()) {
                    this.victims.clear();
                } else {
                    List<LivingEntity> var25 = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
                    if (!var25.isEmpty()) {
                        for(LivingEntity var26 : var25) {
                            if (!this.victims.containsKey(var26) && var26.isAffectedByPotions()) {
                                double var27 = var26.getX() - this.getX();
                                double var28 = var26.getZ() - this.getZ();
                                double var29 = var27 * var27 + var28 * var28;
                                if (var29 <= (double)(var1 * var1)) {
                                    this.victims.put(var26, this.tickCount + this.reapplicationDelay);

                                    for(MobEffectInstance var30 : var23) {
                                        if (var30.getEffect().isInstantenous()) {
                                            var30.getEffect().applyInstantenousEffect(this, this.getOwner(), var26, var30.getAmplifier(), 0.5);
                                        } else {
                                            var26.addEffect(new MobEffectInstance(var30));
                                        }
                                    }

                                    if (this.radiusOnUse != 0.0F) {
                                        var1 += this.radiusOnUse;
                                        if (var1 < 0.5F) {
                                            this.discard();
                                            return;
                                        }

                                        this.setRadius(var1);
                                    }

                                    if (this.durationOnUse != 0) {
                                        this.duration += this.durationOnUse;
                                        if (this.duration <= 0) {
                                            this.discard();
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public void setRadiusOnUse(float param0) {
        this.radiusOnUse = param0;
    }

    public void setRadiusPerTick(float param0) {
        this.radiusPerTick = param0;
    }

    public void setWaitTime(int param0) {
        this.waitTime = param0;
    }

    public void setOwner(@Nullable LivingEntity param0) {
        this.owner = param0;
        this.ownerUUID = param0 == null ? null : param0.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level instanceof ServerLevel) {
            Entity var0 = ((ServerLevel)this.level).getEntity(this.ownerUUID);
            if (var0 instanceof LivingEntity) {
                this.owner = (LivingEntity)var0;
            }
        }

        return this.owner;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        this.tickCount = param0.getInt("Age");
        this.duration = param0.getInt("Duration");
        this.waitTime = param0.getInt("WaitTime");
        this.reapplicationDelay = param0.getInt("ReapplicationDelay");
        this.durationOnUse = param0.getInt("DurationOnUse");
        this.radiusOnUse = param0.getFloat("RadiusOnUse");
        this.radiusPerTick = param0.getFloat("RadiusPerTick");
        this.setRadius(param0.getFloat("Radius"));
        if (param0.hasUUID("Owner")) {
            this.ownerUUID = param0.getUUID("Owner");
        }

        if (param0.contains("Particle", 8)) {
            try {
                this.setParticle(ParticleArgument.readParticle(new StringReader(param0.getString("Particle"))));
            } catch (CommandSyntaxException var5) {
                LOGGER.warn("Couldn't load custom particle {}", param0.getString("Particle"), var5);
            }
        }

        if (param0.contains("Color", 99)) {
            this.setFixedColor(param0.getInt("Color"));
        }

        if (param0.contains("Potion", 8)) {
            this.setPotion(PotionUtils.getPotion(param0));
        }

        if (param0.contains("Effects", 9)) {
            ListTag var1 = param0.getList("Effects", 10);
            this.effects.clear();

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                MobEffectInstance var3 = MobEffectInstance.load(var1.getCompound(var2));
                if (var3 != null) {
                    this.addEffect(var3);
                }
            }
        }

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        param0.putInt("Age", this.tickCount);
        param0.putInt("Duration", this.duration);
        param0.putInt("WaitTime", this.waitTime);
        param0.putInt("ReapplicationDelay", this.reapplicationDelay);
        param0.putInt("DurationOnUse", this.durationOnUse);
        param0.putFloat("RadiusOnUse", this.radiusOnUse);
        param0.putFloat("RadiusPerTick", this.radiusPerTick);
        param0.putFloat("Radius", this.getRadius());
        param0.putString("Particle", this.getParticle().writeToString());
        if (this.ownerUUID != null) {
            param0.putUUID("Owner", this.ownerUUID);
        }

        if (this.fixedColor) {
            param0.putInt("Color", this.getColor());
        }

        if (this.potion != Potions.EMPTY) {
            param0.putString("Potion", Registry.POTION.getKey(this.potion).toString());
        }

        if (!this.effects.isEmpty()) {
            ListTag var0 = new ListTag();

            for(MobEffectInstance var1 : this.effects) {
                var0.add(var1.save(new CompoundTag()));
            }

            param0.put("Effects", var0);
        }

    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_RADIUS.equals(param0)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(param0);
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public EntityDimensions getDimensions(Pose param0) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, 0.5F);
    }
}
