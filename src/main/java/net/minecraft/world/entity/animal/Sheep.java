package net.minecraft.world.entity.animal;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Sheep extends Animal {
    private static final EntityDataAccessor<Byte> DATA_WOOL_ID = SynchedEntityData.defineId(Sheep.class, EntityDataSerializers.BYTE);
    private static final Map<DyeColor, ItemLike> ITEM_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), param0 -> {
        param0.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
        param0.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
        param0.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
        param0.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
        param0.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
        param0.put(DyeColor.LIME, Blocks.LIME_WOOL);
        param0.put(DyeColor.PINK, Blocks.PINK_WOOL);
        param0.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
        param0.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
        param0.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
        param0.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
        param0.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
        param0.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
        param0.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
        param0.put(DyeColor.RED, Blocks.RED_WOOL);
        param0.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
    });
    private static final Map<DyeColor, float[]> COLORARRAY_BY_COLOR = Maps.newEnumMap(
        Arrays.stream(DyeColor.values()).collect(Collectors.toMap(param0 -> param0, Sheep::createSheepColor))
    );
    private int eatAnimationTick;
    private EatBlockGoal eatBlockGoal;

    private static float[] createSheepColor(DyeColor param0) {
        if (param0 == DyeColor.WHITE) {
            return new float[]{0.9019608F, 0.9019608F, 0.9019608F};
        } else {
            float[] var0 = param0.getTextureDiffuseColors();
            float var1 = 0.75F;
            return new float[]{var0[0] * 0.75F, var0[1] * 0.75F, var0[2] * 0.75F};
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static float[] getColorArray(DyeColor param0) {
        return COLORARRAY_BY_COLOR.get(param0);
    }

    public Sheep(EntityType<? extends Sheep> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerGoals() {
        this.eatBlockGoal = new EatBlockGoal(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, Ingredient.of(Items.WHEAT), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, this.eatBlockGoal);
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep() {
        this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        if (this.level.isClientSide) {
            this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        }

        super.aiStep();
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_WOOL_ID, (byte)0);
    }

    @Override
    public ResourceLocation getDefaultLootTable() {
        if (this.isSheared()) {
            return this.getType().getDefaultLootTable();
        } else {
            switch(this.getColor()) {
                case WHITE:
                default:
                    return BuiltInLootTables.SHEEP_WHITE;
                case ORANGE:
                    return BuiltInLootTables.SHEEP_ORANGE;
                case MAGENTA:
                    return BuiltInLootTables.SHEEP_MAGENTA;
                case LIGHT_BLUE:
                    return BuiltInLootTables.SHEEP_LIGHT_BLUE;
                case YELLOW:
                    return BuiltInLootTables.SHEEP_YELLOW;
                case LIME:
                    return BuiltInLootTables.SHEEP_LIME;
                case PINK:
                    return BuiltInLootTables.SHEEP_PINK;
                case GRAY:
                    return BuiltInLootTables.SHEEP_GRAY;
                case LIGHT_GRAY:
                    return BuiltInLootTables.SHEEP_LIGHT_GRAY;
                case CYAN:
                    return BuiltInLootTables.SHEEP_CYAN;
                case PURPLE:
                    return BuiltInLootTables.SHEEP_PURPLE;
                case BLUE:
                    return BuiltInLootTables.SHEEP_BLUE;
                case BROWN:
                    return BuiltInLootTables.SHEEP_BROWN;
                case GREEN:
                    return BuiltInLootTables.SHEEP_GREEN;
                case RED:
                    return BuiltInLootTables.SHEEP_RED;
                case BLACK:
                    return BuiltInLootTables.SHEEP_BLACK;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 10) {
            this.eatAnimationTick = 40;
        } else {
            super.handleEntityEvent(param0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public float getHeadEatPositionScale(float param0) {
        if (this.eatAnimationTick <= 0) {
            return 0.0F;
        } else if (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36) {
            return 1.0F;
        } else {
            return this.eatAnimationTick < 4 ? ((float)this.eatAnimationTick - param0) / 4.0F : -((float)(this.eatAnimationTick - 40) - param0) / 4.0F;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public float getHeadEatAngleScale(float param0) {
        if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
            float var0 = ((float)(this.eatAnimationTick - 4) - param0) / 32.0F;
            return (float) (Math.PI / 5) + 0.21991149F * Mth.sin(var0 * 28.7F);
        } else {
            return this.eatAnimationTick > 0 ? (float) (Math.PI / 5) : this.xRot * (float) (Math.PI / 180.0);
        }
    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.getItem() == Items.SHEARS && !this.isSheared() && !this.isBaby()) {
            this.shear();
            this.level.playSound(null, this, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!this.level.isClientSide) {
                var0.hurtAndBreak(1, param0, param1x -> param1x.broadcastBreakEvent(param1));
            }

            return true;
        } else {
            return super.mobInteract(param0, param1);
        }
    }

    public void shear() {
        if (!this.level.isClientSide) {
            this.setSheared(true);
            int var0 = 1 + this.random.nextInt(3);

            for(int var1 = 0; var1 < var0; ++var1) {
                ItemEntity var2 = this.spawnAtLocation(ITEM_BY_DYE.get(this.getColor()), 1);
                if (var2 != null) {
                    var2.setDeltaMovement(
                        var2.getDeltaMovement()
                            .add(
                                (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
                                (double)(this.random.nextFloat() * 0.05F),
                                (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F)
                            )
                    );
                }
            }
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putBoolean("Sheared", this.isSheared());
        param0.putByte("Color", (byte)this.getColor().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setSheared(param0.getBoolean("Sheared"));
        this.setColor(DyeColor.byId(param0.getByte("Color")));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.SHEEP_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHEEP_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.SHEEP_STEP, 0.15F, 1.0F);
    }

    public DyeColor getColor() {
        return DyeColor.byId(this.entityData.get(DATA_WOOL_ID) & 15);
    }

    public void setColor(DyeColor param0) {
        byte var0 = this.entityData.get(DATA_WOOL_ID);
        this.entityData.set(DATA_WOOL_ID, (byte)(var0 & 240 | param0.getId() & 15));
    }

    public boolean isSheared() {
        return (this.entityData.get(DATA_WOOL_ID) & 16) != 0;
    }

    public void setSheared(boolean param0) {
        byte var0 = this.entityData.get(DATA_WOOL_ID);
        if (param0) {
            this.entityData.set(DATA_WOOL_ID, (byte)(var0 | 16));
        } else {
            this.entityData.set(DATA_WOOL_ID, (byte)(var0 & -17));
        }

    }

    public static DyeColor getRandomSheepColor(Random param0) {
        int var0 = param0.nextInt(100);
        if (var0 < 5) {
            return DyeColor.BLACK;
        } else if (var0 < 10) {
            return DyeColor.GRAY;
        } else if (var0 < 15) {
            return DyeColor.LIGHT_GRAY;
        } else if (var0 < 18) {
            return DyeColor.BROWN;
        } else {
            return param0.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE;
        }
    }

    public Sheep getBreedOffspring(AgableMob param0) {
        Sheep var0 = (Sheep)param0;
        Sheep var1 = EntityType.SHEEP.create(this.level);
        var1.setColor(this.getOffspringColor(this, var0));
        return var1;
    }

    @Override
    public void ate() {
        this.setSheared(false);
        if (this.isBaby()) {
            this.ageUp(60);
        }

    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.setColor(getRandomSheepColor(param0.getRandom()));
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    private DyeColor getOffspringColor(Animal param0, Animal param1) {
        DyeColor var0 = ((Sheep)param0).getColor();
        DyeColor var1 = ((Sheep)param1).getColor();
        CraftingContainer var2 = makeContainer(var0, var1);
        return this.level
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, var2, this.level)
            .map(param1x -> param1x.assemble(var2))
            .map(ItemStack::getItem)
            .filter(DyeItem.class::isInstance)
            .map(DyeItem.class::cast)
            .map(DyeItem::getDyeColor)
            .orElseGet(() -> this.level.random.nextBoolean() ? var0 : var1);
    }

    private static CraftingContainer makeContainer(DyeColor param0, DyeColor param1) {
        CraftingContainer var0 = new CraftingContainer(new AbstractContainerMenu(null, -1) {
            @Override
            public boolean stillValid(Player param0) {
                return false;
            }
        }, 2, 1);
        var0.setItem(0, new ItemStack(DyeItem.byColor(param0)));
        var0.setItem(1, new ItemStack(DyeItem.byColor(param1)));
        return var0;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return 0.95F * param1.height;
    }
}
