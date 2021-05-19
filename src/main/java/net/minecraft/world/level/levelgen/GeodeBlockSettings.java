package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class GeodeBlockSettings {
    public final BlockStateProvider fillingProvider;
    public final BlockStateProvider innerLayerProvider;
    public final BlockStateProvider alternateInnerLayerProvider;
    public final BlockStateProvider middleLayerProvider;
    public final BlockStateProvider outerLayerProvider;
    public final List<BlockState> innerPlacements;
    public final ResourceLocation cannotReplace;
    public static final Codec<GeodeBlockSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockStateProvider.CODEC.fieldOf("filling_provider").forGetter(param0x -> param0x.fillingProvider),
                    BlockStateProvider.CODEC.fieldOf("inner_layer_provider").forGetter(param0x -> param0x.innerLayerProvider),
                    BlockStateProvider.CODEC.fieldOf("alternate_inner_layer_provider").forGetter(param0x -> param0x.alternateInnerLayerProvider),
                    BlockStateProvider.CODEC.fieldOf("middle_layer_provider").forGetter(param0x -> param0x.middleLayerProvider),
                    BlockStateProvider.CODEC.fieldOf("outer_layer_provider").forGetter(param0x -> param0x.outerLayerProvider),
                    ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements").forGetter(param0x -> param0x.innerPlacements),
                    ResourceLocation.CODEC.fieldOf("cannot_replace").forGetter(param0x -> param0x.cannotReplace)
                )
                .apply(param0, GeodeBlockSettings::new)
    );

    public GeodeBlockSettings(
        BlockStateProvider param0,
        BlockStateProvider param1,
        BlockStateProvider param2,
        BlockStateProvider param3,
        BlockStateProvider param4,
        List<BlockState> param5,
        ResourceLocation param6
    ) {
        this.fillingProvider = param0;
        this.innerLayerProvider = param1;
        this.alternateInnerLayerProvider = param2;
        this.middleLayerProvider = param3;
        this.outerLayerProvider = param4;
        this.innerPlacements = param5;
        this.cannotReplace = param6;
    }
}
