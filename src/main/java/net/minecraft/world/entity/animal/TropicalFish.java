package net.minecraft.world.entity.animal;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;

public class TropicalFish extends AbstractSchoolingFish {
    public static final String BUCKET_VARIANT_TAG = "BucketVariantTag";
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(TropicalFish.class, EntityDataSerializers.INT);
    public static final int BASE_SMALL = 0;
    public static final int BASE_LARGE = 1;
    private static final int BASES = 2;
    private static final ResourceLocation[] BASE_TEXTURE_LOCATIONS = new ResourceLocation[]{
        new ResourceLocation("textures/entity/fish/tropical_a.png"), new ResourceLocation("textures/entity/fish/tropical_b.png")
    };
    private static final ResourceLocation[] PATTERN_A_TEXTURE_LOCATIONS = new ResourceLocation[]{
        new ResourceLocation("textures/entity/fish/tropical_a_pattern_1.png"),
        new ResourceLocation("textures/entity/fish/tropical_a_pattern_2.png"),
        new ResourceLocation("textures/entity/fish/tropical_a_pattern_3.png"),
        new ResourceLocation("textures/entity/fish/tropical_a_pattern_4.png"),
        new ResourceLocation("textures/entity/fish/tropical_a_pattern_5.png"),
        new ResourceLocation("textures/entity/fish/tropical_a_pattern_6.png")
    };
    private static final ResourceLocation[] PATTERN_B_TEXTURE_LOCATIONS = new ResourceLocation[]{
        new ResourceLocation("textures/entity/fish/tropical_b_pattern_1.png"),
        new ResourceLocation("textures/entity/fish/tropical_b_pattern_2.png"),
        new ResourceLocation("textures/entity/fish/tropical_b_pattern_3.png"),
        new ResourceLocation("textures/entity/fish/tropical_b_pattern_4.png"),
        new ResourceLocation("textures/entity/fish/tropical_b_pattern_5.png"),
        new ResourceLocation("textures/entity/fish/tropical_b_pattern_6.png")
    };
    private static final int PATTERNS = 6;
    private static final int COLORS = 15;
    public static final int[] COMMON_VARIANTS = new int[]{
        calculateVariant(TropicalFish.Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY),
        calculateVariant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY),
        calculateVariant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE),
        calculateVariant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY),
        calculateVariant(TropicalFish.Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY),
        calculateVariant(TropicalFish.Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE),
        calculateVariant(TropicalFish.Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE),
        calculateVariant(TropicalFish.Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW),
        calculateVariant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED),
        calculateVariant(TropicalFish.Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW),
        calculateVariant(TropicalFish.Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY),
        calculateVariant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE),
        calculateVariant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK),
        calculateVariant(TropicalFish.Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE),
        calculateVariant(TropicalFish.Pattern.BETTY, DyeColor.RED, DyeColor.WHITE),
        calculateVariant(TropicalFish.Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED),
        calculateVariant(TropicalFish.Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE),
        calculateVariant(TropicalFish.Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW),
        calculateVariant(TropicalFish.Pattern.KOB, DyeColor.RED, DyeColor.WHITE),
        calculateVariant(TropicalFish.Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE),
        calculateVariant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW),
        calculateVariant(TropicalFish.Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW)
    };
    private boolean isSchool = true;

    private static int calculateVariant(TropicalFish.Pattern param0, DyeColor param1, DyeColor param2) {
        return param0.getBase() & 0xFF | (param0.getIndex() & 0xFF) << 8 | (param1.getId() & 0xFF) << 16 | (param2.getId() & 0xFF) << 24;
    }

    public TropicalFish(EntityType<? extends TropicalFish> param0, Level param1) {
        super(param0, param1);
    }

    public static String getPredefinedName(int param0) {
        return "entity.minecraft.tropical_fish.predefined." + param0;
    }

    public static DyeColor getBaseColor(int param0) {
        return DyeColor.byId(getBaseColorIdx(param0));
    }

    public static DyeColor getPatternColor(int param0) {
        return DyeColor.byId(getPatternColorIdx(param0));
    }

    public static String getFishTypeName(int param0) {
        int var0 = getBaseVariant(param0);
        int var1 = getPatternVariant(param0);
        return "entity.minecraft.tropical_fish.type." + TropicalFish.Pattern.getPatternName(var0, var1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Variant", this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setVariant(param0.getInt("Variant"));
    }

    public void setVariant(int param0) {
        this.entityData.set(DATA_ID_TYPE_VARIANT, param0);
    }

    @Override
    public boolean isMaxGroupSizeReached(int param0) {
        return !this.isSchool;
    }

    public int getVariant() {
        return this.entityData.get(DATA_ID_TYPE_VARIANT);
    }

    @Override
    public void saveToBucketTag(ItemStack param0) {
        super.saveToBucketTag(param0);
        CompoundTag var0 = param0.getOrCreateTag();
        var0.putInt("BucketVariantTag", this.getVariant());
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.TROPICAL_FISH_BUCKET);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.TROPICAL_FISH_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.TROPICAL_FISH_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.TROPICAL_FISH_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.TROPICAL_FISH_FLOP;
    }

    private static int getBaseColorIdx(int param0) {
        return (param0 & 0xFF0000) >> 16;
    }

    public float[] getBaseColor() {
        return DyeColor.byId(getBaseColorIdx(this.getVariant())).getTextureDiffuseColors();
    }

    private static int getPatternColorIdx(int param0) {
        return (param0 & 0xFF000000) >> 24;
    }

    public float[] getPatternColor() {
        return DyeColor.byId(getPatternColorIdx(this.getVariant())).getTextureDiffuseColors();
    }

    public static int getBaseVariant(int param0) {
        return Math.min(param0 & 0xFF, 1);
    }

    public int getBaseVariant() {
        return getBaseVariant(this.getVariant());
    }

    private static int getPatternVariant(int param0) {
        return Math.min((param0 & 0xFF00) >> 8, 5);
    }

    public ResourceLocation getPatternTextureLocation() {
        return getBaseVariant(this.getVariant()) == 0
            ? PATTERN_A_TEXTURE_LOCATIONS[getPatternVariant(this.getVariant())]
            : PATTERN_B_TEXTURE_LOCATIONS[getPatternVariant(this.getVariant())];
    }

    public ResourceLocation getBaseTextureLocation() {
        return BASE_TEXTURE_LOCATIONS[getBaseVariant(this.getVariant())];
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        param3 = super.finalizeSpawn(param0, param1, param2, param3, param4);
        if (param2 == MobSpawnType.BUCKET && param4 != null && param4.contains("BucketVariantTag", 3)) {
            this.setVariant(param4.getInt("BucketVariantTag"));
            return param3;
        } else {
            int var1;
            int var2;
            int var3;
            int var4;
            if (param3 instanceof TropicalFish.TropicalFishGroupData var0) {
                var1 = var0.base;
                var2 = var0.pattern;
                var3 = var0.baseColor;
                var4 = var0.patternColor;
            } else if ((double)this.random.nextFloat() < 0.9) {
                int var5 = Util.getRandom(COMMON_VARIANTS, this.random);
                var1 = var5 & 0xFF;
                var2 = (var5 & 0xFF00) >> 8;
                var3 = (var5 & 0xFF0000) >> 16;
                var4 = (var5 & 0xFF000000) >> 24;
                param3 = new TropicalFish.TropicalFishGroupData(this, var1, var2, var3, var4);
            } else {
                this.isSchool = false;
                var1 = this.random.nextInt(2);
                var2 = this.random.nextInt(6);
                var3 = this.random.nextInt(15);
                var4 = this.random.nextInt(15);
            }

            this.setVariant(var1 | var2 << 8 | var3 << 16 | var4 << 24);
            return param3;
        }
    }

    public static boolean checkTropicalFishSpawnRules(
        EntityType<TropicalFish> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4
    ) {
        return param1.getBlockState(param3).is(Blocks.WATER)
            && (
                Objects.equals(param1.getBiomeName(param3), Optional.of(Biomes.LUSH_CAVES))
                    || WaterAnimal.checkSurfaceWaterAnimalSpawnRules(param0, param1, param2, param3, param4)
            );
    }

    static enum Pattern {
        KOB(0, 0),
        SUNSTREAK(0, 1),
        SNOOPER(0, 2),
        DASHER(0, 3),
        BRINELY(0, 4),
        SPOTTY(0, 5),
        FLOPPER(1, 0),
        STRIPEY(1, 1),
        GLITTER(1, 2),
        BLOCKFISH(1, 3),
        BETTY(1, 4),
        CLAYFISH(1, 5);

        private final int base;
        private final int index;
        private static final TropicalFish.Pattern[] VALUES = values();

        private Pattern(int param0, int param1) {
            this.base = param0;
            this.index = param1;
        }

        public int getBase() {
            return this.base;
        }

        public int getIndex() {
            return this.index;
        }

        public static String getPatternName(int param0, int param1) {
            return VALUES[param1 + 6 * param0].getName();
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    static class TropicalFishGroupData extends AbstractSchoolingFish.SchoolSpawnGroupData {
        final int base;
        final int pattern;
        final int baseColor;
        final int patternColor;

        TropicalFishGroupData(TropicalFish param0, int param1, int param2, int param3, int param4) {
            super(param0);
            this.base = param1;
            this.pattern = param2;
            this.baseColor = param3;
            this.patternColor = param4;
        }
    }
}
