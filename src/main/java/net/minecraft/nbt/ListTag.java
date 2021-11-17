package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ListTag extends CollectionTag<Tag> {
    private static final int SELF_SIZE_IN_BITS = 296;
    public static final TagType<ListTag> TYPE = new TagType.VariableSize<ListTag>() {
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
        public StreamTagVisitor.ValueResult parse(DataInput param0, StreamTagVisitor param1) throws IOException {
            TagType<?> var0 = TagTypes.getType(param0.readByte());
            int var1 = param0.readInt();
            switch(param1.visitList(var0, var1)) {
                case HALT:
                    return StreamTagVisitor.ValueResult.HALT;
                case BREAK:
                    var0.skip(param0, var1);
                    return param1.visitContainerEnd();
                default:
                    int var2 = 0;

                    while(true) {
                        label41: {
                            if (var2 < var1) {
                                switch(param1.visitElement(var0, var2)) {
                                    case HALT:
                                        return StreamTagVisitor.ValueResult.HALT;
                                    case BREAK:
                                        var0.skip(param0);
                                        break;
                                    case SKIP:
                                        var0.skip(param0);
                                        break label41;
                                    default:
                                        switch(var0.parse(param0, param1)) {
                                            case HALT:
                                                return StreamTagVisitor.ValueResult.HALT;
                                            case BREAK:
                                                break;
                                            default:
                                                break label41;
                                        }
                                }
                            }

                            int var3 = var1 - 1 - var2;
                            if (var3 > 0) {
                                var0.skip(param0, var3);
                            }

                            return param1.visitContainerEnd();
                        }

                        ++var2;
                    }
            }
        }

        @Override
        public void skip(DataInput param0) throws IOException {
            TagType<?> var0 = TagTypes.getType(param0.readByte());
            int var1 = param0.readInt();
            var0.skip(param0, var1);
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
    private final List<Tag> list;
    private byte type;

    ListTag(List<Tag> param0, byte param1) {
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
        return this.getAsString();
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

    public long[] getLongArray(int param0) {
        if (param0 >= 0 && param0 < this.list.size()) {
            Tag var0 = this.list.get(param0);
            if (var0.getId() == 11) {
                return ((LongArrayTag)var0).getAsLongArray();
            }
        }

        return new long[0];
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
    public void accept(TagVisitor param0) {
        param0.visitList(this);
    }

    @Override
    public byte getElementType() {
        return this.type;
    }

    @Override
    public void clear() {
        this.list.clear();
        this.type = 0;
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor param0) {
        switch(param0.visitList(TagTypes.getType(this.type), this.list.size())) {
            case HALT:
                return StreamTagVisitor.ValueResult.HALT;
            case BREAK:
                return param0.visitContainerEnd();
            default:
                int var0 = 0;

                while(var0 < this.list.size()) {
                    Tag var1 = this.list.get(var0);
                    switch(param0.visitElement(var1.getType(), var0)) {
                        case HALT:
                            return StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                            return param0.visitContainerEnd();
                        default:
                            switch(var1.accept(param0)) {
                                case HALT:
                                    return StreamTagVisitor.ValueResult.HALT;
                                case BREAK:
                                    return param0.visitContainerEnd();
                            }
                        case SKIP:
                            ++var0;
                    }
                }

                return param0.visitContainerEnd();
        }
    }
}
