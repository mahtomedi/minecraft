package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractUniversalBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class JavaOps implements DynamicOps<Object> {
    public static final JavaOps INSTANCE = new JavaOps();

    private JavaOps() {
    }

    @Override
    public Object empty() {
        return null;
    }

    @Override
    public Object emptyMap() {
        return Map.of();
    }

    @Override
    public Object emptyList() {
        return List.of();
    }

    @Override
    public <U> U convertTo(DynamicOps<U> param0, Object param1) {
        if (param1 == null) {
            return param0.empty();
        } else if (param1 instanceof Map) {
            return this.convertMap(param0, param1);
        } else if (param1 instanceof ByteList var0) {
            return param0.createByteList(ByteBuffer.wrap(var0.toByteArray()));
        } else if (param1 instanceof IntList var1) {
            return param0.createIntList(var1.intStream());
        } else if (param1 instanceof LongList var2) {
            return param0.createLongList(var2.longStream());
        } else if (param1 instanceof List) {
            return this.convertList(param0, param1);
        } else if (param1 instanceof String var3) {
            return param0.createString(var3);
        } else if (param1 instanceof Boolean var4) {
            return param0.createBoolean(var4);
        } else if (param1 instanceof Byte var5) {
            return param0.createByte(var5);
        } else if (param1 instanceof Short var6) {
            return param0.createShort(var6);
        } else if (param1 instanceof Integer var7) {
            return param0.createInt(var7);
        } else if (param1 instanceof Long var8) {
            return param0.createLong(var8);
        } else if (param1 instanceof Float var9) {
            return param0.createFloat(var9);
        } else if (param1 instanceof Double var10) {
            return param0.createDouble(var10);
        } else if (param1 instanceof Number var11) {
            return param0.createNumeric(var11);
        } else {
            throw new IllegalStateException("Don't know how to convert " + param1);
        }
    }

    @Override
    public DataResult<Number> getNumberValue(Object param0) {
        return param0 instanceof Number var0 ? DataResult.success(var0) : DataResult.error(() -> "Not a number: " + param0);
    }

    @Override
    public Object createNumeric(Number param0) {
        return param0;
    }

    @Override
    public Object createByte(byte param0) {
        return param0;
    }

    @Override
    public Object createShort(short param0) {
        return param0;
    }

    @Override
    public Object createInt(int param0) {
        return param0;
    }

    @Override
    public Object createLong(long param0) {
        return param0;
    }

    @Override
    public Object createFloat(float param0) {
        return param0;
    }

    @Override
    public Object createDouble(double param0) {
        return param0;
    }

    @Override
    public DataResult<Boolean> getBooleanValue(Object param0) {
        return param0 instanceof Boolean var0 ? DataResult.success(var0) : DataResult.error(() -> "Not a boolean: " + param0);
    }

    @Override
    public Object createBoolean(boolean param0) {
        return param0;
    }

    @Override
    public DataResult<String> getStringValue(Object param0) {
        return param0 instanceof String var0 ? DataResult.success(var0) : DataResult.error(() -> "Not a string: " + param0);
    }

    @Override
    public Object createString(String param0) {
        return param0;
    }

    @Override
    public DataResult<Object> mergeToList(Object param0, Object param1) {
        if (param0 == this.empty()) {
            return DataResult.success(List.of(param1));
        } else if (param0 instanceof List var0) {
            return var0.isEmpty() ? DataResult.success(List.of(param1)) : DataResult.success(ImmutableList.builder().addAll(var0).add(param1).build());
        } else {
            return DataResult.error(() -> "Not a list: " + param0);
        }
    }

    @Override
    public DataResult<Object> mergeToList(Object param0, List<Object> param1) {
        if (param0 == this.empty()) {
            return DataResult.success(param1);
        } else if (param0 instanceof List var0) {
            return var0.isEmpty() ? DataResult.success(param1) : DataResult.success(ImmutableList.builder().addAll(var0).addAll(param1).build());
        } else {
            return DataResult.error(() -> "Not a list: " + param0);
        }
    }

    @Override
    public DataResult<Object> mergeToMap(Object param0, Object param1, Object param2) {
        if (param0 == this.empty()) {
            return DataResult.success(Map.of(param1, param2));
        } else if (param0 instanceof Map var0) {
            if (var0.isEmpty()) {
                return DataResult.success(Map.of(param1, param2));
            } else {
                Builder<Object, Object> var1 = ImmutableMap.builderWithExpectedSize(var0.size() + 1);
                var1.putAll(var0);
                var1.put(param1, param2);
                return DataResult.success(var1.buildKeepingLast());
            }
        } else {
            return DataResult.error(() -> "Not a map: " + param0);
        }
    }

    @Override
    public DataResult<Object> mergeToMap(Object param0, Map<Object, Object> param1) {
        if (param0 == this.empty()) {
            return DataResult.success(param1);
        } else if (param0 instanceof Map var0) {
            if (var0.isEmpty()) {
                return DataResult.success(param1);
            } else {
                Builder<Object, Object> var1 = ImmutableMap.builderWithExpectedSize(var0.size() + param1.size());
                var1.putAll(var0);
                var1.putAll(param1);
                return DataResult.success(var1.buildKeepingLast());
            }
        } else {
            return DataResult.error(() -> "Not a map: " + param0);
        }
    }

    private static Map<Object, Object> mapLikeToMap(MapLike<Object> param0) {
        return param0.entries().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
    }

    @Override
    public DataResult<Object> mergeToMap(Object param0, MapLike<Object> param1) {
        if (param0 == this.empty()) {
            return DataResult.success(mapLikeToMap(param1));
        } else if (param0 instanceof Map var0) {
            if (var0.isEmpty()) {
                return DataResult.success(mapLikeToMap(param1));
            } else {
                Builder<Object, Object> var1 = ImmutableMap.builderWithExpectedSize(var0.size());
                var1.putAll(var0);
                param1.entries().forEach(param1x -> var1.put(param1x.getFirst(), param1x.getSecond()));
                return DataResult.success(var1.buildKeepingLast());
            }
        } else {
            return DataResult.error(() -> "Not a map: " + param0);
        }
    }

    static Stream<Pair<Object, Object>> getMapEntries(Map<?, ?> param0) {
        return param0.entrySet().stream().map(param0x -> Pair.of(param0x.getKey(), param0x.getValue()));
    }

    @Override
    public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object param0) {
        return param0 instanceof Map var0 ? DataResult.success(getMapEntries(var0)) : DataResult.error(() -> "Not a map: " + param0);
    }

    @Override
    public DataResult<Consumer<BiConsumer<Object, Object>>> getMapEntries(Object param0) {
        return param0 instanceof Map var0 ? DataResult.success(var0::forEach) : DataResult.error(() -> "Not a map: " + param0);
    }

    @Override
    public Object createMap(Stream<Pair<Object, Object>> param0) {
        return param0.collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
    }

    @Override
    public DataResult<MapLike<Object>> getMap(Object param0) {
        return param0 instanceof Map var0 ? DataResult.success(new MapLike<Object>() {
            @Nullable
            @Override
            public Object get(Object param0) {
                return var0.get(param0);
            }

            @Nullable
            @Override
            public Object get(String param0) {
                return var0.get(param0);
            }

            @Override
            public Stream<Pair<Object, Object>> entries() {
                return JavaOps.getMapEntries(var0);
            }

            @Override
            public String toString() {
                return "MapLike[" + var0 + "]";
            }
        }) : DataResult.error(() -> "Not a map: " + param0);
    }

    @Override
    public Object createMap(Map<Object, Object> param0) {
        return param0;
    }

    @Override
    public DataResult<Stream<Object>> getStream(Object param0) {
        return param0 instanceof List var0 ? DataResult.success(var0.stream().map(param0x -> param0x)) : DataResult.error(() -> "Not an list: " + param0);
    }

    @Override
    public DataResult<Consumer<Consumer<Object>>> getList(Object param0) {
        return param0 instanceof List var0 ? DataResult.success(var0::forEach) : DataResult.error(() -> "Not an list: " + param0);
    }

    @Override
    public Object createList(Stream<Object> param0) {
        return param0.toList();
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(Object param0) {
        return param0 instanceof ByteList var0 ? DataResult.success(ByteBuffer.wrap(var0.toByteArray())) : DataResult.error(() -> "Not a byte list: " + param0);
    }

    @Override
    public Object createByteList(ByteBuffer param0) {
        ByteBuffer var0 = param0.duplicate().clear();
        ByteArrayList var1 = new ByteArrayList();
        var1.size(var0.capacity());
        var0.get(0, var1.elements(), 0, var1.size());
        return var1;
    }

    @Override
    public DataResult<IntStream> getIntStream(Object param0) {
        return param0 instanceof IntList var0 ? DataResult.success(var0.intStream()) : DataResult.error(() -> "Not an int list: " + param0);
    }

    @Override
    public Object createIntList(IntStream param0) {
        return IntArrayList.toList(param0);
    }

    @Override
    public DataResult<LongStream> getLongStream(Object param0) {
        return param0 instanceof LongList var0 ? DataResult.success(var0.longStream()) : DataResult.error(() -> "Not a long list: " + param0);
    }

    @Override
    public Object createLongList(LongStream param0) {
        return LongArrayList.toList(param0);
    }

    @Override
    public Object remove(Object param0, String param1) {
        if (param0 instanceof Map var0) {
            Map<Object, Object> var1 = new LinkedHashMap<>(var0);
            var1.remove(param1);
            return DataResult.success(Map.copyOf(var1));
        } else {
            return DataResult.error(() -> "Not a map: " + param0);
        }
    }

    @Override
    public RecordBuilder<Object> mapBuilder() {
        return new JavaOps.FixedMapBuilder<>(this);
    }

    @Override
    public String toString() {
        return "Java";
    }

    static final class FixedMapBuilder<T> extends AbstractUniversalBuilder<T, Builder<T, T>> {
        public FixedMapBuilder(DynamicOps<T> param0) {
            super(param0);
        }

        protected Builder<T, T> initBuilder() {
            return ImmutableMap.builder();
        }

        protected Builder<T, T> append(T param0, T param1, Builder<T, T> param2) {
            return param2.put(param0, param1);
        }

        protected DataResult<T> build(Builder<T, T> param0, T param1) {
            ImmutableMap<T, T> var0;
            try {
                var0 = param0.buildOrThrow();
            } catch (IllegalArgumentException var5) {
                return DataResult.error(() -> "Can't build map: " + var5.getMessage());
            }

            return this.ops().mergeToMap(param1, var0);
        }
    }
}
