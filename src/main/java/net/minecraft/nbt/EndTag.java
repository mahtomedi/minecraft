package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag implements Tag {
    private static final int SELF_SIZE_IN_BYTES = 8;
    public static final TagType<EndTag> TYPE = new TagType<EndTag>() {
        public EndTag load(DataInput param0, NbtAccounter param1) {
            param1.accountBytes(8L);
            return EndTag.INSTANCE;
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput param0, StreamTagVisitor param1, NbtAccounter param2) {
            param2.accountBytes(8L);
            return param1.visitEnd();
        }

        @Override
        public void skip(DataInput param0, int param1, NbtAccounter param2) {
        }

        @Override
        public void skip(DataInput param0, NbtAccounter param1) {
        }

        @Override
        public String getName() {
            return "END";
        }

        @Override
        public String getPrettyName() {
            return "TAG_End";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    public static final EndTag INSTANCE = new EndTag();

    private EndTag() {
    }

    @Override
    public void write(DataOutput param0) throws IOException {
    }

    @Override
    public int sizeInBytes() {
        return 8;
    }

    @Override
    public byte getId() {
        return 0;
    }

    @Override
    public TagType<EndTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    public EndTag copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor param0) {
        param0.visitEnd(this);
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor param0) {
        return param0.visitEnd();
    }
}
