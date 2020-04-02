package net.minecraft.nbt;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NbtOps implements DynamicOps<Tag> {
    public static final NbtOps INSTANCE = new NbtOps();

    protected NbtOps() {
    }

    public Tag empty() {
        return EndTag.INSTANCE;
    }

    public Type<?> getType(Tag param0) {
        switch(param0.getId()) {
            case 0:
                return DSL.nilType();
            case 1:
                return DSL.byteType();
            case 2:
                return DSL.shortType();
            case 3:
                return DSL.intType();
            case 4:
                return DSL.longType();
            case 5:
                return DSL.floatType();
            case 6:
                return DSL.doubleType();
            case 7:
                return DSL.list(DSL.byteType());
            case 8:
                return DSL.string();
            case 9:
                return DSL.list(DSL.remainderType());
            case 10:
                return DSL.compoundList(DSL.remainderType(), DSL.remainderType());
            case 11:
                return DSL.list(DSL.intType());
            case 12:
                return DSL.list(DSL.longType());
            default:
                return DSL.remainderType();
        }
    }

    public Optional<Number> getNumberValue(Tag param0) {
        return param0 instanceof NumericTag ? Optional.of(((NumericTag)param0).getAsNumber()) : Optional.empty();
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

    public Optional<String> getStringValue(Tag param0) {
        return param0 instanceof StringTag ? Optional.of(param0.getAsString()) : Optional.empty();
    }

    public Tag createString(String param0) {
        return StringTag.valueOf(param0);
    }

    public Tag mergeInto(Tag param0, Tag param1) {
        if (param1 instanceof EndTag) {
            return param0;
        } else if (!(param0 instanceof CompoundTag)) {
            if (param0 instanceof EndTag) {
                throw new IllegalArgumentException("mergeInto called with a null input.");
            } else if (param0 instanceof CollectionTag) {
                CollectionTag<Tag> var5 = new ListTag();
                CollectionTag<?> var6 = (CollectionTag)param0;
                var5.addAll(var6);
                var5.add(param1);
                return var5;
            } else {
                return param0;
            }
        } else if (!(param1 instanceof CompoundTag)) {
            return param0;
        } else {
            CompoundTag var0 = new CompoundTag();
            CompoundTag var1 = (CompoundTag)param0;

            for(String var2 : var1.getAllKeys()) {
                var0.put(var2, var1.get(var2));
            }

            CompoundTag var3 = (CompoundTag)param1;

            for(String var4 : var3.getAllKeys()) {
                var0.put(var4, var3.get(var4));
            }

            return var0;
        }
    }

    public Tag mergeInto(Tag param0, Tag param1, Tag param2) {
        CompoundTag var0;
        if (param0 instanceof EndTag) {
            var0 = new CompoundTag();
        } else {
            if (!(param0 instanceof CompoundTag)) {
                return param0;
            }

            CompoundTag var1 = (CompoundTag)param0;
            var0 = new CompoundTag();
            var1.getAllKeys().forEach(param2x -> var0.put(param2x, var1.get(param2x)));
        }

        var0.put(param1.getAsString(), param2);
        return var0;
    }

    public Tag merge(Tag param0, Tag param1) {
        if (param0 instanceof EndTag) {
            return param1;
        } else if (param1 instanceof EndTag) {
            return param0;
        } else if (param0 instanceof CompoundTag && param1 instanceof CompoundTag) {
            CompoundTag var0 = (CompoundTag)param0;
            CompoundTag var1 = (CompoundTag)param1;
            CompoundTag var2 = new CompoundTag();
            var0.getAllKeys().forEach(param2 -> var2.put(param2, var0.get(param2)));
            var1.getAllKeys().forEach(param2 -> var2.put(param2, var1.get(param2)));
            return var2;
        } else if (param0 instanceof CollectionTag && param1 instanceof CollectionTag) {
            ListTag var3 = new ListTag();
            var3.addAll((CollectionTag)param0);
            var3.addAll((CollectionTag)param1);
            return var3;
        } else {
            throw new IllegalArgumentException("Could not merge " + param0 + " and " + param1);
        }
    }

    public Optional<Map<Tag, Tag>> getMapValues(Tag param0) {
        if (param0 instanceof CompoundTag) {
            CompoundTag var0 = (CompoundTag)param0;
            return Optional.of(
                var0.getAllKeys()
                    .stream()
                    .map(param1 -> Pair.of(this.createString(param1), var0.get(param1)))
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
            );
        } else {
            return Optional.empty();
        }
    }

    public Tag createMap(Map<Tag, Tag> param0) {
        CompoundTag var0 = new CompoundTag();

        for(Entry<Tag, Tag> var1 : param0.entrySet()) {
            var0.put(var1.getKey().getAsString(), var1.getValue());
        }

        return var0;
    }

    public Optional<Stream<Tag>> getStream(Tag param0) {
        return param0 instanceof CollectionTag ? Optional.of(((CollectionTag)param0).stream().map(param0x -> param0x)) : Optional.empty();
    }

    public Optional<ByteBuffer> getByteBuffer(Tag param0) {
        return param0 instanceof ByteArrayTag ? Optional.of(ByteBuffer.wrap(((ByteArrayTag)param0).getAsByteArray())) : DynamicOps.super.getByteBuffer(param0);
    }

    public Tag createByteList(ByteBuffer param0) {
        return new ByteArrayTag(DataFixUtils.toArray(param0));
    }

    public Optional<IntStream> getIntStream(Tag param0) {
        return param0 instanceof IntArrayTag ? Optional.of(Arrays.stream(((IntArrayTag)param0).getAsIntArray())) : DynamicOps.super.getIntStream(param0);
    }

    public Tag createIntList(IntStream param0) {
        return new IntArrayTag(param0.toArray());
    }

    public Optional<LongStream> getLongStream(Tag param0) {
        return param0 instanceof LongArrayTag ? Optional.of(Arrays.stream(((LongArrayTag)param0).getAsLongArray())) : DynamicOps.super.getLongStream(param0);
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
        if (param0 instanceof CompoundTag) {
            CompoundTag var0 = (CompoundTag)param0;
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
}
