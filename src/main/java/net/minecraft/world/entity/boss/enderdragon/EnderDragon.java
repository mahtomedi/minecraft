package net.minecraft.world.entity.boss.enderdragon;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnderDragon extends Mob implements Enemy {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(EnderDragon.class, EntityDataSerializers.INT);
    private static final TargetingConditions CRYSTAL_DESTROY_TARGETING = new TargetingConditions().range(64.0);
    public final double[][] positions = new double[64][3];
    public int posPointer = -1;
    private final EnderDragonPart[] subEntities;
    public final EnderDragonPart head;
    private final EnderDragonPart neck;
    private final EnderDragonPart body;
    private final EnderDragonPart tail1;
    private final EnderDragonPart tail2;
    private final EnderDragonPart tail3;
    private final EnderDragonPart wing1;
    private final EnderDragonPart wing2;
    public float oFlapTime;
    public float flapTime;
    public boolean inWall;
    public int dragonDeathTime;
    public float yRotA;
    @Nullable
    public EndCrystal nearestCrystal;
    @Nullable
    private final EndDragonFight dragonFight;
    private final EnderDragonPhaseManager phaseManager;
    private int growlTime = 100;
    private int sittingDamageReceived;
    private final Node[] nodes = new Node[24];
    private final int[] nodeAdjacency = new int[24];
    private final BinaryHeap openSet = new BinaryHeap();

    public EnderDragon(EntityType<? extends EnderDragon> param0, Level param1) {
        super(EntityType.ENDER_DRAGON, param1);
        this.head = new EnderDragonPart(this, "head", 1.0F, 1.0F);
        this.neck = new EnderDragonPart(this, "neck", 3.0F, 3.0F);
        this.body = new EnderDragonPart(this, "body", 5.0F, 3.0F);
        this.tail1 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
        this.tail2 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
        this.tail3 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
        this.wing1 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
        this.wing2 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
        this.subEntities = new EnderDragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};
        this.setHealth(this.getMaxHealth());
        this.noPhysics = true;
        this.noCulling = true;
        if (param1 instanceof ServerLevel) {
            this.dragonFight = ((ServerLevel)param1).dragonFight();
        } else {
            this.dragonFight = null;
        }

        this.phaseManager = new EnderDragonPhaseManager(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 200.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_PHASE, EnderDragonPhase.HOVERING.getId());
    }

    public double[] getLatencyPos(int param0, float param1) {
        if (this.getHealth() <= 0.0F) {
            param1 = 0.0F;
        }

        param1 = 1.0F - param1;
        int var0 = this.posPointer - param0 & 63;
        int var1 = this.posPointer - param0 - 1 & 63;
        double[] var2 = new double[3];
        double var3 = this.positions[var0][0];
        double var4 = Mth.wrapDegrees(this.positions[var1][0] - var3);
        var2[0] = var3 + var4 * (double)param1;
        var3 = this.positions[var0][1];
        var4 = this.positions[var1][1] - var3;
        var2[1] = var3 + var4 * (double)param1;
        var2[2] = Mth.lerp((double)param1, this.positions[var0][2], this.positions[var1][2]);
        return var2;
    }

    @Override
    public void aiStep() {
        if (this.level.isClientSide) {
            this.setHealth(this.getHealth());
            if (!this.isSilent()) {
                float var0 = Mth.cos(this.flapTime * (float) (Math.PI * 2));
                float var1 = Mth.cos(this.oFlapTime * (float) (Math.PI * 2));
                if (var1 <= -0.3F && var0 >= -0.3F) {
                    this.level
                        .playLocalSound(
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.ENDER_DRAGON_FLAP,
                            this.getSoundSource(),
                            5.0F,
                            0.8F + this.random.nextFloat() * 0.3F,
                            false
                        );
                }

                if (!this.phaseManager.getCurrentPhase().isSitting() && --this.growlTime < 0) {
                    this.level
                        .playLocalSound(
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.ENDER_DRAGON_GROWL,
                            this.getSoundSource(),
                            2.5F,
                            0.8F + this.random.nextFloat() * 0.3F,
                            false
                        );
                    this.growlTime = 200 + this.random.nextInt(200);
                }
            }
        }

        this.oFlapTime = this.flapTime;
        if (this.getHealth() <= 0.0F) {
            float var2 = (this.random.nextFloat() - 0.5F) * 8.0F;
            float var3 = (this.random.nextFloat() - 0.5F) * 4.0F;
            float var4 = (this.random.nextFloat() - 0.5F) * 8.0F;
            this.level
                .addParticle(ParticleTypes.EXPLOSION, this.getX() + (double)var2, this.getY() + 2.0 + (double)var3, this.getZ() + (double)var4, 0.0, 0.0, 0.0);
        } else {
            this.checkCrystals();
            Vec3 var5 = this.getDeltaMovement();
            float var6 = 0.2F / (Mth.sqrt(getHorizontalDistanceSqr(var5)) * 10.0F + 1.0F);
            var6 *= (float)Math.pow(2.0, var5.y);
            if (this.phaseManager.getCurrentPhase().isSitting()) {
                this.flapTime += 0.1F;
            } else if (this.inWall) {
                this.flapTime += var6 * 0.5F;
            } else {
                this.flapTime += var6;
            }

            this.yRot = Mth.wrapDegrees(this.yRot);
            if (this.isNoAi()) {
                this.flapTime = 0.5F;
            } else {
                if (this.posPointer < 0) {
                    for(int var7 = 0; var7 < this.positions.length; ++var7) {
                        this.positions[var7][0] = (double)this.yRot;
                        this.positions[var7][1] = this.getY();
                    }
                }

                if (++this.posPointer == this.positions.length) {
                    this.posPointer = 0;
                }

                this.positions[this.posPointer][0] = (double)this.yRot;
                this.positions[this.posPointer][1] = this.getY();
                if (this.level.isClientSide) {
                    if (this.lerpSteps > 0) {
                        double var8 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
                        double var9 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
                        double var10 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
                        double var11 = Mth.wrapDegrees(this.lerpYRot - (double)this.yRot);
                        this.yRot = (float)((double)this.yRot + var11 / (double)this.lerpSteps);
                        this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
                        --this.lerpSteps;
                        this.setPos(var8, var9, var10);
                        this.setRot(this.yRot, this.xRot);
                    }

                    this.phaseManager.getCurrentPhase().doClientTick();
                } else {
                    DragonPhaseInstance var12 = this.phaseManager.getCurrentPhase();
                    var12.doServerTick();
                    if (this.phaseManager.getCurrentPhase() != var12) {
                        var12 = this.phaseManager.getCurrentPhase();
                        var12.doServerTick();
                    }

                    Vec3 var13 = var12.getFlyTargetLocation();
                    if (var13 != null) {
                        double var14 = var13.x - this.getX();
                        double var15 = var13.y - this.getY();
                        double var16 = var13.z - this.getZ();
                        double var17 = var14 * var14 + var15 * var15 + var16 * var16;
                        float var18 = var12.getFlySpeed();
                        double var19 = (double)Mth.sqrt(var14 * var14 + var16 * var16);
                        if (var19 > 0.0) {
                            var15 = Mth.clamp(var15 / var19, (double)(-var18), (double)var18);
                        }

                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, var15 * 0.01, 0.0));
                        this.yRot = Mth.wrapDegrees(this.yRot);
                        double var20 = Mth.clamp(Mth.wrapDegrees(180.0 - Mth.atan2(var14, var16) * 180.0F / (float)Math.PI - (double)this.yRot), -50.0, 50.0);
                        Vec3 var21 = var13.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                        Vec3 var22 = new Vec3(
                                (double)Mth.sin(this.yRot * (float) (Math.PI / 180.0)),
                                this.getDeltaMovement().y,
                                (double)(-Mth.cos(this.yRot * (float) (Math.PI / 180.0)))
                            )
                            .normalize();
                        float var23 = Math.max(((float)var22.dot(var21) + 0.5F) / 1.5F, 0.0F);
                        this.yRotA *= 0.8F;
                        this.yRotA = (float)((double)this.yRotA + var20 * (double)var12.getTurnSpeed());
                        this.yRot += this.yRotA * 0.1F;
                        float var24 = (float)(2.0 / (var17 + 1.0));
                        float var25 = 0.06F;
                        this.moveRelative(0.06F * (var23 * var24 + (1.0F - var24)), new Vec3(0.0, 0.0, -1.0));
                        if (this.inWall) {
                            this.move(MoverType.SELF, this.getDeltaMovement().scale(0.8F));
                        } else {
                            this.move(MoverType.SELF, this.getDeltaMovement());
                        }

                        Vec3 var26 = this.getDeltaMovement().normalize();
                        double var27 = 0.8 + 0.15 * (var26.dot(var22) + 1.0) / 2.0;
                        this.setDeltaMovement(this.getDeltaMovement().multiply(var27, 0.91F, var27));
                    }
                }

                this.yBodyRot = this.yRot;
                Vec3[] var28 = new Vec3[this.subEntities.length];

                for(int var29 = 0; var29 < this.subEntities.length; ++var29) {
                    var28[var29] = new Vec3(this.subEntities[var29].getX(), this.subEntities[var29].getY(), this.subEntities[var29].getZ());
                }

                float var30 = (float)(this.getLatencyPos(5, 1.0F)[1] - this.getLatencyPos(10, 1.0F)[1]) * 10.0F * (float) (Math.PI / 180.0);
                float var31 = Mth.cos(var30);
                float var32 = Mth.sin(var30);
                float var33 = this.yRot * (float) (Math.PI / 180.0);
                float var34 = Mth.sin(var33);
                float var35 = Mth.cos(var33);
                this.tickPart(this.body, (double)(var34 * 0.5F), 0.0, (double)(-var35 * 0.5F));
                this.tickPart(this.wing1, (double)(var35 * 4.5F), 2.0, (double)(var34 * 4.5F));
                this.tickPart(this.wing2, (double)(var35 * -4.5F), 2.0, (double)(var34 * -4.5F));
                if (!this.level.isClientSide && this.hurtTime == 0) {
                    this.knockBack(
                        this.level
                            .getEntities(this, this.wing1.getBoundingBox().inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR)
                    );
                    this.knockBack(
                        this.level
                            .getEntities(this, this.wing2.getBoundingBox().inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR)
                    );
                    this.hurt(this.level.getEntities(this, this.head.getBoundingBox().inflate(1.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                    this.hurt(this.level.getEntities(this, this.neck.getBoundingBox().inflate(1.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                }

                float var36 = Mth.sin(this.yRot * (float) (Math.PI / 180.0) - this.yRotA * 0.01F);
                float var37 = Mth.cos(this.yRot * (float) (Math.PI / 180.0) - this.yRotA * 0.01F);
                float var38 = this.getHeadYOffset();
                this.tickPart(this.head, (double)(var36 * 6.5F * var31), (double)(var38 + var32 * 6.5F), (double)(-var37 * 6.5F * var31));
                this.tickPart(this.neck, (double)(var36 * 5.5F * var31), (double)(var38 + var32 * 5.5F), (double)(-var37 * 5.5F * var31));
                double[] var39 = this.getLatencyPos(5, 1.0F);

                for(int var40 = 0; var40 < 3; ++var40) {
                    EnderDragonPart var41 = null;
                    if (var40 == 0) {
                        var41 = this.tail1;
                    }

                    if (var40 == 1) {
                        var41 = this.tail2;
                    }

                    if (var40 == 2) {
                        var41 = this.tail3;
                    }

                    double[] var42 = this.getLatencyPos(12 + var40 * 2, 1.0F);
                    float var43 = this.yRot * (float) (Math.PI / 180.0) + this.rotWrap(var42[0] - var39[0]) * (float) (Math.PI / 180.0);
                    float var44 = Mth.sin(var43);
                    float var45 = Mth.cos(var43);
                    float var46 = 1.5F;
                    float var47 = (float)(var40 + 1) * 2.0F;
                    this.tickPart(
                        var41,
                        (double)(-(var34 * 1.5F + var44 * var47) * var31),
                        var42[1] - var39[1] - (double)((var47 + 1.5F) * var32) + 1.5,
                        (double)((var35 * 1.5F + var45 * var47) * var31)
                    );
                }

                if (!this.level.isClientSide) {
                    this.inWall = this.checkWalls(this.head.getBoundingBox())
                        | this.checkWalls(this.neck.getBoundingBox())
                        | this.checkWalls(this.body.getBoundingBox());
                    if (this.dragonFight != null) {
                        this.dragonFight.updateDragon(this);
                    }
                }

                for(int var48 = 0; var48 < this.subEntities.length; ++var48) {
                    this.subEntities[var48].xo = var28[var48].x;
                    this.subEntities[var48].yo = var28[var48].y;
                    this.subEntities[var48].zo = var28[var48].z;
                    this.subEntities[var48].xOld = var28[var48].x;
                    this.subEntities[var48].yOld = var28[var48].y;
                    this.subEntities[var48].zOld = var28[var48].z;
                }

            }
        }
    }

    private void tickPart(EnderDragonPart param0, double param1, double param2, double param3) {
        param0.setPos(this.getX() + param1, this.getY() + param2, this.getZ() + param3);
    }

    private float getHeadYOffset() {
        if (this.phaseManager.getCurrentPhase().isSitting()) {
            return -1.0F;
        } else {
            double[] var0 = this.getLatencyPos(5, 1.0F);
            double[] var1 = this.getLatencyPos(0, 1.0F);
            return (float)(var0[1] - var1[1]);
        }
    }

    private void checkCrystals() {
        if (this.nearestCrystal != null) {
            if (this.nearestCrystal.removed) {
                this.nearestCrystal = null;
            } else if (this.tickCount % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0F);
            }
        }

        if (this.random.nextInt(10) == 0) {
            List<EndCrystal> var0 = this.level.getEntitiesOfClass(EndCrystal.class, this.getBoundingBox().inflate(32.0));
            EndCrystal var1 = null;
            double var2 = Double.MAX_VALUE;

            for(EndCrystal var3 : var0) {
                double var4 = var3.distanceToSqr(this);
                if (var4 < var2) {
                    var2 = var4;
                    var1 = var3;
                }
            }

            this.nearestCrystal = var1;
        }

    }

    private void knockBack(List<Entity> param0) {
        double var0 = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0;
        double var1 = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0;

        for(Entity var2 : param0) {
            if (var2 instanceof LivingEntity) {
                double var3 = var2.getX() - var0;
                double var4 = var2.getZ() - var1;
                double var5 = var3 * var3 + var4 * var4;
                var2.push(var3 / var5 * 4.0, 0.2F, var4 / var5 * 4.0);
                if (!this.phaseManager.getCurrentPhase().isSitting() && ((LivingEntity)var2).getLastHurtByMobTimestamp() < var2.tickCount - 2) {
                    var2.hurt(DamageSource.mobAttack(this), 5.0F);
                    this.doEnchantDamageEffects(this, var2);
                }
            }
        }

    }

    private void hurt(List<Entity> param0) {
        for(Entity var0 : param0) {
            if (var0 instanceof LivingEntity) {
                var0.hurt(DamageSource.mobAttack(this), 10.0F);
                this.doEnchantDamageEffects(this, var0);
            }
        }

    }

    private float rotWrap(double param0) {
        return (float)Mth.wrapDegrees(param0);
    }

    private boolean checkWalls(AABB param0) {
        int var0 = Mth.floor(param0.minX);
        int var1 = Mth.floor(param0.minY);
        int var2 = Mth.floor(param0.minZ);
        int var3 = Mth.floor(param0.maxX);
        int var4 = Mth.floor(param0.maxY);
        int var5 = Mth.floor(param0.maxZ);
        boolean var6 = false;
        boolean var7 = false;

        for(int var8 = var0; var8 <= var3; ++var8) {
            for(int var9 = var1; var9 <= var4; ++var9) {
                for(int var10 = var2; var10 <= var5; ++var10) {
                    BlockPos var11 = new BlockPos(var8, var9, var10);
                    BlockState var12 = this.level.getBlockState(var11);
                    Block var13 = var12.getBlock();
                    if (!var12.isAir() && var12.getMaterial() != Material.FIRE) {
                        if (this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && !BlockTags.DRAGON_IMMUNE.contains(var13)) {
                            var7 = this.level.removeBlock(var11, false) || var7;
                        } else {
                            var6 = true;
                        }
                    }
                }
            }
        }

        if (var7) {
            BlockPos var14 = new BlockPos(
                var0 + this.random.nextInt(var3 - var0 + 1), var1 + this.random.nextInt(var4 - var1 + 1), var2 + this.random.nextInt(var5 - var2 + 1)
            );
            this.level.levelEvent(2008, var14, 0);
        }

        return var6;
    }

    public boolean hurt(EnderDragonPart param0, DamageSource param1, float param2) {
        if (this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
            return false;
        } else {
            param2 = this.phaseManager.getCurrentPhase().onHurt(param1, param2);
            if (param0 != this.head) {
                param2 = param2 / 4.0F + Math.min(param2, 1.0F);
            }

            if (param2 < 0.01F) {
                return false;
            } else {
                if (param1.getEntity() instanceof Player || param1.isExplosion()) {
                    float var0 = this.getHealth();
                    this.reallyHurt(param1, param2);
                    if (this.getHealth() <= 0.0F && !this.phaseManager.getCurrentPhase().isSitting()) {
                        this.setHealth(1.0F);
                        this.phaseManager.setPhase(EnderDragonPhase.DYING);
                    }

                    if (this.phaseManager.getCurrentPhase().isSitting()) {
                        this.sittingDamageReceived = (int)((float)this.sittingDamageReceived + (var0 - this.getHealth()));
                        if ((float)this.sittingDamageReceived > 0.25F * this.getMaxHealth()) {
                            this.sittingDamageReceived = 0;
                            this.phaseManager.setPhase(EnderDragonPhase.TAKEOFF);
                        }
                    }
                }

                return true;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (param0 instanceof EntityDamageSource && ((EntityDamageSource)param0).isThorns()) {
            this.hurt(this.body, param0, param1);
        }

        return false;
    }

    protected boolean reallyHurt(DamageSource param0, float param1) {
        return super.hurt(param0, param1);
    }

    @Override
    public void kill() {
        this.remove();
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
            this.dragonFight.setDragonKilled(this);
        }

    }

    @Override
    protected void tickDeath() {
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
        }

        ++this.dragonDeathTime;
        if (this.dragonDeathTime >= 180 && this.dragonDeathTime <= 200) {
            float var0 = (this.random.nextFloat() - 0.5F) * 8.0F;
            float var1 = (this.random.nextFloat() - 0.5F) * 4.0F;
            float var2 = (this.random.nextFloat() - 0.5F) * 8.0F;
            this.level
                .addParticle(
                    ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double)var0, this.getY() + 2.0 + (double)var1, this.getZ() + (double)var2, 0.0, 0.0, 0.0
                );
        }

        boolean var3 = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
        int var4 = 500;
        if (this.dragonFight != null && !this.dragonFight.hasPreviouslyKilledDragon()) {
            var4 = 12000;
        }

        if (!this.level.isClientSide) {
            if (this.dragonDeathTime > 150 && this.dragonDeathTime % 5 == 0 && var3) {
                this.dropExperience(Mth.floor((float)var4 * 0.08F));
            }

            if (this.dragonDeathTime == 1 && !this.isSilent()) {
                this.level.globalLevelEvent(1028, this.blockPosition(), 0);
            }
        }

        this.move(MoverType.SELF, new Vec3(0.0, 0.1F, 0.0));
        this.yRot += 20.0F;
        this.yBodyRot = this.yRot;
        if (this.dragonDeathTime == 200 && !this.level.isClientSide) {
            if (var3) {
                this.dropExperience(Mth.floor((float)var4 * 0.2F));
            }

            if (this.dragonFight != null) {
                this.dragonFight.setDragonKilled(this);
            }

            this.remove();
        }

    }

    private void dropExperience(int param0) {
        while(param0 > 0) {
            int var0 = ExperienceOrb.getExperienceValue(param0);
            param0 -= var0;
            this.level.addFreshEntity(new ExperienceOrb(this.level, this.getX(), this.getY(), this.getZ(), var0));
        }

    }

    public int findClosestNode() {
        if (this.nodes[0] == null) {
            for(int var0 = 0; var0 < 24; ++var0) {
                int var1 = 5;
                int var3;
                int var4;
                if (var0 < 12) {
                    var3 = Mth.floor(60.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 12) * (float)var0)));
                    var4 = Mth.floor(60.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 12) * (float)var0)));
                } else if (var0 < 20) {
                    int var2 = var0 - 12;
                    var3 = Mth.floor(40.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 8) * (float)var2)));
                    var4 = Mth.floor(40.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 8) * (float)var2)));
                    var1 += 10;
                } else {
                    int var71 = var0 - 20;
                    var3 = Mth.floor(20.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 4) * (float)var71)));
                    var4 = Mth.floor(20.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 4) * (float)var71)));
                }

                int var9 = Math.max(
                    this.level.getSeaLevel() + 10,
                    this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(var3, 0, var4)).getY() + var1
                );
                this.nodes[var0] = new Node(var3, var9, var4);
            }

            this.nodeAdjacency[0] = 6146;
            this.nodeAdjacency[1] = 8197;
            this.nodeAdjacency[2] = 8202;
            this.nodeAdjacency[3] = 16404;
            this.nodeAdjacency[4] = 32808;
            this.nodeAdjacency[5] = 32848;
            this.nodeAdjacency[6] = 65696;
            this.nodeAdjacency[7] = 131392;
            this.nodeAdjacency[8] = 131712;
            this.nodeAdjacency[9] = 263424;
            this.nodeAdjacency[10] = 526848;
            this.nodeAdjacency[11] = 525313;
            this.nodeAdjacency[12] = 1581057;
            this.nodeAdjacency[13] = 3166214;
            this.nodeAdjacency[14] = 2138120;
            this.nodeAdjacency[15] = 6373424;
            this.nodeAdjacency[16] = 4358208;
            this.nodeAdjacency[17] = 12910976;
            this.nodeAdjacency[18] = 9044480;
            this.nodeAdjacency[19] = 9706496;
            this.nodeAdjacency[20] = 15216640;
            this.nodeAdjacency[21] = 13688832;
            this.nodeAdjacency[22] = 11763712;
            this.nodeAdjacency[23] = 8257536;
        }

        return this.findClosestNode(this.getX(), this.getY(), this.getZ());
    }

    public int findClosestNode(double param0, double param1, double param2) {
        float var0 = 10000.0F;
        int var1 = 0;
        Node var2 = new Node(Mth.floor(param0), Mth.floor(param1), Mth.floor(param2));
        int var3 = 0;
        if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
            var3 = 12;
        }

        for(int var4 = var3; var4 < 24; ++var4) {
            if (this.nodes[var4] != null) {
                float var5 = this.nodes[var4].distanceToSqr(var2);
                if (var5 < var0) {
                    var0 = var5;
                    var1 = var4;
                }
            }
        }

        return var1;
    }

    @Nullable
    public Path findPath(int param0, int param1, @Nullable Node param2) {
        for(int var0 = 0; var0 < 24; ++var0) {
            Node var1 = this.nodes[var0];
            var1.closed = false;
            var1.f = 0.0F;
            var1.g = 0.0F;
            var1.h = 0.0F;
            var1.cameFrom = null;
            var1.heapIdx = -1;
        }

        Node var2 = this.nodes[param0];
        Node var3 = this.nodes[param1];
        var2.g = 0.0F;
        var2.h = var2.distanceTo(var3);
        var2.f = var2.h;
        this.openSet.clear();
        this.openSet.insert(var2);
        Node var4 = var2;
        int var5 = 0;
        if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
            var5 = 12;
        }

        while(!this.openSet.isEmpty()) {
            Node var6 = this.openSet.pop();
            if (var6.equals(var3)) {
                if (param2 != null) {
                    param2.cameFrom = var3;
                    var3 = param2;
                }

                return this.reconstructPath(var2, var3);
            }

            if (var6.distanceTo(var3) < var4.distanceTo(var3)) {
                var4 = var6;
            }

            var6.closed = true;
            int var7 = 0;

            for(int var8 = 0; var8 < 24; ++var8) {
                if (this.nodes[var8] == var6) {
                    var7 = var8;
                    break;
                }
            }

            for(int var9 = var5; var9 < 24; ++var9) {
                if ((this.nodeAdjacency[var7] & 1 << var9) > 0) {
                    Node var10 = this.nodes[var9];
                    if (!var10.closed) {
                        float var11 = var6.g + var6.distanceTo(var10);
                        if (!var10.inOpenSet() || var11 < var10.g) {
                            var10.cameFrom = var6;
                            var10.g = var11;
                            var10.h = var10.distanceTo(var3);
                            if (var10.inOpenSet()) {
                                this.openSet.changeCost(var10, var10.g + var10.h);
                            } else {
                                var10.f = var10.g + var10.h;
                                this.openSet.insert(var10);
                            }
                        }
                    }
                }
            }
        }

        if (var4 == var2) {
            return null;
        } else {
            LOGGER.debug("Failed to find path from {} to {}", param0, param1);
            if (param2 != null) {
                param2.cameFrom = var4;
                var4 = param2;
            }

            return this.reconstructPath(var2, var4);
        }
    }

    private Path reconstructPath(Node param0, Node param1) {
        List<Node> var0 = Lists.newArrayList();
        Node var1 = param1;
        var0.add(0, param1);

        while(var1.cameFrom != null) {
            var1 = var1.cameFrom;
            var0.add(0, var1);
        }

        return new Path(var0, new BlockPos(param1.x, param1.y, param1.z), true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("DragonPhase", this.phaseManager.getCurrentPhase().getPhase().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("DragonPhase")) {
            this.phaseManager.setPhase(EnderDragonPhase.getById(param0.getInt("DragonPhase")));
        }

    }

    @Override
    public void checkDespawn() {
    }

    public EnderDragonPart[] getSubEntities() {
        return this.subEntities;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public float getHeadPartYOffset(int param0, double[] param1, double[] param2) {
        DragonPhaseInstance var0 = this.phaseManager.getCurrentPhase();
        EnderDragonPhase<? extends DragonPhaseInstance> var1 = var0.getPhase();
        double var4;
        if (var1 == EnderDragonPhase.LANDING || var1 == EnderDragonPhase.TAKEOFF) {
            BlockPos var2 = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
            float var3 = Math.max(Mth.sqrt(var2.distSqr(this.position(), true)) / 4.0F, 1.0F);
            var4 = (double)((float)param0 / var3);
        } else if (var0.isSitting()) {
            var4 = (double)param0;
        } else if (param0 == 6) {
            var4 = 0.0;
        } else {
            var4 = param2[1] - param1[1];
        }

        return (float)var4;
    }

    public Vec3 getHeadLookVector(float param0) {
        DragonPhaseInstance var0 = this.phaseManager.getCurrentPhase();
        EnderDragonPhase<? extends DragonPhaseInstance> var1 = var0.getPhase();
        Vec3 var7;
        if (var1 == EnderDragonPhase.LANDING || var1 == EnderDragonPhase.TAKEOFF) {
            BlockPos var2 = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
            float var3 = Math.max(Mth.sqrt(var2.distSqr(this.position(), true)) / 4.0F, 1.0F);
            float var4 = 6.0F / var3;
            float var5 = this.xRot;
            float var6 = 1.5F;
            this.xRot = -var4 * 1.5F * 5.0F;
            var7 = this.getViewVector(param0);
            this.xRot = var5;
        } else if (var0.isSitting()) {
            float var8 = this.xRot;
            float var9 = 1.5F;
            this.xRot = -45.0F;
            var7 = this.getViewVector(param0);
            this.xRot = var8;
        } else {
            var7 = this.getViewVector(param0);
        }

        return var7;
    }

    public void onCrystalDestroyed(EndCrystal param0, BlockPos param1, DamageSource param2) {
        Player var0;
        if (param2.getEntity() instanceof Player) {
            var0 = (Player)param2.getEntity();
        } else {
            var0 = this.level.getNearestPlayer(CRYSTAL_DESTROY_TARGETING, (double)param1.getX(), (double)param1.getY(), (double)param1.getZ());
        }

        if (param0 == this.nearestCrystal) {
            this.hurt(this.head, DamageSource.explosion(var0), 10.0F);
        }

        this.phaseManager.getCurrentPhase().onCrystalDestroyed(param0, param1, param2, var0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_PHASE.equals(param0) && this.level.isClientSide) {
            this.phaseManager.setPhase(EnderDragonPhase.getById(this.getEntityData().get(DATA_PHASE)));
        }

        super.onSyncedDataUpdated(param0);
    }

    public EnderDragonPhaseManager getPhaseManager() {
        return this.phaseManager;
    }

    @Nullable
    public EndDragonFight getDragonFight() {
        return this.dragonFight;
    }

    @Override
    public boolean addEffect(MobEffectInstance param0) {
        return false;
    }

    @Override
    protected boolean canRide(Entity param0) {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }
}
