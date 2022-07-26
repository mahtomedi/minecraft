package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VegetationPatchConfiguration implements FeatureConfiguration {
    public static final Codec<VegetationPatchConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    TagKey.hashedCodec(Registries.BLOCK).fieldOf("replaceable").forGetter(param0x -> param0x.replaceable),
                    BlockStateProvider.CODEC.fieldOf("ground_state").forGetter(param0x -> param0x.groundState),
                    PlacedFeature.CODEC.fieldOf("vegetation_feature").forGetter(param0x -> param0x.vegetationFeature),
                    CaveSurface.CODEC.fieldOf("surface").forGetter(param0x -> param0x.surface),
                    IntProvider.codec(1, 128).fieldOf("depth").forGetter(param0x -> param0x.depth),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("extra_bottom_block_chance").forGetter(param0x -> param0x.extraBottomBlockChance),
                    Codec.intRange(1, 256).fieldOf("vertical_range").forGetter(param0x -> param0x.verticalRange),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("vegetation_chance").forGetter(param0x -> param0x.vegetationChance),
                    IntProvider.CODEC.fieldOf("xz_radius").forGetter(param0x -> param0x.xzRadius),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("extra_edge_column_chance").forGetter(param0x -> param0x.extraEdgeColumnChance)
                )
                .apply(param0, VegetationPatchConfiguration::new)
    );
    public final TagKey<Block> replaceable;
    public final BlockStateProvider groundState;
    public final Holder<PlacedFeature> vegetationFeature;
    public final CaveSurface surface;
    public final IntProvider depth;
    public final float extraBottomBlockChance;
    public final int verticalRange;
    public final float vegetationChance;
    public final IntProvider xzRadius;
    public final float extraEdgeColumnChance;

    public VegetationPatchConfiguration(
        TagKey<Block> param0,
        BlockStateProvider param1,
        Holder<PlacedFeature> param2,
        CaveSurface param3,
        IntProvider param4,
        float param5,
        int param6,
        float param7,
        IntProvider param8,
        float param9
    ) {
        this.replaceable = param0;
        this.groundState = param1;
        this.vegetationFeature = param2;
        this.surface = param3;
        this.depth = param4;
        this.extraBottomBlockChance = param5;
        this.verticalRange = param6;
        this.vegetationChance = param7;
        this.xzRadius = param8;
        this.extraEdgeColumnChance = param9;
    }
}
