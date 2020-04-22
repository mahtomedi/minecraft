package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class ListTag extends CollectionTag<Tag> {
    public static final TagType<ListTag> TYPE = new TagType<ListTag>() {
        public ListTag load(DataInput param0, int param1, NbtAccounter param2) throws IOException {
            param2.accountBits(296L);
            if (param1 > 512) {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            } else {
                byte var0 = param0.readByte();
                int var1 = param0.readInt();
                if (var0 == 0 && var1 > 0) {
                    throw new RuntimeException("Missing type on ListTag");
                } else {
                    param2.accountBits(32L * (long)var1);
                    TagType<?> var2 = TagTypes.getType(var0);
                    List<Tag> var3 = Lists.newArrayListWithCapacity(var1);

                    for(int var4 = 0; var4 < var1; ++var4) {
                        var3.add(var2.load(param0, param1 + 1, param2));
                    }

                    return new ListTag(var3, var0);
                }
            }
        }

        @Override
        public String getName() {
            return "LIST";
        }

        @Override
        public String getPrettyName() {
            return "TAG_List";
        }
    };
    private static final ByteSet INLINE_ELEMENT_TYPES = new ByteOpenHashSet(Arrays.asList((byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6));
    private final List<Tag> list;
    private byte type;

    private ListTag(List<Tag> param0, byte param1) {
        this.list = param0;
        this.type = param1;
    }

    public ListTag() {
        this(Lists.newArrayList(), (byte)0);
    }

    @Override
    public void write(DataOutput param0) throws IOException {
        if (this.list.isEmpty()) {
            this.type = 0;
        } else {
            this.type = this.list.get(0).getId();
        }

        param0.writeByte(this.type);
        param0.writeInt(this.list.size());

        for(Tag var0 : this.list) {
            var0.write(param0);
        }

    }

    @Override
    public byte getId() {
        return 9;
    }

    @Override
    public TagType<ListTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder("[");

        for(int var1 = 0; var1 < this.list.size(); ++var1) {
            if (var1 != 0) {
                var0.append(',');
            }

            var0.append(this.list.get(var1));
        }

        return var0.append(']').toString();
    }

    private void updateTypeAfterRemove() {
        if (this.list.isEmpty()) {
            this.type = 0;
        }

    }

    @Override
    public Tag remove(int param0) {
        Tag var0 = this.list.remove(param0);
        this.updateTypeAfterRemove();
        return var0;
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public CompoundTag getCompound(int param0) {
        if (param0 >= 0 && param0 < this.list.size()) {
            Tag var0 = this.list.get(param0);
            if (var0.getId() == 10) {
                return (CompoundTag)var0;
            }
        }

        return new CompoundTag();
    }

    public ListTag getList(int param0) {
        if (param0 >= 0 && param0 < this.list.size()) {
            Tag var0 = this.list.get(param0);
            if (var0.getId() == 9) {
                return (ListTag)var0;
            }
        }

        return new ListTag();
    }

    public short getShort(int param0) {
        if (param0 >= 0 && param0 < this.list.size()) {
            Tag var0 = this.list.get(param0);
            if (var0.getId() == 2) {
                return ((ShortTag)var0).getAsShort();
            }
        }

        return 0;
    }

    public int getInt(int param0) {
        if (param0 >= 0 && param0 < this.list.size()) {
            Tag var0 = this.list.get(param0);
            if (var0.getId() == 3) {
                return ((IntTag)var0).getAsInt();
            }
        }

        return 0;
    }

    public int[] getIntArray(int param0) {
        if (param0 >= 0 && param0 < this.list.size()) {
            Tag var0 = this.list.get(param0);
            if (var0.getId() == 11) {
                return ((IntArrayTag)var0).getAsIntArray();
            }
        }

        return new int[0];
    }

    public double getDouble(int param0) {
        if (param0 >= 0 && param0 < this.list.size()) {
            Tag var0 = this.list.get(param0);
            if (var0.getId() == 6) {
                return ((DoubleTag)var0).getAsDouble();
            }
        }

        return 0.0;
    }

    public float getFloat(int param0) {
        if (param0 >= 0 && param0 < this.list.size()) {
            Tag var0 = this.list.get(param0);
            if (var0.getId() == 5) {
                return ((FloatTag)var0).getAsFloat();
            }
        }

        return 0.0F;
    }

    public String getString(int param0) {
        if (param0 >= 0 && param0 < this.list.size()) {
            Tag var0 = this.list.get(param0);
            return var0.getId() == 8 ? var0.getAsString() : var0.toString();
        } else {
            return "";
        }
    }

    @Override
    public int size() {
        return this.list.size();
    }

    public Tag get(int param0) {
        return this.list.get(param0);
    }

    @Override
    public Tag set(int param0, Tag param1) {
        Tag var0 = this.get(param0);
        if (!this.setTag(param0, param1)) {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", param1.getId(), this.type));
        } else {
            return var0;
        }
    }

    @Override
    public void add(int param0, Tag param1) {
        if (!this.addTag(param0, param1)) {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", param1.getId(), this.type));
        }
    }

    @Override
    public boolean setTag(int param0, Tag param1) {
        if (this.updateType(param1)) {
            this.list.set(param0, param1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTag(int param0, Tag param1) {
        if (this.updateType(param1)) {
            this.list.add(param0, param1);
            return true;
        } else {
            return false;
        }
    }

    private boolean updateType(Tag param0) {
        if (param0.getId() == 0) {
            return false;
        } else if (this.type == 0) {
            this.type = param0.getId();
            return true;
        } else {
            return this.type == param0.getId();
        }
    }

    public ListTag copy() {
        Iterable<Tag> var0 = (Iterable<Tag>)(TagTypes.getType(this.type).isValue() ? this.list : Iterables.transform(this.list, Tag::copy));
        List<Tag> var1 = Lists.newArrayList(var0);
        return new ListTag(var1, this.type);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof ListTag && Objects.equals(this.list, ((ListTag)param0).list);
        }
    }

    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    @Override
    public Component getPrettyDisplay(String param0, int param1) {
        if (this.isEmpty()) {
            return new TextComponent("[]");
        } else if (INLINE_ELEMENT_TYPES.contains(this.type) && this.size() <= 8) {
            String var0 = ", ";
            MutableComponent var1 = new TextComponent("[");

            for(int var2 = 0; var2 < this.list.size(); ++var2) {
                if (var2 != 0) {
                    var1.append(", ");
                }

                var1.append(this.list.get(var2).getPrettyDisplay());
            }

            var1.append("]");
            return var1;
        } else {
            MutableComponent var3 = new TextComponent("[");
            if (!param0.isEmpty()) {
                var3.append("\n");
            }

            String var4 = String.valueOf(',');

            for(int var5 = 0; var5 < this.list.size(); ++var5) {
                MutableComponent var6 = new TextComponent(Strings.repeat(param0, param1 + 1));
                var6.append(this.list.get(var5).getPrettyDisplay(param0, param1 + 1));
                if (var5 != this.list.size() - 1) {
                    var6.append(var4).append(param0.isEmpty() ? " " : "\n");
                }

                var3.append(var6);
            }

            if (!param0.isEmpty()) {
                var3.append("\n").append(Strings.repeat(param0, param1));
            }

            var3.append("]");
            return var3;
        }
    }

    public int getElementType() {
        return this.type;
    }

    @Override
    public void clear() {
        this.list.clear();
        this.type = 0;
    }
}
