package net.minecraft.client.telemetry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TelemetryPropertyMap {
    final Map<TelemetryProperty<?>, Object> entries;

    TelemetryPropertyMap(Map<TelemetryProperty<?>, Object> param0) {
        this.entries = param0;
    }

    public static TelemetryPropertyMap.Builder builder() {
        return new TelemetryPropertyMap.Builder();
    }

    public static Codec<TelemetryPropertyMap> createCodec(final List<TelemetryProperty<?>> param0) {
        return (new MapCodec<TelemetryPropertyMap>() {
                public <T> RecordBuilder<T> encode(TelemetryPropertyMap param0x, DynamicOps<T> param1, RecordBuilder<T> param2) {
                    RecordBuilder<T> var0 = param2;
    
                    for(TelemetryProperty<?> var1 : param0) {
                        var0 = this.encodeProperty(param0, var0, var1);
                    }
    
                    return var0;
                }
    
                private <T, V> RecordBuilder<T> encodeProperty(TelemetryPropertyMap param0x, RecordBuilder<T> param1, TelemetryProperty<V> param2) {
                    V var0 = param0.get(param2);
                    return var0 != null ? param1.add(param2.id(), var0, param2.codec()) : param1;
                }
    
                @Override
                public <T> DataResult<TelemetryPropertyMap> decode(DynamicOps<T> param0x, MapLike<T> param1) {
                    DataResult<TelemetryPropertyMap.Builder> var0 = DataResult.success(new TelemetryPropertyMap.Builder());
    
                    for(TelemetryProperty<?> var1 : param0) {
                        var0 = this.decodeProperty(var0, param0, param1, var1);
                    }
    
                    return var0.map(TelemetryPropertyMap.Builder::build);
                }
    
                private <T, V> DataResult<TelemetryPropertyMap.Builder> decodeProperty(
                    DataResult<TelemetryPropertyMap.Builder> param0x, DynamicOps<T> param1, MapLike<T> param2, TelemetryProperty<V> param3
                ) {
                    T var0 = param2.get(param3.id());
                    if (var0 != null) {
                        DataResult<V> var1 = param3.codec().parse(param1, var0);
                        return param0.apply2stable((param1x, param2x) -> param1x.put(param3, (T)param2x), var1);
                    } else {
                        return param0;
                    }
                }
    
                @Override
                public <T> Stream<T> keys(DynamicOps<T> param0x) {
                    return param0.stream().map(TelemetryProperty::id).map(param0::createString);
                }
            })
            .codec();
    }

    @Nullable
    public <T> T get(TelemetryProperty<T> param0) {
        return (T)this.entries.get(param0);
    }

    @Override
    public String toString() {
        return this.entries.toString();
    }

    public Set<TelemetryProperty<?>> propertySet() {
        return this.entries.keySet();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Map<TelemetryProperty<?>, Object> entries = new Reference2ObjectOpenHashMap<>();

        Builder() {
        }

        public <T> TelemetryPropertyMap.Builder put(TelemetryProperty<T> param0, T param1) {
            this.entries.put(param0, param1);
            return this;
        }

        public TelemetryPropertyMap.Builder putAll(TelemetryPropertyMap param0) {
            this.entries.putAll(param0.entries);
            return this;
        }

        public TelemetryPropertyMap build() {
            return new TelemetryPropertyMap(this.entries);
        }
    }
}
