package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;

public class ReplaceBlockConfiguration implements FeatureConfiguration {
    public static final Codec<ReplaceBlockConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter(param0x -> param0x.targetStates))
                .apply(param0, ReplaceBlockConfiguration::new)
    );
    public final List<OreConfiguration.TargetBlockState> targetStates;

    public ReplaceBlockConfiguration(BlockState param0, BlockState param1) {
        this(ImmutableList.of(OreConfiguration.target(new BlockStateMatchTest(param0), param1)));
    }

    public ReplaceBlockConfiguration(List<OreConfiguration.TargetBlockState> param0) {
        this.targetStates = param0;
    }
}
