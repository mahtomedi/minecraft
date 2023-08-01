package net.minecraft.data;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class BlockFamily {
    private final Block baseBlock;
    final Map<BlockFamily.Variant, Block> variants = Maps.newHashMap();
    FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
    boolean generateModel = true;
    boolean generateRecipe = true;
    @Nullable
    String recipeGroupPrefix;
    @Nullable
    String recipeUnlockedBy;

    BlockFamily(Block param0) {
        this.baseBlock = param0;
    }

    public Block getBaseBlock() {
        return this.baseBlock;
    }

    public Map<BlockFamily.Variant, Block> getVariants() {
        return this.variants;
    }

    public Block get(BlockFamily.Variant param0) {
        return this.variants.get(param0);
    }

    public boolean shouldGenerateModel() {
        return this.generateModel;
    }

    public boolean shouldGenerateRecipe(FeatureFlagSet param0) {
        return this.generateRecipe && this.requiredFeatures.isSubsetOf(param0);
    }

    public Optional<String> getRecipeGroupPrefix() {
        return Util.isBlank(this.recipeGroupPrefix) ? Optional.empty() : Optional.of(this.recipeGroupPrefix);
    }

    public Optional<String> getRecipeUnlockedBy() {
        return Util.isBlank(this.recipeUnlockedBy) ? Optional.empty() : Optional.of(this.recipeUnlockedBy);
    }

    public static class Builder {
        private final BlockFamily family;

        public Builder(Block param0) {
            this.family = new BlockFamily(param0);
        }

        public BlockFamily getFamily() {
            return this.family;
        }

        public BlockFamily.Builder button(Block param0) {
            this.family.variants.put(BlockFamily.Variant.BUTTON, param0);
            return this;
        }

        public BlockFamily.Builder chiseled(Block param0) {
            this.family.variants.put(BlockFamily.Variant.CHISELED, param0);
            return this;
        }

        public BlockFamily.Builder mosaic(Block param0) {
            this.family.variants.put(BlockFamily.Variant.MOSAIC, param0);
            return this;
        }

        public BlockFamily.Builder cracked(Block param0) {
            this.family.variants.put(BlockFamily.Variant.CRACKED, param0);
            return this;
        }

        public BlockFamily.Builder cut(Block param0) {
            this.family.variants.put(BlockFamily.Variant.CUT, param0);
            return this;
        }

        public BlockFamily.Builder door(Block param0) {
            this.family.variants.put(BlockFamily.Variant.DOOR, param0);
            return this;
        }

        public BlockFamily.Builder customFence(Block param0) {
            this.family.variants.put(BlockFamily.Variant.CUSTOM_FENCE, param0);
            return this;
        }

        public BlockFamily.Builder fence(Block param0) {
            this.family.variants.put(BlockFamily.Variant.FENCE, param0);
            return this;
        }

        public BlockFamily.Builder customFenceGate(Block param0) {
            this.family.variants.put(BlockFamily.Variant.CUSTOM_FENCE_GATE, param0);
            return this;
        }

        public BlockFamily.Builder fenceGate(Block param0) {
            this.family.variants.put(BlockFamily.Variant.FENCE_GATE, param0);
            return this;
        }

        public BlockFamily.Builder sign(Block param0, Block param1) {
            this.family.variants.put(BlockFamily.Variant.SIGN, param0);
            this.family.variants.put(BlockFamily.Variant.WALL_SIGN, param1);
            return this;
        }

        public BlockFamily.Builder slab(Block param0) {
            this.family.variants.put(BlockFamily.Variant.SLAB, param0);
            return this;
        }

        public BlockFamily.Builder stairs(Block param0) {
            this.family.variants.put(BlockFamily.Variant.STAIRS, param0);
            return this;
        }

        public BlockFamily.Builder pressurePlate(Block param0) {
            this.family.variants.put(BlockFamily.Variant.PRESSURE_PLATE, param0);
            return this;
        }

        public BlockFamily.Builder polished(Block param0) {
            this.family.variants.put(BlockFamily.Variant.POLISHED, param0);
            return this;
        }

        public BlockFamily.Builder trapdoor(Block param0) {
            this.family.variants.put(BlockFamily.Variant.TRAPDOOR, param0);
            return this;
        }

        public BlockFamily.Builder wall(Block param0) {
            this.family.variants.put(BlockFamily.Variant.WALL, param0);
            return this;
        }

        public BlockFamily.Builder dontGenerateModel() {
            this.family.generateModel = false;
            return this;
        }

        public BlockFamily.Builder dontGenerateRecipe() {
            this.family.generateRecipe = false;
            return this;
        }

        public BlockFamily.Builder featureLockedBehind(FeatureFlag... param0) {
            this.family.requiredFeatures = FeatureFlags.REGISTRY.subset(param0);
            return this;
        }

        public BlockFamily.Builder recipeGroupPrefix(String param0) {
            this.family.recipeGroupPrefix = param0;
            return this;
        }

        public BlockFamily.Builder recipeUnlockedBy(String param0) {
            this.family.recipeUnlockedBy = param0;
            return this;
        }
    }

    public static enum Variant {
        BUTTON("button"),
        CHISELED("chiseled"),
        CRACKED("cracked"),
        CUT("cut"),
        DOOR("door"),
        CUSTOM_FENCE("fence"),
        FENCE("fence"),
        CUSTOM_FENCE_GATE("fence_gate"),
        FENCE_GATE("fence_gate"),
        MOSAIC("mosaic"),
        SIGN("sign"),
        SLAB("slab"),
        STAIRS("stairs"),
        PRESSURE_PLATE("pressure_plate"),
        POLISHED("polished"),
        TRAPDOOR("trapdoor"),
        WALL("wall"),
        WALL_SIGN("wall_sign");

        private final String recipeGroup;

        private Variant(String param0) {
            this.recipeGroup = param0;
        }

        public String getRecipeGroup() {
            return this.recipeGroup;
        }
    }
}
