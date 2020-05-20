package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;

public class DiskConfiguration implements FeatureConfiguration {
    public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockState.CODEC.fieldOf("state").forGetter(param0x -> param0x.state),
                    Codec.INT.fieldOf("radius").withDefault(0).forGetter(param0x -> param0x.radius),
                    Codec.INT.fieldOf("y_size").withDefault(0).forGetter(param0x -> param0x.ySize),
                    BlockState.CODEC.listOf().fieldOf("targets").forGetter(param0x -> param0x.targets)
                )
                .apply(param0, DiskConfiguration::new)
    );
    public final BlockState state;
    public final int radius;
    public final int ySize;
    public final List<BlockState> targets;

    public DiskConfiguration(BlockState param0, int param1, int param2, List<BlockState> param3) {
        this.state = param0;
        this.radius = param1;
        this.ySize = param2;
        this.targets = param3;
    }
}
