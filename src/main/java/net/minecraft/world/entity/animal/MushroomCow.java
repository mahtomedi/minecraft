package net.minecraft.world.entity.animal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class MushroomCow extends Cow implements Shearable, VariantHolder<MushroomCow.MushroomType> {
    private static final EntityDataAccessor<String> DATA_TYPE = SynchedEntityData.defineId(MushroomCow.class, EntityDataSerializers.STRING);
    private static final int MUTATE_CHANCE = 1024;
    private static final String TAG_STEW_EFFECTS = "stew_effects";
    @Nullable
    private List<SuspiciousEffectHolder.EffectEntry> stewEffects;
    @Nullable
    private UUID lastLightningBoltUUID;

    public MushroomCow(EntityType<? extends MushroomCow> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        return param1.getBlockState(param0.below()).is(Blocks.MYCELIUM) ? 10.0F : param1.getPathfindingCostFromLightLevels(param0);
    }

    public static boolean checkMushroomSpawnRules(
        EntityType<MushroomCow> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4
    ) {
        return param1.getBlockState(param3.below()).is(BlockTags.MOOSHROOMS_SPAWNABLE_ON) && isBrightEnoughToSpawn(param1, param3);
    }

    @Override
    public void thunderHit(ServerLevel param0, LightningBolt param1) {
        UUID var0 = param1.getUUID();
        if (!var0.equals(this.lastLightningBoltUUID)) {
            this.setVariant(this.getVariant() == MushroomCow.MushroomType.RED ? MushroomCow.MushroomType.BROWN : MushroomCow.MushroomType.RED);
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
            if (this.stewEffects != null) {
                var1 = true;
                var2 = new ItemStack(Items.SUSPICIOUS_STEW);
                SuspiciousStewItem.saveMobEffects(var2, this.stewEffects);
                this.stewEffects = null;
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
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else if (var0.is(Items.SHEARS) && this.readyForShearing()) {
            this.shear(SoundSource.PLAYERS);
            this.gameEvent(GameEvent.SHEAR, param0);
            if (!this.level().isClientSide) {
                var0.hurtAndBreak(1, param0, param1x -> param1x.broadcastBreakEvent(param1));
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else if (this.getVariant() == MushroomCow.MushroomType.BROWN && var0.is(ItemTags.SMALL_FLOWERS)) {
            if (this.stewEffects != null) {
                for(int var7 = 0; var7 < 2; ++var7) {
                    this.level()
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
                Optional<List<SuspiciousEffectHolder.EffectEntry>> var8 = this.getEffectsFromItemStack(var0);
                if (var8.isEmpty()) {
                    return InteractionResult.PASS;
                }

                if (!param0.getAbilities().instabuild) {
                    var0.shrink(1);
                }

                for(int var9 = 0; var9 < 4; ++var9) {
                    this.level()
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

                this.stewEffects = var8.get();
                this.playSound(SoundEvents.MOOSHROOM_EAT, 2.0F, 1.0F);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            return super.mobInteract(param0, param1);
        }
    }

    @Override
    public void shear(SoundSource param0) {
        this.level().playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, param0, 1.0F, 1.0F);
        if (!this.level().isClientSide()) {
            Cow var0 = EntityType.COW.create(this.level());
            if (var0 != null) {
                ((ServerLevel)this.level()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                this.discard();
                var0.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
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
                this.level().addFreshEntity(var0);

                for(int var1 = 0; var1 < 5; ++var1) {
                    this.level()
                        .addFreshEntity(
                            new ItemEntity(this.level(), this.getX(), this.getY(1.0), this.getZ(), new ItemStack(this.getVariant().blockState.getBlock()))
                        );
                }
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
        param0.putString("Type", this.getVariant().getSerializedName());
        if (this.stewEffects != null) {
            SuspiciousEffectHolder.EffectEntry.LIST_CODEC
                .encodeStart(NbtOps.INSTANCE, this.stewEffects)
                .result()
                .ifPresent(param1 -> param0.put("stew_effects", param1));
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setVariant(MushroomCow.MushroomType.byType(param0.getString("Type")));
        if (param0.contains("stew_effects", 9)) {
            SuspiciousEffectHolder.EffectEntry.LIST_CODEC
                .parse(NbtOps.INSTANCE, param0.get("stew_effects"))
                .result()
                .ifPresent(param0x -> this.stewEffects = param0x);
        }

    }

    private Optional<List<SuspiciousEffectHolder.EffectEntry>> getEffectsFromItemStack(ItemStack param0) {
        SuspiciousEffectHolder var0 = SuspiciousEffectHolder.tryGet(param0.getItem());
        return var0 != null ? Optional.of(var0.getSuspiciousEffects()) : Optional.empty();
    }

    public void setVariant(MushroomCow.MushroomType param0) {
        this.entityData.set(DATA_TYPE, param0.type);
    }

    public MushroomCow.MushroomType getVariant() {
        return MushroomCow.MushroomType.byType(this.entityData.get(DATA_TYPE));
    }

    @Nullable
    public MushroomCow getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        MushroomCow var0 = EntityType.MOOSHROOM.create(param0);
        if (var0 != null) {
            var0.setVariant(this.getOffspringType((MushroomCow)param1));
        }

        return var0;
    }

    private MushroomCow.MushroomType getOffspringType(MushroomCow param0) {
        MushroomCow.MushroomType var0 = this.getVariant();
        MushroomCow.MushroomType var1 = param0.getVariant();
        MushroomCow.MushroomType var2;
        if (var0 == var1 && this.random.nextInt(1024) == 0) {
            var2 = var0 == MushroomCow.MushroomType.BROWN ? MushroomCow.MushroomType.RED : MushroomCow.MushroomType.BROWN;
        } else {
            var2 = this.random.nextBoolean() ? var0 : var1;
        }

        return var2;
    }

    public static enum MushroomType implements StringRepresentable {
        RED("red", Blocks.RED_MUSHROOM.defaultBlockState()),
        BROWN("brown", Blocks.BROWN_MUSHROOM.defaultBlockState());

        public static final StringRepresentable.EnumCodec<MushroomCow.MushroomType> CODEC = StringRepresentable.fromEnum(MushroomCow.MushroomType::values);
        final String type;
        final BlockState blockState;

        private MushroomType(String param0, BlockState param1) {
            this.type = param0;
            this.blockState = param1;
        }

        public BlockState getBlockState() {
            return this.blockState;
        }

        @Override
        public String getSerializedName() {
            return this.type;
        }

        static MushroomCow.MushroomType byType(String param0) {
            return CODEC.byName(param0, RED);
        }
    }
}
