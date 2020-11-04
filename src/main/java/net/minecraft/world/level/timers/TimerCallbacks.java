package net.minecraft.world.level.timers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimerCallbacks<C> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final TimerCallbacks<MinecraftServer> SERVER_CALLBACKS = new TimerCallbacks<MinecraftServer>()
        .register(new FunctionCallback.Serializer())
        .register(new FunctionTagCallback.Serializer());
    private final Map<ResourceLocation, TimerCallback.Serializer<C, ?>> idToSerializer = Maps.newHashMap();
    private final Map<Class<?>, TimerCallback.Serializer<C, ?>> classToSerializer = Maps.newHashMap();

    public TimerCallbacks<C> register(TimerCallback.Serializer<C, ?> param0) {
        this.idToSerializer.put(param0.getId(), param0);
        this.classToSerializer.put(param0.getCls(), param0);
        return this;
    }

    private <T extends TimerCallback<C>> TimerCallback.Serializer<C, T> getSerializer(Class<?> param0) {
        return (TimerCallback.Serializer<C, T>)this.classToSerializer.get(param0);
    }

    public <T extends TimerCallback<C>> CompoundTag serialize(T param0) {
        TimerCallback.Serializer<C, T> var0 = this.getSerializer(param0.getClass());
        CompoundTag var1 = new CompoundTag();
        var0.serialize(var1, param0);
        var1.putString("Type", var0.getId().toString());
        return var1;
    }

    @Nullable
    public TimerCallback<C> deserialize(CompoundTag param0) {
        ResourceLocation var0 = ResourceLocation.tryParse(param0.getString("Type"));
        TimerCallback.Serializer<C, ?> var1 = this.idToSerializer.get(var0);
        if (var1 == null) {
            LOGGER.error("Failed to deserialize timer callback: {}", param0);
            return null;
        } else {
            try {
                return var1.deserialize(param0);
            } catch (Exception var5) {
                LOGGER.error("Failed to deserialize timer callback: {}", param0, var5);
                return null;
            }
        }
    }
}
