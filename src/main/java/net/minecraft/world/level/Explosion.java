package net.minecraft.world.level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
    private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();

    public Explosion(Level param0, @Nullable Entity param1, double param2, double param3, double param4, float param5, List<BlockPos> param6) {
        this(param0, param1, param2, param3, param4, param5, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY, param6);
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
        this(param0, param1, null, null, param2, param3, param4, param5, param6, param7);
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
        Explosion.BlockInteraction param9
    ) {
        this.level = param0;
        this.source = param1;
        this.radius = param7;
        this.x = param4;
        this.y = param5;
        this.z = param6;
        this.fire = param8;
        this.blockInteraction = param9;
        this.damageSource = param2 == null ? DamageSource.explosion(this) : param2;
        this.damageCalculator = param3 == null ? this.makeDamageCalculator(param1) : param3;
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
                        if (param1.level.clip(new ClipContext(var14, param0, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, param1)).getType()
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
                            BlockPos var14 = new BlockPos(var10, var11, var12);
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

        for(int var27 = 0; var27 < var25.size(); ++var27) {
            Entity var28 = var25.get(var27);
            if (!var28.ignoreExplosion()) {
                double var29 = Math.sqrt(var28.distanceToSqr(var26)) / (double)var18;
                if (var29 <= 1.0) {
                    double var30 = var28.getX() - this.x;
                    double var31 = (var28 instanceof PrimedTnt ? var28.getY() : var28.getEyeY()) - this.y;
                    double var32 = var28.getZ() - this.z;
                    double var33 = Math.sqrt(var30 * var30 + var31 * var31 + var32 * var32);
                    if (var33 != 0.0) {
                        var30 /= var33;
                        var31 /= var33;
                        var32 /= var33;
                        double var34 = (double)getSeenPercent(var26, var28);
                        double var35 = (1.0 - var29) * var34;
                        var28.hurt(this.getDamageSource(), (float)((int)((var35 * var35 + var35) / 2.0 * 7.0 * (double)var18 + 1.0)));
                        double var36 = var35;
                        if (var28 instanceof LivingEntity) {
                            var36 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity)var28, var35);
                        }

                        var28.setDeltaMovement(var28.getDeltaMovement().add(var30 * var36, var31 * var36, var32 * var36));
                        if (var28 instanceof Player var37 && !var37.isSpectator() && (!var37.isCreative() || !var37.getAbilities().flying)) {
                            this.hitPlayers.put(var37, new Vec3(var30 * var35, var31 * var35, var32 * var35));
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
                    SoundEvents.GENERIC_EXPLODE,
                    SoundSource.BLOCKS,
                    4.0F,
                    (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F,
                    false
                );
        }

        boolean var0 = this.interactsWithBlocks();
        if (param0) {
            if (!(this.radius < 2.0F) && var0) {
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            } else {
                this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            }
        }

        if (var0) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> var1 = new ObjectArrayList<>();
            boolean var2 = this.getIndirectSourceEntity() instanceof Player;
            Util.shuffle(this.toBlow, this.level.random);

            for(BlockPos var3 : this.toBlow) {
                BlockState var4 = this.level.getBlockState(var3);
                Block var5 = var4.getBlock();
                if (!var4.isAir()) {
                    BlockPos var6 = var3.immutable();
                    this.level.getProfiler().push("explosion_blocks");
                    if (var5.dropFromExplosion(this)) {
                        Level var8 = this.level;
                        if (var8 instanceof ServerLevel var7) {
                            BlockEntity var8x = var4.hasBlockEntity() ? this.level.getBlockEntity(var3) : null;
                            LootContext.Builder var9 = new LootContext.Builder(var7)
                                .withRandom(this.level.random)
                                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(var3))
                                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, var8x)
                                .withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                            if (this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
                                var9.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                            }

                            var4.spawnAfterBreak(var7, var3, ItemStack.EMPTY, var2);
                            var4.getDrops(var9).forEach(param2 -> addBlockDrops(var1, param2, var6));
                        }
                    }

                    this.level.setBlock(var3, Blocks.AIR.defaultBlockState(), 3);
                    var5.wasExploded(this.level, var3, this);
                    this.level.getProfiler().pop();
                }
            }

            for(Pair<ItemStack, BlockPos> var10 : var1) {
                Block.popResource(this.level, var10.getSecond(), var10.getFirst());
            }
        }

        if (this.fire) {
            for(BlockPos var11 : this.toBlow) {
                if (this.random.nextInt(3) == 0
                    && this.level.getBlockState(var11).isAir()
                    && this.level.getBlockState(var11.below()).isSolidRender(this.level, var11.below())) {
                    this.level.setBlockAndUpdate(var11, BaseFireBlock.getState(this.level, var11));
                }
            }
        }

    }

    public boolean interactsWithBlocks() {
        return this.blockInteraction != Explosion.BlockInteraction.KEEP;
    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> param0, ItemStack param1, BlockPos param2) {
        int var0 = param0.size();

        for(int var1 = 0; var1 < var0; ++var1) {
            Pair<ItemStack, BlockPos> var2 = param0.get(var1);
            ItemStack var3 = var2.getFirst();
            if (ItemEntity.areMergable(var3, param1)) {
                ItemStack var4 = ItemEntity.merge(var3, param1, 16);
                param0.set(var1, Pair.of(var4, var2.getSecond()));
                if (param1.isEmpty()) {
                    return;
                }
            }
        }

        param0.add(Pair.of(param1, param2));
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    public Map<Player, Vec3> getHitPlayers() {
        return this.hitPlayers;
    }

    @Nullable
    public LivingEntity getIndirectSourceEntity() {
        if (this.source == null) {
            return null;
        } else {
            Entity var3 = this.source;
            if (var3 instanceof PrimedTnt var0) {
                return var0.getOwner();
            } else {
                var3 = this.source;
                if (var3 instanceof LivingEntity var1) {
                    return var1;
                } else {
                    var3 = this.source;
                    if (var3 instanceof Projectile var2) {
                        var3 = var2.getOwner();
                        if (var3 instanceof LivingEntity) {
                            return (LivingEntity)var3;
                        }
                    }

                    return null;
                }
            }
        }
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

    public static enum BlockInteraction {
        KEEP,
        DESTROY,
        DESTROY_WITH_DECAY;
    }
}
