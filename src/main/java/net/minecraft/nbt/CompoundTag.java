package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CompoundTag implements Tag {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    public static final TagType<CompoundTag> TYPE = new TagType<CompoundTag>() {
        public CompoundTag load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
            param2.accountBits(384L);
            if (param1 > 512) {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            } else {
                Map<String, Tag> var0 = Maps.newHashMap();

                byte var1;
                while((var1 = CompoundTag.readNamedTagType(param0, param2)) != 0) {
                    String var2 = CompoundTag.readNamedTagName(param0, param2);
                    param2.accountBits((long)(224 + 16 * var2.length()));
                    Tag var3 = CompoundTag.readNamedTagData(TagTypes.getType(var1), var2, param0, param1 + 1, param2);
                    if (var0.put(var2, var3) != null) {
                        param2.accountBits(288L);
                    }
                }

                return new CompoundTag(var0);
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

    private CompoundTag(Map<String, Tag> param0) {
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
        StringBuilder var0 = new StringBuilder("{");
        Collection<String> var1 = this.tags.keySet();
        if (LOGGER.isDebugEnabled()) {
            List<String> var2 = Lists.newArrayList(this.tags.keySet());
            Collections.sort(var2);
            var1 = var2;
        }

        for(String var3 : var1) {
            if (var0.length() != 1) {
                var0.append(',');
            }

            var0.append(handleEscape(var3)).append(':').append(this.tags.get(var3));
        }

        return var0.append('}').toString();
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

    private static byte readNamedTagType(DataInput param0, NbtAccounter param1) throws IOException {
        return param0.readByte();
    }

    private static String readNamedTagName(DataInput param0, NbtAccounter param1) throws IOException {
        return param0.readUTF();
    }

    private static Tag readNamedTagData(TagType<?> param0, String param1, DataInput param2, int param3, NbtAccounter param4) {
        try {
            return param0.load(param2, param3, param4);
        } catch (IOException var8) {
            CrashReport var1 = CrashReport.forThrowable(var8, "Loading NBT data");
            CrashReportCategory var2 = var1.addCategory("NBT Tag");
            var2.setDetail("Tag name", param1);
            var2.setDetail("Tag type", param0.getName());
            throw new ReportedException(var1);
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

    protected static String handleEscape(String param0) {
        return SIMPLE_VALUE.matcher(param0).matches() ? param0 : StringTag.quoteAndEscape(param0);
    }

    protected static Component handleEscapePretty(String param0) {
        if (SIMPLE_VALUE.matcher(param0).matches()) {
            return new TextComponent(param0).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        } else {
            String var0 = StringTag.quoteAndEscape(param0);
            String var1 = var0.substring(0, 1);
            Component var2 = new TextComponent(var0.substring(1, var0.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
            return new TextComponent(var1).append(var2).append(var1);
        }
    }

    @Override
    public Component getPrettyDisplay(String param0, int param1) {
        if (this.tags.isEmpty()) {
            return new TextComponent("{}");
        } else {
            MutableComponent var0 = new TextComponent("{");
            Collection<String> var1 = this.tags.keySet();
            if (LOGGER.isDebugEnabled()) {
                List<String> var2 = Lists.newArrayList(this.tags.keySet());
                Collections.sort(var2);
                var1 = var2;
            }

            if (!param0.isEmpty()) {
                var0.append("\n");
            }

            MutableComponent var5;
            for(Iterator<String> var3 = var1.iterator(); var3.hasNext(); var0.append(var5)) {
                String var4 = var3.next();
                var5 = new TextComponent(Strings.repeat(param0, param1 + 1))
                    .append(handleEscapePretty(var4))
                    .append(String.valueOf(':'))
                    .append(" ")
                    .append(this.tags.get(var4).getPrettyDisplay(param0, param1 + 1));
                if (var3.hasNext()) {
                    var5.append(String.valueOf(',')).append(param0.isEmpty() ? " " : "\n");
                }
            }

            if (!param0.isEmpty()) {
                var0.append("\n").append(Strings.repeat(param0, param1));
            }

            var0.append("}");
            return var0;
        }
    }
}
