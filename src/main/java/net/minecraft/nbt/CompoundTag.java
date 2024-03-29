package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class CompoundTag implements Tag {
    public static final Codec<CompoundTag> CODEC = Codec.PASSTHROUGH
        .comapFlatMap(
            param0 -> {
                Tag var0 = param0.convert(NbtOps.INSTANCE).getValue();
                return var0 instanceof CompoundTag var1
                    ? DataResult.success(var1 == param0.getValue() ? var1.copy() : var1)
                    : DataResult.error(() -> "Not a compound tag: " + var0);
            },
            param0 -> new Dynamic<>(NbtOps.INSTANCE, param0.copy())
        );
    private static final int SELF_SIZE_IN_BYTES = 48;
    private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;
    public static final TagType<CompoundTag> TYPE = new TagType.VariableSize<CompoundTag>() {
        public CompoundTag load(DataInput param0, NbtAccounter param1) throws IOException {
            param1.pushDepth();

            CompoundTag var3;
            try {
                var3 = loadCompound(param0, param1);
            } finally {
                param1.popDepth();
            }

            return var3;
        }

        private static CompoundTag loadCompound(DataInput param0, NbtAccounter param1) throws IOException {
            param1.accountBytes(48L);
            Map<String, Tag> var0 = Maps.newHashMap();

            byte var1;
            while((var1 = param0.readByte()) != 0) {
                String var2 = readString(param0, param1);
                Tag var3 = CompoundTag.readNamedTagData(TagTypes.getType(var1), var2, param0, param1);
                if (var0.put(var2, var3) == null) {
                    param1.accountBytes(36L);
                }
            }

            return new CompoundTag(var0);
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput param0, StreamTagVisitor param1, NbtAccounter param2) throws IOException {
            param2.pushDepth();

            StreamTagVisitor.ValueResult var4;
            try {
                var4 = parseCompound(param0, param1, param2);
            } finally {
                param2.popDepth();
            }

            return var4;
        }

        private static StreamTagVisitor.ValueResult parseCompound(DataInput param0, StreamTagVisitor param1, NbtAccounter param2) throws IOException {
            param2.accountBytes(48L);

            byte var0;
            label35:
            while((var0 = param0.readByte()) != 0) {
                TagType<?> var1 = TagTypes.getType(var0);
                switch(param1.visitEntry(var1)) {
                    case HALT:
                        return StreamTagVisitor.ValueResult.HALT;
                    case BREAK:
                        StringTag.skipString(param0);
                        var1.skip(param0, param2);
                        break label35;
                    case SKIP:
                        StringTag.skipString(param0);
                        var1.skip(param0, param2);
                        break;
                    default:
                        String var2 = readString(param0, param2);
                        switch(param1.visitEntry(var1, var2)) {
                            case HALT:
                                return StreamTagVisitor.ValueResult.HALT;
                            case BREAK:
                                var1.skip(param0, param2);
                                break label35;
                            case SKIP:
                                var1.skip(param0, param2);
                                break;
                            default:
                                param2.accountBytes(36L);
                                switch(var1.parse(param0, param1, param2)) {
                                    case HALT:
                                        return StreamTagVisitor.ValueResult.HALT;
                                    case BREAK:
                                }
                        }
                }
            }

            if (var0 != 0) {
                while((var0 = param0.readByte()) != 0) {
                    StringTag.skipString(param0);
                    TagTypes.getType(var0).skip(param0, param2);
                }
            }

            return param1.visitContainerEnd();
        }

        private static String readString(DataInput param0, NbtAccounter param1) throws IOException {
            String var0 = param0.readUTF();
            param1.accountBytes(28L);
            param1.accountBytes(2L, (long)var0.length());
            return var0;
        }

        @Override
        public void skip(DataInput param0, NbtAccounter param1) throws IOException {
            param1.pushDepth();

            byte var0;
            try {
                while((var0 = param0.readByte()) != 0) {
                    StringTag.skipString(param0);
                    TagTypes.getType(var0).skip(param0, param1);
                }
            } finally {
                param1.popDepth();
            }

        }

        @Override
        public String getName() {
            return "COMPOUND";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Compound";
        }
    };
    private final Map<String, Tag> tags;

    protected CompoundTag(Map<String, Tag> param0) {
        this.tags = param0;
    }

    public CompoundTag() {
        this(Maps.newHashMap());
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        for(String var0 : this.tags.keySet()) {
            Tag var1 = this.tags.get(var0);
            writeNamedTag(var0, var1, param0);
        }

        param0.writeByte(0);
    }

    @Override
    public int sizeInBytes() {
        int var0 = 48;

        for(Entry<String, Tag> var1 : this.tags.entrySet()) {
            var0 += 28 + 2 * var1.getKey().length();
            var0 += 36;
            var0 += var1.getValue().sizeInBytes();
        }

        return var0;
    }

    public Set<String> getAllKeys() {
        return this.tags.keySet();
    }

    @Override
    public byte getId() {
        return 10;
    }

    @Override
    public TagType<CompoundTag> getType() {
        return TYPE;
    }

    public int size() {
        return this.tags.size();
    }

    @Nullable
    public Tag put(String param0, Tag param1) {
        return this.tags.put(param0, param1);
    }

    public void putByte(String param0, byte param1) {
        this.tags.put(param0, ByteTag.valueOf(param1));
    }

    public void putShort(String param0, short param1) {
        this.tags.put(param0, ShortTag.valueOf(param1));
    }

    public void putInt(String param0, int param1) {
        this.tags.put(param0, IntTag.valueOf(param1));
    }

    public void putLong(String param0, long param1) {
        this.tags.put(param0, LongTag.valueOf(param1));
    }

    public void putUUID(String param0, UUID param1) {
        this.tags.put(param0, NbtUtils.createUUID(param1));
    }

    public UUID getUUID(String param0) {
        return NbtUtils.loadUUID(this.get(param0));
    }

    public boolean hasUUID(String param0) {
        Tag var0 = this.get(param0);
        return var0 != null && var0.getType() == IntArrayTag.TYPE && ((IntArrayTag)var0).getAsIntArray().length == 4;
    }

    public void putFloat(String param0, float param1) {
        this.tags.put(param0, FloatTag.valueOf(param1));
    }

    public void putDouble(String param0, double param1) {
        this.tags.put(param0, DoubleTag.valueOf(param1));
    }

    public void putString(String param0, String param1) {
        this.tags.put(param0, StringTag.valueOf(param1));
    }

    public void putByteArray(String param0, byte[] param1) {
        this.tags.put(param0, new ByteArrayTag(param1));
    }

    public void putByteArray(String param0, List<Byte> param1) {
        this.tags.put(param0, new ByteArrayTag(param1));
    }

    public void putIntArray(String param0, int[] param1) {
        this.tags.put(param0, new IntArrayTag(param1));
    }

    public void putIntArray(String param0, List<Integer> param1) {
        this.tags.put(param0, new IntArrayTag(param1));
    }

    public void putLongArray(String param0, long[] param1) {
        this.tags.put(param0, new LongArrayTag(param1));
    }

    public void putLongArray(String param0, List<Long> param1) {
        this.tags.put(param0, new LongArrayTag(param1));
    }

    public void putBoolean(String param0, boolean param1) {
        this.tags.put(param0, ByteTag.valueOf(param1));
    }

    @Nullable
    public Tag get(String param0) {
        return this.tags.get(param0);
    }

    public byte getTagType(String param0) {
        Tag var0 = this.tags.get(param0);
        return var0 == null ? 0 : var0.getId();
    }

    public boolean contains(String param0) {
        return this.tags.containsKey(param0);
    }

    public boolean contains(String param0, int param1) {
        int var0 = this.getTagType(param0);
        if (var0 == param1) {
            return true;
        } else if (param1 != 99) {
            return false;
        } else {
            return var0 == 1 || var0 == 2 || var0 == 3 || var0 == 4 || var0 == 5 || var0 == 6;
        }
    }

    public byte getByte(String param0) {
        try {
            if (this.contains(param0, 99)) {
                return ((NumericTag)this.tags.get(param0)).getAsByte();
            }
        } catch (ClassCastException var3) {
        }

        return 0;
    }

    public short getShort(String param0) {
        try {
            if (this.contains(param0, 99)) {
                return ((NumericTag)this.tags.get(param0)).getAsShort();
            }
        } catch (ClassCastException var3) {
        }

        return 0;
    }

    public int getInt(String param0) {
        try {
            if (this.contains(param0, 99)) {
                return ((NumericTag)this.tags.get(param0)).getAsInt();
            }
        } catch (ClassCastException var3) {
        }

        return 0;
    }

    public long getLong(String param0) {
        try {
            if (this.contains(param0, 99)) {
                return ((NumericTag)this.tags.get(param0)).getAsLong();
            }
        } catch (ClassCastException var3) {
        }

        return 0L;
    }

    public float getFloat(String param0) {
        try {
            if (this.contains(param0, 99)) {
                return ((NumericTag)this.tags.get(param0)).getAsFloat();
            }
        } catch (ClassCastException var3) {
        }

        return 0.0F;
    }

    public double getDouble(String param0) {
        try {
            if (this.contains(param0, 99)) {
                return ((NumericTag)this.tags.get(param0)).getAsDouble();
            }
        } catch (ClassCastException var3) {
        }

        return 0.0;
    }

    public String getString(String param0) {
        try {
            if (this.contains(param0, 8)) {
                return this.tags.get(param0).getAsString();
            }
        } catch (ClassCastException var3) {
        }

        return "";
    }

    public byte[] getByteArray(String param0) {
        try {
            if (this.contains(param0, 7)) {
                return ((ByteArrayTag)this.tags.get(param0)).getAsByteArray();
            }
        } catch (ClassCastException var3) {
            throw new ReportedException(this.createReport(param0, ByteArrayTag.TYPE, var3));
        }

        return new byte[0];
    }

    public int[] getIntArray(String param0) {
        try {
            if (this.contains(param0, 11)) {
                return ((IntArrayTag)this.tags.get(param0)).getAsIntArray();
            }
        } catch (ClassCastException var3) {
            throw new ReportedException(this.createReport(param0, IntArrayTag.TYPE, var3));
        }

        return new int[0];
    }

    public long[] getLongArray(String param0) {
        try {
            if (this.contains(param0, 12)) {
                return ((LongArrayTag)this.tags.get(param0)).getAsLongArray();
            }
        } catch (ClassCastException var3) {
            throw new ReportedException(this.createReport(param0, LongArrayTag.TYPE, var3));
        }

        return new long[0];
    }

    public CompoundTag getCompound(String param0) {
        try {
            if (this.contains(param0, 10)) {
                return (CompoundTag)this.tags.get(param0);
            }
        } catch (ClassCastException var3) {
            throw new ReportedException(this.createReport(param0, TYPE, var3));
        }

        return new CompoundTag();
    }

    public ListTag getList(String param0, int param1) {
        try {
            if (this.getTagType(param0) == 9) {
                ListTag var0 = (ListTag)this.tags.get(param0);
                if (!var0.isEmpty() && var0.getElementType() != param1) {
                    return new ListTag();
                }

                return var0;
            }
        } catch (ClassCastException var4) {
            throw new ReportedException(this.createReport(param0, ListTag.TYPE, var4));
        }

        return new ListTag();
    }

    public boolean getBoolean(String param0) {
        return this.getByte(param0) != 0;
    }

    public void remove(String param0) {
        this.tags.remove(param0);
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    public boolean isEmpty() {
        return this.tags.isEmpty();
    }

    private CrashReport createReport(String param0, TagType<?> param1, ClassCastException param2) {
        CrashReport var0 = CrashReport.forThrowable(param2, "Reading NBT data");
        CrashReportCategory var1 = var0.addCategory("Corrupt NBT tag", 1);
        var1.setDetail("Tag type found", () -> this.tags.get(param0).getType().getName());
        var1.setDetail("Tag type expected", param1::getName);
        var1.setDetail("Tag name", param0);
        return var0;
    }

    public CompoundTag copy() {
        Map<String, Tag> var0 = Maps.newHashMap(Maps.transformValues(this.tags, Tag::copy));
        return new CompoundTag(var0);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag)param0).tags);
        }
    }

    @Override
    public int hashCode() {
        return this.tags.hashCode();
    }

    private static void writeNamedTag(String param0, Tag param1, DataOutput param2) throws IOException {
        param2.writeByte(param1.getId());
        if (param1.getId() != 0) {
            param2.writeUTF(param0);
            param1.write(param2);
        }
    }

    static Tag readNamedTagData(TagType<?> param0, String param1, DataInput param2, NbtAccounter param3) {
        try {
            return param0.load(param2, param3);
        } catch (IOException var7) {
            CrashReport var1 = CrashReport.forThrowable(var7, "Loading NBT data");
            CrashReportCategory var2 = var1.addCategory("NBT Tag");
            var2.setDetail("Tag name", param1);
            var2.setDetail("Tag type", param0.getName());
            throw new ReportedNbtException(var1);
        }
    }

    public CompoundTag merge(CompoundTag param0) {
        for(String var0 : param0.tags.keySet()) {
            Tag var1 = param0.tags.get(var0);
            if (var1.getId() == 10) {
                if (this.contains(var0, 10)) {
                    CompoundTag var2 = this.getCompound(var0);
                    var2.merge((CompoundTag)var1);
                } else {
                    this.put(var0, var1.copy());
                }
            } else {
                this.put(var0, var1.copy());
            }
        }

        return this;
    }

    @Override
    public void accept(TagVisitor param0) {
        param0.visitCompound(this);
    }

    protected Map<String, Tag> entries() {
        return Collections.unmodifiableMap(this.tags);
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor param0) {
        for(Entry<String, Tag> var0 : this.tags.entrySet()) {
            Tag var1 = var0.getValue();
            TagType<?> var2 = var1.getType();
            StreamTagVisitor.EntryResult var3 = param0.visitEntry(var2);
            switch(var3) {
                case HALT:
                    return StreamTagVisitor.ValueResult.HALT;
                case BREAK:
                    return param0.visitContainerEnd();
                case SKIP:
                    break;
                default:
                    var3 = param0.visitEntry(var2, var0.getKey());
                    switch(var3) {
                        case HALT:
                            return StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                            return param0.visitContainerEnd();
                        case SKIP:
                            break;
                        default:
                            StreamTagVisitor.ValueResult var4 = var1.accept(param0);
                            switch(var4) {
                                case HALT:
                                    return StreamTagVisitor.ValueResult.HALT;
                                case BREAK:
                                    return param0.visitContainerEnd();
                            }
                    }
            }
        }

        return param0.visitContainerEnd();
    }
}
