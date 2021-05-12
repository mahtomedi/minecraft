package net.minecraft.nbt;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.PeekingIterator;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractStringBuilder;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class NbtOps implements DynamicOps<Tag> {
    public static final NbtOps INSTANCE = new NbtOps();

    protected NbtOps() {
    }

    public Tag empty() {
        return EndTag.INSTANCE;
    }

    public <U> U convertTo(DynamicOps<U> param0, Tag param1) {
        switch(param1.getId()) {
            case 0:
                return param0.empty();
            case 1:
                return param0.createByte(((NumericTag)param1).getAsByte());
            case 2:
                return param0.createShort(((NumericTag)param1).getAsShort());
            case 3:
                return param0.createInt(((NumericTag)param1).getAsInt());
            case 4:
                return param0.createLong(((NumericTag)param1).getAsLong());
            case 5:
                return param0.createFloat(((NumericTag)param1).getAsFloat());
            case 6:
                return param0.createDouble(((NumericTag)param1).getAsDouble());
            case 7:
                return param0.createByteList(ByteBuffer.wrap(((ByteArrayTag)param1).getAsByteArray()));
            case 8:
                return param0.createString(param1.getAsString());
            case 9:
                return this.convertList(param0, param1);
            case 10:
                return this.convertMap(param0, param1);
            case 11:
                return param0.createIntList(Arrays.stream(((IntArrayTag)param1).getAsIntArray()));
            case 12:
                return param0.createLongList(Arrays.stream(((LongArrayTag)param1).getAsLongArray()));
            default:
                throw new IllegalStateException("Unknown tag type: " + param1);
        }
    }

    public DataResult<Number> getNumberValue(Tag param0) {
        return param0 instanceof NumericTag ? DataResult.success(((NumericTag)param0).getAsNumber()) : DataResult.error("Not a number");
    }

    public Tag createNumeric(Number param0) {
        return DoubleTag.valueOf(param0.doubleValue());
    }

    public Tag createByte(byte param0) {
        return ByteTag.valueOf(param0);
    }

    public Tag createShort(short param0) {
        return ShortTag.valueOf(param0);
    }

    public Tag createInt(int param0) {
        return IntTag.valueOf(param0);
    }

    public Tag createLong(long param0) {
        return LongTag.valueOf(param0);
    }

    public Tag createFloat(float param0) {
        return FloatTag.valueOf(param0);
    }

    public Tag createDouble(double param0) {
        return DoubleTag.valueOf(param0);
    }

    public Tag createBoolean(boolean param0) {
        return ByteTag.valueOf(param0);
    }

    public DataResult<String> getStringValue(Tag param0) {
        return param0 instanceof StringTag ? DataResult.success(param0.getAsString()) : DataResult.error("Not a string");
    }

    public Tag createString(String param0) {
        return StringTag.valueOf(param0);
    }

    private static CollectionTag<?> createGenericList(byte param0, byte param1) {
        if (typesMatch(param0, param1, (byte)4)) {
            return new LongArrayTag(new long[0]);
        } else if (typesMatch(param0, param1, (byte)1)) {
            return new ByteArrayTag(new byte[0]);
        } else {
            return (CollectionTag<?>)(typesMatch(param0, param1, (byte)3) ? new IntArrayTag(new int[0]) : new ListTag());
        }
    }

    private static boolean typesMatch(byte param0, byte param1, byte param2) {
        return param0 == param2 && (param1 == param2 || param1 == 0);
    }

    private static <T extends Tag> void fillOne(CollectionTag<T> param0, Tag param1, Tag param2) {
        if (param1 instanceof CollectionTag var0) {
            var0.forEach(param1x -> param0.add(param1x));
        }

        param0.add(param2);
    }

    private static <T extends Tag> void fillMany(CollectionTag<T> param0, Tag param1, List<Tag> param2) {
        if (param1 instanceof CollectionTag var0) {
            var0.forEach(param1x -> param0.add(param1x));
        }

        param2.forEach(param1x -> param0.add(param1x));
    }

    public DataResult<Tag> mergeToList(Tag param0, Tag param1) {
        if (!(param0 instanceof CollectionTag) && !(param0 instanceof EndTag)) {
            return DataResult.error("mergeToList called with not a list: " + param0, param0);
        } else {
            CollectionTag<?> var0 = createGenericList(param0 instanceof CollectionTag ? ((CollectionTag)param0).getElementType() : 0, param1.getId());
            fillOne(var0, param0, param1);
            return DataResult.success(var0);
        }
    }

    public DataResult<Tag> mergeToList(Tag param0, List<Tag> param1) {
        if (!(param0 instanceof CollectionTag) && !(param0 instanceof EndTag)) {
            return DataResult.error("mergeToList called with not a list: " + param0, param0);
        } else {
            CollectionTag<?> var0 = createGenericList(
                param0 instanceof CollectionTag ? ((CollectionTag)param0).getElementType() : 0, param1.stream().findFirst().map(Tag::getId).orElse((byte)0)
            );
            fillMany(var0, param0, param1);
            return DataResult.success(var0);
        }
    }

    public DataResult<Tag> mergeToMap(Tag param0, Tag param1, Tag param2) {
        if (!(param0 instanceof CompoundTag) && !(param0 instanceof EndTag)) {
            return DataResult.error("mergeToMap called with not a map: " + param0, param0);
        } else if (!(param1 instanceof StringTag)) {
            return DataResult.error("key is not a string: " + param1, param0);
        } else {
            CompoundTag var0 = new CompoundTag();
            if (param0 instanceof CompoundTag var1) {
                var1.getAllKeys().forEach(param2x -> var0.put(param2x, var1.get(param2x)));
            }

            var0.put(param1.getAsString(), param2);
            return DataResult.success(var0);
        }
    }

    public DataResult<Tag> mergeToMap(Tag param0, MapLike<Tag> param1) {
        if (!(param0 instanceof CompoundTag) && !(param0 instanceof EndTag)) {
            return DataResult.error("mergeToMap called with not a map: " + param0, param0);
        } else {
            CompoundTag var0 = new CompoundTag();
            if (param0 instanceof CompoundTag var1) {
                var1.getAllKeys().forEach(param2 -> var0.put(param2, var1.get(param2)));
            }

            List<Tag> var2 = Lists.newArrayList();
            param1.entries().forEach(param2 -> {
                Tag var0x = param2.getFirst();
                if (!(var0x instanceof StringTag)) {
                    var2.add(var0x);
                } else {
                    var0.put(var0x.getAsString(), param2.getSecond());
                }
            });
            return !var2.isEmpty() ? DataResult.error("some keys are not strings: " + var2, var0) : DataResult.success(var0);
        }
    }

    public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag param0) {
        if (!(param0 instanceof CompoundTag)) {
            return DataResult.error("Not a map: " + param0);
        } else {
            CompoundTag var0 = (CompoundTag)param0;
            return DataResult.success(var0.getAllKeys().stream().map(param1 -> Pair.of(this.createString(param1), var0.get(param1))));
        }
    }

    public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag param0) {
        if (!(param0 instanceof CompoundTag)) {
            return DataResult.error("Not a map: " + param0);
        } else {
            CompoundTag var0 = (CompoundTag)param0;
            return DataResult.success(param1 -> var0.getAllKeys().forEach(param2 -> param1.accept(this.createString(param2), var0.get(param2))));
        }
    }

    public DataResult<MapLike<Tag>> getMap(Tag param0) {
        if (!(param0 instanceof CompoundTag)) {
            return DataResult.error("Not a map: " + param0);
        } else {
            final CompoundTag var0 = (CompoundTag)param0;
            return DataResult.success(new MapLike<Tag>() {
                @Nullable
                public Tag get(Tag param0) {
                    return var0.get(param0.getAsString());
                }

                @Nullable
                public Tag get(String param0) {
                    return var0.get(param0);
                }

                @Override
                public Stream<Pair<Tag, Tag>> entries() {
                    return var0.getAllKeys().stream().map(param1 -> Pair.of(NbtOps.this.createString(param1), var0.get(param1)));
                }

                @Override
                public String toString() {
                    return "MapLike[" + var0 + "]";
                }
            });
        }
    }

    public Tag createMap(Stream<Pair<Tag, Tag>> param0) {
        CompoundTag var0 = new CompoundTag();
        param0.forEach(param1 -> var0.put(param1.getFirst().getAsString(), param1.getSecond()));
        return var0;
    }

    public DataResult<Stream<Tag>> getStream(Tag param0) {
        return param0 instanceof CollectionTag ? DataResult.success(((CollectionTag)param0).stream().map(param0x -> param0x)) : DataResult.error("Not a list");
    }

    public DataResult<Consumer<Consumer<Tag>>> getList(Tag param0) {
        return param0 instanceof CollectionTag var0 ? DataResult.success(var0::forEach) : DataResult.error("Not a list: " + param0);
    }

    public DataResult<ByteBuffer> getByteBuffer(Tag param0) {
        return param0 instanceof ByteArrayTag
            ? DataResult.success(ByteBuffer.wrap(((ByteArrayTag)param0).getAsByteArray()))
            : DynamicOps.super.getByteBuffer(param0);
    }

    public Tag createByteList(ByteBuffer param0) {
        return new ByteArrayTag(DataFixUtils.toArray(param0));
    }

    public DataResult<IntStream> getIntStream(Tag param0) {
        return param0 instanceof IntArrayTag ? DataResult.success(Arrays.stream(((IntArrayTag)param0).getAsIntArray())) : DynamicOps.super.getIntStream(param0);
    }

    public Tag createIntList(IntStream param0) {
        return new IntArrayTag(param0.toArray());
    }

    public DataResult<LongStream> getLongStream(Tag param0) {
        return param0 instanceof LongArrayTag
            ? DataResult.success(Arrays.stream(((LongArrayTag)param0).getAsLongArray()))
            : DynamicOps.super.getLongStream(param0);
    }

    public Tag createLongList(LongStream param0) {
        return new LongArrayTag(param0.toArray());
    }

    public Tag createList(Stream<Tag> param0) {
        PeekingIterator<Tag> var0 = Iterators.peekingIterator(param0.iterator());
        if (!var0.hasNext()) {
            return new ListTag();
        } else {
            Tag var1 = var0.peek();
            if (var1 instanceof ByteTag) {
                List<Byte> var2 = Lists.newArrayList(Iterators.transform(var0, param0x -> ((ByteTag)param0x).getAsByte()));
                return new ByteArrayTag(var2);
            } else if (var1 instanceof IntTag) {
                List<Integer> var3 = Lists.newArrayList(Iterators.transform(var0, param0x -> ((IntTag)param0x).getAsInt()));
                return new IntArrayTag(var3);
            } else if (var1 instanceof LongTag) {
                List<Long> var4 = Lists.newArrayList(Iterators.transform(var0, param0x -> ((LongTag)param0x).getAsLong()));
                return new LongArrayTag(var4);
            } else {
                ListTag var5 = new ListTag();

                while(var0.hasNext()) {
                    Tag var6 = var0.next();
                    if (!(var6 instanceof EndTag)) {
                        var5.add(var6);
                    }
                }

                return var5;
            }
        }
    }

    public Tag remove(Tag param0, String param1) {
        if (param0 instanceof CompoundTag var0) {
            CompoundTag var1 = new CompoundTag();
            var0.getAllKeys().stream().filter(param1x -> !Objects.equals(param1x, param1)).forEach(param2 -> var1.put(param2, var0.get(param2)));
            return var1;
        } else {
            return param0;
        }
    }

    @Override
    public String toString() {
        return "NBT";
    }

    @Override
    public RecordBuilder<Tag> mapBuilder() {
        return new NbtOps.NbtRecordBuilder();
    }

    class NbtRecordBuilder extends AbstractStringBuilder<Tag, CompoundTag> {
        protected NbtRecordBuilder() {
            super(NbtOps.this);
        }

        protected CompoundTag initBuilder() {
            return new CompoundTag();
        }

        protected CompoundTag append(String param0, Tag param1, CompoundTag param2) {
            param2.put(param0, param1);
            return param2;
        }

        protected DataResult<Tag> build(CompoundTag param0, Tag param1) {
            if (param1 == null || param1 == EndTag.INSTANCE) {
                return DataResult.success(param0);
            } else if (!(param1 instanceof CompoundTag)) {
                return DataResult.error("mergeToMap called with not a map: " + param1, param1);
            } else {
                CompoundTag var0 = new CompoundTag(Maps.newHashMap(((CompoundTag)param1).entries()));

                for(Entry<String, Tag> var1 : param0.entries().entrySet()) {
                    var0.put(var1.getKey(), var1.getValue());
                }

                return DataResult.success(var0);
            }
        }
    }
}
