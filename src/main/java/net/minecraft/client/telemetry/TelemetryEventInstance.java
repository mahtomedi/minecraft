package net.minecraft.client.telemetry;

import com.mojang.authlib.minecraft.TelemetryEvent;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.serialization.Codec;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record TelemetryEventInstance(TelemetryEventType type, TelemetryPropertyMap properties) {
    public static final Codec<TelemetryEventInstance> CODEC = TelemetryEventType.CODEC.dispatchStable(TelemetryEventInstance::type, TelemetryEventType::codec);

    public TelemetryEventInstance(TelemetryEventType param0, TelemetryPropertyMap param1) {
        param1.propertySet().forEach(param1x -> {
            if (!param0.contains(param1x)) {
                throw new IllegalArgumentException("Property '" + param1x.id() + "' not expected for event: '" + param0.id() + "'");
            }
        });
        this.type = param0;
        this.properties = param1;
    }

    public TelemetryEvent export(TelemetrySession param0) {
        return this.type.export(param0, this.properties);
    }
}
