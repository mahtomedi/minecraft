package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface TagType<T extends Tag> {
    T load(DataInput var1, int var2, NbtAccounter var3) throws IOException;

    StreamTagVisitor.ValueResult parse(DataInput var1, StreamTagVisitor var2) throws IOException;

    default void parseRoot(DataInput param0, StreamTagVisitor param1) throws IOException {
        switch(param1.visitRootEntry(this)) {
            case CONTINUE:
                this.parse(param0, param1);
            case HALT:
            default:
                break;
            case BREAK:
                this.skip(param0);
        }

    }

    void skip(DataInput var1, int var2) throws IOException;

    void skip(DataInput var1) throws IOException;

    default boolean isValue() {
        return false;
    }

    String getName();

    String getPrettyName();

    static TagType<EndTag> createInvalid(final int param0) {
        return new TagType<EndTag>() {
            private IOException createException() {
                return new IOException("Invalid tag id: " + param0);
            }

            public EndTag load(DataInput param0x, int param1, NbtAccounter param2) throws IOException {
                throw this.createException();
            }

            @Override
            public StreamTagVisitor.ValueResult parse(DataInput param0x, StreamTagVisitor param1) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput param0x, int param1) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput param0x) throws IOException {
                throw this.createException();
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

    public interface StaticSize<T extends Tag> extends TagType<T> {
        @Override
        default void skip(DataInput param0) throws IOException {
            param0.skipBytes(this.size());
        }

        @Override
        default void skip(DataInput param0, int param1) throws IOException {
            param0.skipBytes(this.size() * param1);
        }

        int size();
    }

    public interface VariableSize<T extends Tag> extends TagType<T> {
        @Override
        default void skip(DataInput param0, int param1) throws IOException {
            for(int var0 = 0; var0 < param1; ++var0) {
                this.skip(param0);
            }

        }
    }
}
