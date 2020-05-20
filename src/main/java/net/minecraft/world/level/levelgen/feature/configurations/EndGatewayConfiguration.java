package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;

public class EndGatewayConfiguration implements FeatureConfiguration {
    public static final Codec<EndGatewayConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockPos.CODEC.optionalFieldOf("exit").forGetter(param0x -> param0x.exit), Codec.BOOL.fieldOf("exact").forGetter(param0x -> param0x.exact)
                )
                .apply(param0, EndGatewayConfiguration::new)
    );
    private final Optional<BlockPos> exit;
    private final boolean exact;

    private EndGatewayConfiguration(Optional<BlockPos> param0, boolean param1) {
        this.exit = param0;
        this.exact = param1;
    }

    public static EndGatewayConfiguration knownExit(BlockPos param0, boolean param1) {
        return new EndGatewayConfiguration(Optional.of(param0), param1);
    }

    public static EndGatewayConfiguration delayedExitSearch() {
        return new EndGatewayConfiguration(Optional.empty(), false);
    }

    public Optional<BlockPos> getExit() {
        return this.exit;
    }

    public boolean isExitExact() {
        return this.exact;
    }
}
