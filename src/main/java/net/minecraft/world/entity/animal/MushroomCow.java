package net.minecraft.world.entity.animal;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

public class MushroomCow extends Cow implements Shearable {
    private static final EntityDataAccessor<String> DATA_TYPE = SynchedEntityData.defineId(MushroomCow.class, EntityDataSerializers.STRING);
    private MobEffect effect;
    private int effectDuration;
    private UUID lastLightningBoltUUID;

    public MushroomCow(EntityType<? extends MushroomCow> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        return param1.getBlockState(param0.below()).is(Blocks.MYCELIUM) ? 10.0F : param1.getBrightness(param0) - 0.5F;
    }

    public static boolean checkMushroomSpawnRules(EntityType<MushroomCow> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return param1.getBlockState(param3.below()).is(Blocks.MYCELIUM) && param1.getRawBrightness(param3, 0) > 8;
    }

    @Override
    public void thunderHit(ServerLevel param0, LightningBolt param1) {
        UUID var0 = param1.getUUID();
        if (!var0.equals(this.lastLightningBoltUUID)) {
            this.setMushroomType(this.getMushroomType() == MushroomCow.MushroomType.RED ? MushroomCow.MushroomType.BROWN : MushroomCow.MushroomType.RED);
            this.lastLightningBoltUUID = var0;
            this.playSound(SoundEvents.MOOSHROOM_CONVERT, 2.0F, 1.0F);
        }

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE, MushroomCow.MushroomType.RED.type);
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.is(Items.BOWL) && !this.isBaby()) {
            boolean var1 = false;
            ItemStack var2;
            if (this.effect != null) {
                var1 = true;
                var2 = new ItemStack(Items.SUSPICIOUS_STEW);
                SuspiciousStewItem.saveMobEffect(var2, this.effect, this.effectDuration);
                this.effect = null;
                this.effectDuration = 0;
            } else {
                var2 = new ItemStack(Items.MUSHROOM_STEW);
            }

            ItemStack var4 = ItemUtils.createFilledResult(var0, param0, var2, false);
            param0.setItemInHand(param1, var4);
            SoundEvent var5;
            if (var1) {
                var5 = SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY;
            } else {
                var5 = SoundEvents.MOOSHROOM_MILK;
            }

            this.playSound(var5, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else if (var0.is(Items.SHEARS) && this.readyForShearing()) {
            this.shear(SoundSource.PLAYERS);
            this.gameEvent(GameEvent.SHEAR, param0);
            if (!this.level.isClientSide) {
                var0.hurtAndBreak(1, param0, param1x -> param1x.broadcastBreakEvent(param1));
            }

            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else if (this.getMushroomType() == MushroomCow.MushroomType.BROWN && var0.is(ItemTags.SMALL_FLOWERS)) {
            if (this.effect != null) {
                for(int var7 = 0; var7 < 2; ++var7) {
                    this.level
                        .addParticle(
                            ParticleTypes.SMOKE,
                            this.getX() + this.random.nextDouble() / 2.0,
                            this.getY(0.5),
                            this.getZ() + this.random.nextDouble() / 2.0,
                            0.0,
                            this.random.nextDouble() / 5.0,
                            0.0
                        );
                }
            } else {
                Optional<Pair<MobEffect, Integer>> var8 = this.getEffectFromItemStack(var0);
                if (!var8.isPresent()) {
                    return InteractionResult.PASS;
                }

                Pair<MobEffect, Integer> var9 = var8.get();
                if (!param0.getAbilities().instabuild) {
                    var0.shrink(1);
                }

                for(int var10 = 0; var10 < 4; ++var10) {
                    this.level
                        .addParticle(
                            ParticleTypes.EFFECT,
                            this.getX() + this.random.nextDouble() / 2.0,
                            this.getY(0.5),
                            this.getZ() + this.random.nextDouble() / 2.0,
                            0.0,
                            this.random.nextDouble() / 5.0,
                            0.0
                        );
                }

                this.effect = var9.getLeft();
                this.effectDuration = var9.getRight();
                this.playSound(SoundEvents.MOOSHROOM_EAT, 2.0F, 1.0F);
            }

            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            return super.mobInteract(param0, param1);
        }
    }

    @Override
    public void shear(SoundSource param0) {
        this.level.playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, param0, 1.0F, 1.0F);
        if (!this.level.isClientSide()) {
            ((ServerLevel)this.level).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            this.discard();
            Cow var0 = EntityType.COW.create(this.level);
            var0.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
            var0.setHealth(this.getHealth());
            var0.yBodyRot = this.yBodyRot;
            if (this.hasCustomName()) {
                var0.setCustomName(this.getCustomName());
                var0.setCustomNameVisible(this.isCustomNameVisible());
            }

            if (this.isPersistenceRequired()) {
                var0.setPersistenceRequired();
            }

            var0.setInvulnerable(this.isInvulnerable());
            this.level.addFreshEntity(var0);

            for(int var1 = 0; var1 < 5; ++var1) {
                this.level
                    .addFreshEntity(
                        new ItemEntity(this.level, this.getX(), this.getY(1.0), this.getZ(), new ItemStack(this.getMushroomType().blockState.getBlock()))
                    );
            }
        }

    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putString("Type", this.getMushroomType().type);
        if (this.effect != null) {
            param0.putByte("EffectId", (byte)MobEffect.getId(this.effect));
            param0.putInt("EffectDuration", this.effectDuration);
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setMushroomType(MushroomCow.MushroomType.byType(param0.getString("Type")));
        if (param0.contains("EffectId", 1)) {
            this.effect = MobEffect.byId(param0.getByte("EffectId"));
        }

        if (param0.contains("EffectDuration", 3)) {
            this.effectDuration = param0.getInt("EffectDuration");
        }

    }

    private Optional<Pair<MobEffect, Integer>> getEffectFromItemStack(ItemStack param0) {
        Item var0 = param0.getItem();
        if (var0 instanceof BlockItem) {
            Block var1 = ((BlockItem)var0).getBlock();
            if (var1 instanceof FlowerBlock) {
                FlowerBlock var2 = (FlowerBlock)var1;
                return Optional.of(Pair.of(var2.getSuspiciousStewEffect(), var2.getEffectDuration()));
            }
        }

        return Optional.empty();
    }

    private void setMushroomType(MushroomCow.MushroomType param0) {
        this.entityData.set(DATA_TYPE, param0.type);
    }

    public MushroomCow.MushroomType getMushroomType() {
        return MushroomCow.MushroomType.byType(this.entityData.get(DATA_TYPE));
    }

    public MushroomCow getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        MushroomCow var0 = EntityType.MOOSHROOM.create(param0);
        var0.setMushroomType(this.getOffspringType((MushroomCow)param1));
        return var0;
    }

    private MushroomCow.MushroomType getOffspringType(MushroomCow param0) {
        MushroomCow.MushroomType var0 = this.getMushroomType();
        MushroomCow.MushroomType var1 = param0.getMushroomType();
        MushroomCow.MushroomType var2;
        if (var0 == var1 && this.random.nextInt(1024) == 0) {
            var2 = var0 == MushroomCow.MushroomType.BROWN ? MushroomCow.MushroomType.RED : MushroomCow.MushroomType.BROWN;
        } else {
            var2 = this.random.nextBoolean() ? var0 : var1;
        }

        return var2;
    }

    public static enum MushroomType {
        RED("red", Blocks.RED_MUSHROOM.defaultBlockState()),
        BROWN("brown", Blocks.BROWN_MUSHROOM.defaultBlockState());

        private final String type;
        private final BlockState blockState;

        private MushroomType(String param0, BlockState param1) {
            this.type = param0;
            this.blockState = param1;
        }

        @OnlyIn(Dist.CLIENT)
        public BlockState getBlockState() {
            return this.blockState;
        }

        private static MushroomCow.MushroomType byType(String param0) {
            for(MushroomCow.MushroomType var0 : values()) {
                if (var0.type.equals(param0)) {
                    return var0;
                }
            }

            return RED;
        }
    }
}
