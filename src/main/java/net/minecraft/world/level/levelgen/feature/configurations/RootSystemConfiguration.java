package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RootSystemConfiguration implements FeatureConfiguration {
    public static final Codec<RootSystemConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    PlacedFeature.CODEC.fieldOf("feature").forGetter(param0x -> param0x.treeFeature),
                    Codec.intRange(1, 64).fieldOf("required_vertical_space_for_tree").forGetter(param0x -> param0x.requiredVerticalSpaceForTree),
                    Codec.intRange(1, 64).fieldOf("root_radius").forGetter(param0x -> param0x.rootRadius),
                    TagKey.hashedCodec(Registries.BLOCK).fieldOf("root_replaceable").forGetter(param0x -> param0x.rootReplaceable),
                    BlockStateProvider.CODEC.fieldOf("root_state_provider").forGetter(param0x -> param0x.rootStateProvider),
                    Codec.intRange(1, 256).fieldOf("root_placement_attempts").forGetter(param0x -> param0x.rootPlacementAttempts),
                    Codec.intRange(1, 4096).fieldOf("root_column_max_height").forGetter(param0x -> param0x.rootColumnMaxHeight),
                    Codec.intRange(1, 64).fieldOf("hanging_root_radius").forGetter(param0x -> param0x.hangingRootRadius),
                    Codec.intRange(0, 16).fieldOf("hanging_roots_vertical_span").forGetter(param0x -> param0x.hangingRootsVerticalSpan),
                    BlockStateProvider.CODEC.fieldOf("hanging_root_state_provider").forGetter(param0x -> param0x.hangingRootStateProvider),
                    Codec.intRange(1, 256).fieldOf("hanging_root_placement_attempts").forGetter(param0x -> param0x.hangingRootPlacementAttempts),
                    Codec.intRange(1, 64).fieldOf("allowed_vertical_water_for_tree").forGetter(param0x -> param0x.allowedVerticalWaterForTree),
                    BlockPredicate.CODEC.fieldOf("allowed_tree_position").forGetter(param0x -> param0x.allowedTreePosition)
                )
                .apply(param0, RootSystemConfiguration::new)
    );
    public final Holder<PlacedFeature> treeFeature;
    public final int requiredVerticalSpaceForTree;
    public final int rootRadius;
    public final TagKey<Block> rootReplaceable;
    public final BlockStateProvider rootStateProvider;
    public final int rootPlacementAttempts;
    public final int rootColumnMaxHeight;
    public final int hangingRootRadius;
    public final int hangingRootsVerticalSpan;
    public final BlockStateProvider hangingRootStateProvider;
    public final int hangingRootPlacementAttempts;
    public final int allowedVerticalWaterForTree;
    public final BlockPredicate allowedTreePosition;

    public RootSystemConfiguration(
        Holder<PlacedFeature> param0,
        int param1,
        int param2,
        TagKey<Block> param3,
        BlockStateProvider param4,
        int param5,
        int param6,
        int param7,
        int param8,
        BlockStateProvider param9,
        int param10,
        int param11,
        BlockPredicate param12
    ) {
        this.treeFeature = param0;
        this.requiredVerticalSpaceForTree = param1;
        this.rootRadius = param2;
        this.rootReplaceable = param3;
        this.rootStateProvider = param4;
        this.rootPlacementAttempts = param5;
        this.rootColumnMaxHeight = param6;
        this.hangingRootRadius = param7;
        this.hangingRootsVerticalSpan = param8;
        this.hangingRootStateProvider = param9;
        this.hangingRootPlacementAttempts = param10;
        this.allowedVerticalWaterForTree = param11;
        this.allowedTreePosition = param12;
    }
}
