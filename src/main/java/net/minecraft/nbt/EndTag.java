package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag implements Tag {
    public static final TagType<EndTag> TYPE = new TagType<EndTag>() {
        public EndTag load(DataInput param0, int param1, NbtAccounter param2) {
            param2.accountBits(64L);
            return EndTag.INSTANCE;
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
}
