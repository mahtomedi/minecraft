package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class NetherForestVegetationConfig extends BlockPileConfiguration {
    public static final Codec<NetherForestVegetationConfig> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(param0x -> param0x.stateProvider),
                    ExtraCodecs.POSITIVE_INT.fieldOf("spread_width").forGetter(param0x -> param0x.spreadWidth),
                    ExtraCodecs.POSITIVE_INT.fieldOf("spread_height").forGetter(param0x -> param0x.spreadHeight)
                )
                .apply(param0, NetherForestVegetationConfig::new)
    );
    public final int spreadWidth;
    public final int spreadHeight;

    public NetherForestVegetationConfig(BlockStateProvider param0, int param1, int param2) {
        super(param0);
        this.spreadWidth = param1;
        this.spreadHeight = param2;
    }
}
