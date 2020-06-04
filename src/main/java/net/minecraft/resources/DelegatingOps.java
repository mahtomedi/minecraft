package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public abstract class DelegatingOps<T> implements DynamicOps<T> {
    protected final DynamicOps<T> delegate;

    protected DelegatingOps(DynamicOps<T> param0) {
        this.delegate = param0;
    }

    @Override
    public T empty() {
        return this.delegate.empty();
    }

    @Override
    public <U> U convertTo(DynamicOps<U> param0, T param1) {
        return this.delegate.convertTo(param0, param1);
    }

    @Override
    public DataResult<Number> getNumberValue(T param0) {
        return this.delegate.getNumberValue(param0);
    }

    @Override
    public T createNumeric(Number param0) {
        return this.delegate.createNumeric(param0);
    }

    @Override
    public T createByte(byte param0) {
        return this.delegate.createByte(param0);
    }

    @Override
    public T createShort(short param0) {
        return this.delegate.createShort(param0);
    }

    @Override
    public T createInt(int param0) {
        return this.delegate.createInt(param0);
    }

    @Override
    public T createLong(long param0) {
        return this.delegate.createLong(param0);
    }

    @Override
    public T createFloat(float param0) {
        return this.delegate.createFloat(param0);
    }

    @Override
    public T createDouble(double param0) {
        return this.delegate.createDouble(param0);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(T param0) {
        return this.delegate.getBooleanValue(param0);
    }

    @Override
    public T createBoolean(boolean param0) {
        return this.delegate.createBoolean(param0);
    }

    @Override
    public DataResult<String> getStringValue(T param0) {
        return this.delegate.getStringValue(param0);
    }

    @Override
    public T createString(String param0) {
        return this.delegate.createString(param0);
    }

    @Override
    public DataResult<T> mergeToList(T param0, T param1) {
        return this.delegate.mergeToList(param0, param1);
    }

    @Override
    public DataResult<T> mergeToList(T param0, List<T> param1) {
        return this.delegate.mergeToList(param0, param1);
    }

    @Override
    public DataResult<T> mergeToMap(T param0, T param1, T param2) {
        return this.delegate.mergeToMap(param0, param1, param2);
    }

    @Override
    public DataResult<T> mergeToMap(T param0, MapLike<T> param1) {
        return this.delegate.mergeToMap(param0, param1);
    }

    @Override
    public DataResult<Stream<Pair<T, T>>> getMapValues(T param0) {
        return this.delegate.getMapValues(param0);
    }

    @Override
    public DataResult<Consumer<BiConsumer<T, T>>> getMapEntries(T param0) {
        return this.delegate.getMapEntries(param0);
    }

    @Override
    public T createMap(Stream<Pair<T, T>> param0) {
        return this.delegate.createMap(param0);
    }

    @Override
    public DataResult<MapLike<T>> getMap(T param0) {
        return this.delegate.getMap(param0);
    }

    @Override
    public DataResult<Stream<T>> getStream(T param0) {
        return this.delegate.getStream(param0);
    }

    @Override
    public DataResult<Consumer<Consumer<T>>> getList(T param0) {
        return this.delegate.getList(param0);
    }

    @Override
    public T createList(Stream<T> param0) {
        return this.delegate.createList(param0);
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(T param0) {
        return this.delegate.getByteBuffer(param0);
    }

    @Override
    public T createByteList(ByteBuffer param0) {
        return this.delegate.createByteList(param0);
    }

    @Override
    public DataResult<IntStream> getIntStream(T param0) {
        return this.delegate.getIntStream(param0);
    }

    @Override
    public T createIntList(IntStream param0) {
        return this.delegate.createIntList(param0);
    }

    @Override
    public DataResult<LongStream> getLongStream(T param0) {
        return this.delegate.getLongStream(param0);
    }

    @Override
    public T createLongList(LongStream param0) {
        return this.delegate.createLongList(param0);
    }

    @Override
    public T remove(T param0, String param1) {
        return this.delegate.remove(param0, param1);
    }

    @Override
    public boolean compressMaps() {
        return this.delegate.compressMaps();
    }

    @Override
    public ListBuilder<T> listBuilder() {
        return this.delegate.listBuilder();
    }

    @Override
    public RecordBuilder<T> mapBuilder() {
        return this.delegate.mapBuilder();
    }
}
