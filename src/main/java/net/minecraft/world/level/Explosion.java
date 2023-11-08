package net.minecraft.world.level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Explosion {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
    private final boolean fire;
    private final Explosion.BlockInteraction blockInteraction;
    private final RandomSource random = RandomSource.create();
    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    private final Entity source;
    private final float radius;
    @Nullable
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final ParticleOptions smallExplosionParticles;
    private final ParticleOptions largeExplosionParticles;
    private final SoundEvent explosionSound;
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
    private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();

    public static DamageSource getDefaultDamageSource(Level param0, @Nullable Entity param1) {
        return param0.damageSources().explosion(param1, getIndirectSourceEntityInternal(param1));
    }

    public Explosion(
        Level param0,
        @Nullable Entity param1,
        double param2,
        double param3,
        double param4,
        float param5,
        List<BlockPos> param6,
        Explosion.BlockInteraction param7,
        ParticleOptions param8,
        ParticleOptions param9,
        SoundEvent param10
    ) {
        this(param0, param1, getDefaultDamageSource(param0, param1), null, param2, param3, param4, param5, false, param7, param8, param9, param10);
        this.toBlow.addAll(param6);
    }

    public Explosion(
        Level param0,
        @Nullable Entity param1,
        double param2,
        double param3,
        double param4,
        float param5,
        boolean param6,
        Explosion.BlockInteraction param7,
        List<BlockPos> param8
    ) {
        this(param0, param1, param2, param3, param4, param5, param6, param7);
        this.toBlow.addAll(param8);
    }

    public Explosion(
        Level param0, @Nullable Entity param1, double param2, double param3, double param4, float param5, boolean param6, Explosion.BlockInteraction param7
    ) {
        this(
            param0,
            param1,
            getDefaultDamageSource(param0, param1),
            null,
            param2,
            param3,
            param4,
            param5,
            param6,
            param7,
            ParticleTypes.EXPLOSION,
            ParticleTypes.EXPLOSION_EMITTER,
            SoundEvents.GENERIC_EXPLODE
        );
    }

    public Explosion(
        Level param0,
        @Nullable Entity param1,
        @Nullable DamageSource param2,
        @Nullable ExplosionDamageCalculator param3,
        double param4,
        double param5,
        double param6,
        float param7,
        boolean param8,
        Explosion.BlockInteraction param9,
        ParticleOptions param10,
        ParticleOptions param11,
        SoundEvent param12
    ) {
        this.level = param0;
        this.source = param1;
        this.radius = param7;
        this.x = param4;
        this.y = param5;
        this.z = param6;
        this.fire = param8;
        this.blockInteraction = param9;
        this.damageSource = param2;
        this.damageCalculator = param3 == null ? this.makeDamageCalculator(param1) : param3;
        this.smallExplosionParticles = param10;
        this.largeExplosionParticles = param11;
        this.explosionSound = param12;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity param0) {
        return (ExplosionDamageCalculator)(param0 == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(param0));
    }

    public static float getSeenPercent(Vec3 param0, Entity param1) {
        AABB var0 = param1.getBoundingBox();
        double var1 = 1.0 / ((var0.maxX - var0.minX) * 2.0 + 1.0);
        double var2 = 1.0 / ((var0.maxY - var0.minY) * 2.0 + 1.0);
        double var3 = 1.0 / ((var0.maxZ - var0.minZ) * 2.0 + 1.0);
        double var4 = (1.0 - Math.floor(1.0 / var1) * var1) / 2.0;
        double var5 = (1.0 - Math.floor(1.0 / var3) * var3) / 2.0;
        if (!(var1 < 0.0) && !(var2 < 0.0) && !(var3 < 0.0)) {
            int var6 = 0;
            int var7 = 0;

            for(double var8 = 0.0; var8 <= 1.0; var8 += var1) {
                for(double var9 = 0.0; var9 <= 1.0; var9 += var2) {
                    for(double var10 = 0.0; var10 <= 1.0; var10 += var3) {
                        double var11 = Mth.lerp(var8, var0.minX, var0.maxX);
                        double var12 = Mth.lerp(var9, var0.minY, var0.maxY);
                        double var13 = Mth.lerp(var10, var0.minZ, var0.maxZ);
                        Vec3 var14 = new Vec3(var11 + var4, var12, var13 + var5);
                        if (param1.level().clip(new ClipContext(var14, param0, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, param1)).getType()
                            == HitResult.Type.MISS) {
                            ++var6;
                        }

                        ++var7;
                    }
                }
            }

            return (float)var6 / (float)var7;
        } else {
            return 0.0F;
        }
    }

    public float radius() {
        return this.radius;
    }

    public Vec3 center() {
        return new Vec3(this.x, this.y, this.z);
    }

    public void explode() {
        this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
        Set<BlockPos> var0 = Sets.newHashSet();
        int var1 = 16;

        for(int var2 = 0; var2 < 16; ++var2) {
            for(int var3 = 0; var3 < 16; ++var3) {
                for(int var4 = 0; var4 < 16; ++var4) {
                    if (var2 == 0 || var2 == 15 || var3 == 0 || var3 == 15 || var4 == 0 || var4 == 15) {
                        double var5 = (double)((float)var2 / 15.0F * 2.0F - 1.0F);
                        double var6 = (double)((float)var3 / 15.0F * 2.0F - 1.0F);
                        double var7 = (double)((float)var4 / 15.0F * 2.0F - 1.0F);
                        double var8 = Math.sqrt(var5 * var5 + var6 * var6 + var7 * var7);
                        var5 /= var8;
                        var6 /= var8;
                        var7 /= var8;
                        float var9 = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double var10 = this.x;
                        double var11 = this.y;
                        double var12 = this.z;

                        for(float var13 = 0.3F; var9 > 0.0F; var9 -= 0.22500001F) {
                            BlockPos var14 = BlockPos.containing(var10, var11, var12);
                            BlockState var15 = this.level.getBlockState(var14);
                            FluidState var16 = this.level.getFluidState(var14);
                            if (!this.level.isInWorldBounds(var14)) {
                                break;
                            }

                            Optional<Float> var17 = this.damageCalculator.getBlockExplosionResistance(this, this.level, var14, var15, var16);
                            if (var17.isPresent()) {
                                var9 -= (var17.get() + 0.3F) * 0.3F;
                            }

                            if (var9 > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, var14, var15, var9)) {
                                var0.add(var14);
                            }

                            var10 += var5 * 0.3F;
                            var11 += var6 * 0.3F;
                            var12 += var7 * 0.3F;
                        }
                    }
                }
            }
        }

        this.toBlow.addAll(var0);
        float var18 = this.radius * 2.0F;
        int var19 = Mth.floor(this.x - (double)var18 - 1.0);
        int var20 = Mth.floor(this.x + (double)var18 + 1.0);
        int var21 = Mth.floor(this.y - (double)var18 - 1.0);
        int var22 = Mth.floor(this.y + (double)var18 + 1.0);
        int var23 = Mth.floor(this.z - (double)var18 - 1.0);
        int var24 = Mth.floor(this.z + (double)var18 + 1.0);
        List<Entity> var25 = this.level
            .getEntities(this.source, new AABB((double)var19, (double)var21, (double)var23, (double)var20, (double)var22, (double)var24));
        Vec3 var26 = new Vec3(this.x, this.y, this.z);

        for(Entity var27 : var25) {
            if (!var27.ignoreExplosion(this)) {
                double var28 = Math.sqrt(var27.distanceToSqr(var26)) / (double)var18;
                if (var28 <= 1.0) {
                    double var29 = var27.getX() - this.x;
                    double var30 = (var27 instanceof PrimedTnt ? var27.getY() : var27.getEyeY()) - this.y;
                    double var31 = var27.getZ() - this.z;
                    double var32 = Math.sqrt(var29 * var29 + var30 * var30 + var31 * var31);
                    if (var32 != 0.0) {
                        var29 /= var32;
                        var30 /= var32;
                        var31 /= var32;
                        if (this.damageSource != null) {
                            var27.hurt(this.damageSource, this.damageCalculator.getEntityDamageAmount(this, var27));
                        }

                        double var33 = (1.0 - var28) * (double)getSeenPercent(var26, var27);
                        double var35;
                        if (var27 instanceof LivingEntity var34) {
                            var35 = ProtectionEnchantment.getExplosionKnockbackAfterDampener(var34, var33);
                        } else {
                            var35 = var33;
                        }

                        var29 *= var35;
                        var30 *= var35;
                        var31 *= var35;
                        Vec3 var37 = new Vec3(var29, var30, var31);
                        var27.setDeltaMovement(var27.getDeltaMovement().add(var37));
                        if (var27 instanceof Player var38 && !var38.isSpectator() && (!var38.isCreative() || !var38.getAbilities().flying)) {
                            this.hitPlayers.put(var38, var37);
                        }
                    }
                }
            }
        }

    }

    public void finalizeExplosion(boolean param0) {
        if (this.level.isClientSide) {
            this.level
                .playLocalSound(
                    this.x,
                    this.y,
                    this.z,
                    this.explosionSound,
                    SoundSource.BLOCKS,
                    4.0F,
                    (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F,
                    false
                );
        }

        boolean var0 = this.interactsWithBlocks();
        if (param0) {
            ParticleOptions var2;
            if (!(this.radius < 2.0F) && var0) {
                var2 = this.largeExplosionParticles;
            } else {
                var2 = this.smallExplosionParticles;
            }

            this.level.addParticle(var2, this.x, this.y, this.z, 1.0, 0.0, 0.0);
        }

        if (var0) {
            this.level.getProfiler().push("explosion_blocks");
            List<Pair<ItemStack, BlockPos>> var3 = new ArrayList<>();
            Util.shuffle(this.toBlow, this.level.random);

            for(BlockPos var4 : this.toBlow) {
                this.level.getBlockState(var4).onExplosionHit(this.level, var4, this, (param1, param2) -> addOrAppendStack(var3, param1, param2));
            }

            for(Pair<ItemStack, BlockPos> var5 : var3) {
                Block.popResource(this.level, var5.getSecond(), var5.getFirst());
            }

            this.level.getProfiler().pop();
        }

        if (this.fire) {
            for(BlockPos var6 : this.toBlow) {
                if (this.random.nextInt(3) == 0
                    && this.level.getBlockState(var6).isAir()
                    && this.level.getBlockState(var6.below()).isSolidRender(this.level, var6.below())) {
                    this.level.setBlockAndUpdate(var6, BaseFireBlock.getState(this.level, var6));
                }
            }
        }

    }

    private static void addOrAppendStack(List<Pair<ItemStack, BlockPos>> param0, ItemStack param1, BlockPos param2) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            Pair<ItemStack, BlockPos> var1 = param0.get(var0);
            ItemStack var2 = var1.getFirst();
            if (ItemEntity.areMergable(var2, param1)) {
                param0.set(var0, Pair.of(ItemEntity.merge(param1, var2, 16), var1.getSecond()));
                return;
            }
        }

        param0.add(Pair.of(param1, param2));
    }

    public boolean interactsWithBlocks() {
        return this.blockInteraction != Explosion.BlockInteraction.KEEP;
    }

    public Map<Player, Vec3> getHitPlayers() {
        return this.hitPlayers;
    }

    @Nullable
    private static LivingEntity getIndirectSourceEntityInternal(@Nullable Entity param0) {
        if (param0 == null) {
            return null;
        } else if (param0 instanceof PrimedTnt var0) {
            return var0.getOwner();
        } else if (param0 instanceof LivingEntity var1) {
            return var1;
        } else {
            if (param0 instanceof Projectile var2) {
                Entity var3 = var2.getOwner();
                if (var3 instanceof LivingEntity) {
                    return (LivingEntity)var3;
                }
            }

            return null;
        }
    }

    @Nullable
    public LivingEntity getIndirectSourceEntity() {
        return getIndirectSourceEntityInternal(this.source);
    }

    @Nullable
    public Entity getDirectSourceEntity() {
        return this.source;
    }

    public void clearToBlow() {
        this.toBlow.clear();
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }

    public Explosion.BlockInteraction getBlockInteraction() {
        return this.blockInteraction;
    }

    public ParticleOptions getSmallExplosionParticles() {
        return this.smallExplosionParticles;
    }

    public ParticleOptions getLargeExplosionParticles() {
        return this.largeExplosionParticles;
    }

    public SoundEvent getExplosionSound() {
        return this.explosionSound;
    }

    public static enum BlockInteraction {
        KEEP,
        DESTROY,
        DESTROY_WITH_DECAY,
        TRIGGER_BLOCK;
    }
}
