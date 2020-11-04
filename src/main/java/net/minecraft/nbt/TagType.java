package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface TagType<T extends Tag> {
    T load(DataInput var1, int var2, NbtAccounter var3) throws IOException;

    default boolean isValue() {
        return false;
    }

    String getName();

    String getPrettyName();

    static TagType<EndTag> createInvalid(final int param0) {
        return new TagType<EndTag>() {
            public EndTag load(DataInput param0x, int param1, NbtAccounter param2) {
                throw new IllegalArgumentException("Invalid tag id: " + param0);
            }

            @Override
            public String getName() {
                return "INVALID[" + param0 + "]";
            }

            @Override
            public String getPrettyName() {
                return "UNKNOWN_" + param0;
            }
        };
    }
}
