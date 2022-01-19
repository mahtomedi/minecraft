package net.minecraft.server.dedicated;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import org.slf4j.Logger;

public abstract class Settings<T extends Settings<T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final Properties properties;

    public Settings(Properties param0) {
        this.properties = param0;
    }

    public static Properties loadFromFile(Path param0) {
        Properties var0 = new Properties();

        try (InputStream var1 = Files.newInputStream(param0)) {
            var0.load(var1);
        } catch (IOException var7) {
            LOGGER.error("Failed to load properties from file: {}", param0);
        }

        return var0;
    }

    public void store(Path param0) {
        try (OutputStream var0 = Files.newOutputStream(param0)) {
            this.properties.store(var0, "Minecraft server properties");
        } catch (IOException var7) {
            LOGGER.error("Failed to store properties to file: {}", param0);
        }

    }

    private static <V extends Number> Function<String, V> wrapNumberDeserializer(Function<String, V> param0) {
        return param1 -> {
            try {
                return param0.apply(param1);
            } catch (NumberFormatException var3) {
                return null;
            }
        };
    }

    protected static <V> Function<String, V> dispatchNumberOrString(IntFunction<V> param0, Function<String, V> param1) {
        return param2 -> {
            try {
                return param0.apply(Integer.parseInt(param2));
            } catch (NumberFormatException var4) {
                return param1.apply(param2);
            }
        };
    }

    @Nullable
    private String getStringRaw(String param0) {
        return (String)this.properties.get(param0);
    }

    @Nullable
    protected <V> V getLegacy(String param0, Function<String, V> param1) {
        String var0 = this.getStringRaw(param0);
        if (var0 == null) {
            return null;
        } else {
            this.properties.remove(param0);
            return param1.apply(var0);
        }
    }

    protected <V> V get(String param0, Function<String, V> param1, Function<V, String> param2, V param3) {
        String var0 = this.getStringRaw(param0);
        V var1 = MoreObjects.firstNonNull(var0 != null ? param1.apply(var0) : null, param3);
        this.properties.put(param0, param2.apply(var1));
        return var1;
    }

    protected <V> Settings<T>.MutableValue<V> getMutable(String param0, Function<String, V> param1, Function<V, String> param2, V param3) {
        String var0 = this.getStringRaw(param0);
        V var1 = MoreObjects.firstNonNull(var0 != null ? param1.apply(var0) : null, param3);
        this.properties.put(param0, param2.apply(var1));
        return new Settings.MutableValue<>(param0, var1, param2);
    }

    protected <V> V get(String param0, Function<String, V> param1, UnaryOperator<V> param2, Function<V, String> param3, V param4) {
        return this.get(param0, param2x -> {
            V var0 = param1.apply(param2x);
            return var0 != null ? param2.apply(var0) : null;
        }, param3, param4);
    }

    protected <V> V get(String param0, Function<String, V> param1, V param2) {
        return this.get(param0, param1, Objects::toString, param2);
    }

    protected <V> Settings<T>.MutableValue<V> getMutable(String param0, Function<String, V> param1, V param2) {
        return this.getMutable(param0, param1, Objects::toString, param2);
    }

    protected String get(String param0, String param1) {
        return this.get(param0, Function.identity(), Function.identity(), param1);
    }

    @Nullable
    protected String getLegacyString(String param0) {
        return this.getLegacy(param0, Function.identity());
    }

    protected int get(String param0, int param1) {
        return this.get(param0, wrapNumberDeserializer(Integer::parseInt), Integer.valueOf(param1));
    }

    protected Settings<T>.MutableValue<Integer> getMutable(String param0, int param1) {
        return this.getMutable(param0, wrapNumberDeserializer(Integer::parseInt), param1);
    }

    protected int get(String param0, UnaryOperator<Integer> param1, int param2) {
        return this.get(param0, wrapNumberDeserializer(Integer::parseInt), param1, Objects::toString, param2);
    }

    protected long get(String param0, long param1) {
        return this.get(param0, wrapNumberDeserializer(Long::parseLong), param1);
    }

    protected boolean get(String param0, boolean param1) {
        return this.get(param0, Boolean::valueOf, param1);
    }

    protected Settings<T>.MutableValue<Boolean> getMutable(String param0, boolean param1) {
        return this.getMutable(param0, Boolean::valueOf, param1);
    }

    @Nullable
    protected Boolean getLegacyBoolean(String param0) {
        return this.getLegacy(param0, Boolean::valueOf);
    }

    protected Properties cloneProperties() {
        Properties var0 = new Properties();
        var0.putAll(this.properties);
        return var0;
    }

    protected abstract T reload(RegistryAccess var1, Properties var2);

    public class MutableValue<V> implements Supplier<V> {
        private final String key;
        private final V value;
        private final Function<V, String> serializer;

        MutableValue(String param1, V param2, Function<V, String> param3) {
            this.key = param1;
            this.value = param2;
            this.serializer = param3;
        }

        @Override
        public V get() {
            return this.value;
        }

        public T update(RegistryAccess param0, V param1) {
            Properties var0 = Settings.this.cloneProperties();
            var0.put(this.key, this.serializer.apply(param1));
            return Settings.this.reload(param0, var0);
        }
    }
}
