package net.minecraft.client.telemetry;

import java.util.function.Consumer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface TelemetryEventSender {
    TelemetryEventSender DISABLED = (param0, param1) -> {
    };

    default TelemetryEventSender decorate(Consumer<TelemetryPropertyMap.Builder> param0) {
        return (param1, param2) -> this.send(param1, param2x -> {
                param2.accept(param2x);
                param0.accept(param2x);
            });
    }

    void send(TelemetryEventType var1, Consumer<TelemetryPropertyMap.Builder> var2);
}
