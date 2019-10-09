package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Optional;
import net.minecraft.core.BlockPos;

public class EndGatewayConfiguration implements FeatureConfiguration {
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

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            this.exit
                .<T>map(
                    param1 -> param0.createMap(
                            ImmutableMap.of(
                                param0.createString("exit_x"),
                                param0.createInt(param1.getX()),
                                param0.createString("exit_y"),
                                param0.createInt(param1.getY()),
                                param0.createString("exit_z"),
                                param0.createInt(param1.getZ()),
                                param0.createString("exact"),
                                param0.createBoolean(this.exact)
                            )
                        )
                )
                .orElse(param0.emptyMap())
        );
    }

    public static <T> EndGatewayConfiguration deserialize(Dynamic<T> param0) {
        Optional<BlockPos> var0 = param0.get("exit_x")
            .asNumber()
            .flatMap(
                param1 -> param0.get("exit_y")
                        .asNumber()
                        .flatMap(
                            param2 -> param0.get("exit_z").asNumber().map(param2x -> new BlockPos(param1.intValue(), param2.intValue(), param2x.intValue()))
                        )
            );
        boolean var1 = param0.get("exact").asBoolean(false);
        return new EndGatewayConfiguration(var0, var1);
    }
}
