package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Explosion {
    private final boolean fire;
    private final Explosion.BlockInteraction blockInteraction;
    private final Random random = new Random();
    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    private final Entity source;
    private final float radius;
    private DamageSource damageSource;
    private final List<BlockPos> toBlow = Lists.newArrayList();
    private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();

    @OnlyIn(Dist.CLIENT)
    public Explosion(Level param0, @Nullable Entity param1, double param2, double param3, double param4, float param5, List<BlockPos> param6) {
        this(param0, param1, param2, param3, param4, param5, false, Explosion.BlockInteraction.DESTROY, param6);
    }

    @OnlyIn(Dist.CLIENT)
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
        this.level = param0;
        this.source = param1;
        this.radius = param5;
        this.x = param2;
        this.y = param3;
        this.z = param4;
        this.fire = param6;
        this.blockInteraction = param7;
        this.damageSource = DamageSource.explosion(this);
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

            for(float var8 = 0.0F; var8 <= 1.0F; var8 = (float)((double)var8 + var1)) {
                for(float var9 = 0.0F; var9 <= 1.0F; var9 = (float)((double)var9 + var2)) {
                    for(float var10 = 0.0F; var10 <= 1.0F; var10 = (float)((double)var10 + var3)) {
                        double var11 = Mth.lerp((double)var8, var0.minX, var0.maxX);
                        double var12 = Mth.lerp((double)var9, var0.minY, var0.maxY);
                        double var13 = Mth.lerp((double)var10, var0.minZ, var0.maxZ);
                        Vec3 var14 = new Vec3(var11 + var4, var12, var13 + var5);
                        if (param1.level.clip(new ClipContext(var14, param0, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, param1)).getType()
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
                            if (!var15.isAir() || !var16.isEmpty()) {
                                float var17 = Math.max(var15.getBlock().getExplosionResistance(), var16.getExplosionResistance());
                                if (this.source != null) {
                                    var17 = this.source.getBlockExplosionResistance(this, this.level, var14, var15, var16, var17);
                                }

                                var9 -= (var17 + 0.3F) * 0.3F;
                            }

                            if (var9 > 0.0F && (this.source == null || this.source.shouldBlockExplode(this, this.level, var14, var15, var9))) {
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
                double var29 = (double)(Mth.sqrt(var28.distanceToSqr(new Vec3(this.x, this.y, this.z))) / var18);
                if (var29 <= 1.0) {
                    double var30 = var28.getX() - this.x;
                    double var31 = var28.getEyeY() - this.y;
                    double var32 = var28.getZ() - this.z;
                    double var33 = (double)Mth.sqrt(var30 * var30 + var31 * var31 + var32 * var32);
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
                        if (var28 instanceof Player) {
                            Player var37 = (Player)var28;
                            if (!var37.isSpectator() && (!var37.isCreative() || !var37.abilities.flying)) {
                                this.hitPlayers.put(var37, new Vec3(var30 * var35, var31 * var35, var32 * var35));
                            }
                        }
                    }
                }
            }
        }

    }

    public void finalizeExplosion(boolean param0) {
        this.level
            .playSound(
                null,
                this.x,
                this.y,
                this.z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                4.0F,
                (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F
            );
        boolean var0 = this.blockInteraction != Explosion.BlockInteraction.NONE;
        if (!(this.radius < 2.0F) && var0) {
            this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0, 0.0, 0.0);
        } else {
            this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
        }

        if (var0) {
            for(BlockPos var1 : this.toBlow) {
                BlockState var2 = this.level.getBlockState(var1);
                Block var3 = var2.getBlock();
                if (param0) {
                    double var4 = (double)((float)var1.getX() + this.level.random.nextFloat());
                    double var5 = (double)((float)var1.getY() + this.level.random.nextFloat());
                    double var6 = (double)((float)var1.getZ() + this.level.random.nextFloat());
                    double var7 = var4 - this.x;
                    double var8 = var5 - this.y;
                    double var9 = var6 - this.z;
                    double var10 = (double)Mth.sqrt(var7 * var7 + var8 * var8 + var9 * var9);
                    var7 /= var10;
                    var8 /= var10;
                    var9 /= var10;
                    double var11 = 0.5 / (var10 / (double)this.radius + 0.1);
                    var11 *= (double)(this.level.random.nextFloat() * this.level.random.nextFloat() + 0.3F);
                    var7 *= var11;
                    var8 *= var11;
                    var9 *= var11;
                    this.level.addParticle(ParticleTypes.POOF, (var4 + this.x) / 2.0, (var5 + this.y) / 2.0, (var6 + this.z) / 2.0, var7, var8, var9);
                    this.level.addParticle(ParticleTypes.SMOKE, var4, var5, var6, var7, var8, var9);
                }

                if (!var2.isAir()) {
                    if (var3.dropFromExplosion(this) && this.level instanceof ServerLevel) {
                        BlockEntity var12 = var3.isEntityBlock() ? this.level.getBlockEntity(var1) : null;
                        LootContext.Builder var13 = new LootContext.Builder((ServerLevel)this.level)
                            .withRandom(this.level.random)
                            .withParameter(LootContextParams.BLOCK_POS, var1)
                            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, var12)
                            .withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                        if (this.blockInteraction == Explosion.BlockInteraction.DESTROY) {
                            var13.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                        }

                        Block.dropResources(var2, var13);
                    }

                    this.level.setBlock(var1, Blocks.AIR.defaultBlockState(), 3);
                    var3.wasExploded(this.level, var1, this);
                }
            }
        }

        if (this.fire) {
            for(BlockPos var14 : this.toBlow) {
                if (this.level.getBlockState(var14).isAir()
                    && this.level.getBlockState(var14.below()).isSolidRender(this.level, var14.below())
                    && this.random.nextInt(3) == 0) {
                    this.level.setBlockAndUpdate(var14, Blocks.FIRE.defaultBlockState());
                }
            }
        }

    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    public void setDamageSource(DamageSource param0) {
        this.damageSource = param0;
    }

    public Map<Player, Vec3> getHitPlayers() {
        return this.hitPlayers;
    }

    @Nullable
    public LivingEntity getSourceMob() {
        if (this.source == null) {
            return null;
        } else if (this.source instanceof PrimedTnt) {
            return ((PrimedTnt)this.source).getOwner();
        } else {
            return this.source instanceof LivingEntity ? (LivingEntity)this.source : null;
        }
    }

    public void clearToBlow() {
        this.toBlow.clear();
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }

    public static enum BlockInteraction {
        NONE,
        BREAK,
        DESTROY;
    }
}
