package net.minecraft.nbt;

import java.io.DataOutput;
import java.io.IOException;

public interface Tag {
    void write(DataOutput var1) throws IOException;

    @Override
    String toString();

    byte getId();

    TagType<?> getType();

    Tag copy();

    default String getAsString() {
        return new StringTagVisitor().visit(this);
    }

    void accept(TagVisitor var1);
}
