package net.minecraft.nbt;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class StringTagVisitor implements TagVisitor {
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private final StringBuilder builder = new StringBuilder();

    public String visit(Tag param0) {
        param0.accept(this);
        return this.builder.toString();
    }

    @Override
    public void visitString(StringTag param0) {
        this.builder.append(StringTag.quoteAndEscape(param0.getAsString()));
    }

    @Override
    public void visitByte(ByteTag param0) {
        this.builder.append(param0.getAsNumber()).append('b');
    }

    @Override
    public void visitShort(ShortTag param0) {
        this.builder.append(param0.getAsNumber()).append('s');
    }

    @Override
    public void visitInt(IntTag param0) {
        this.builder.append(param0.getAsNumber());
    }

    @Override
    public void visitLong(LongTag param0) {
        this.builder.append(param0.getAsNumber()).append('L');
    }

    @Override
    public void visitFloat(FloatTag param0) {
        this.builder.append(param0.getAsFloat()).append('f');
    }

    @Override
    public void visitDouble(DoubleTag param0) {
        this.builder.append(param0.getAsDouble()).append('d');
    }

    @Override
    public void visitByteArray(ByteArrayTag param0) {
        this.builder.append("[B;");
        byte[] var0 = param0.getAsByteArray();

        for(int var1 = 0; var1 < var0.length; ++var1) {
            if (var1 != 0) {
                this.builder.append(',');
            }

            this.builder.append(var0[var1]).append('B');
        }

        this.builder.append(']');
    }

    @Override
    public void visitIntArray(IntArrayTag param0) {
        this.builder.append("[I;");
        int[] var0 = param0.getAsIntArray();

        for(int var1 = 0; var1 < var0.length; ++var1) {
            if (var1 != 0) {
                this.builder.append(',');
            }

            this.builder.append(var0[var1]);
        }

        this.builder.append(']');
    }

    @Override
    public void visitLongArray(LongArrayTag param0) {
        this.builder.append("[L;");
        long[] var0 = param0.getAsLongArray();

        for(int var1 = 0; var1 < var0.length; ++var1) {
            if (var1 != 0) {
                this.builder.append(',');
            }

            this.builder.append(var0[var1]).append('L');
        }

        this.builder.append(']');
    }

    @Override
    public void visitList(ListTag param0) {
        this.builder.append('[');

        for(int var0 = 0; var0 < param0.size(); ++var0) {
            if (var0 != 0) {
                this.builder.append(',');
            }

            this.builder.append(new StringTagVisitor().visit(param0.get(var0)));
        }

        this.builder.append(']');
    }

    @Override
    public void visitCompound(CompoundTag param0) {
        this.builder.append('{');
        List<String> var0 = Lists.newArrayList(param0.getAllKeys());
        Collections.sort(var0);

        for(String var1 : var0) {
            if (this.builder.length() != 1) {
                this.builder.append(',');
            }

            this.builder.append(handleEscape(var1)).append(':').append(new StringTagVisitor().visit(param0.get(var1)));
        }

        this.builder.append('}');
    }

    protected static String handleEscape(String param0) {
        return SIMPLE_VALUE.matcher(param0).matches() ? param0 : StringTag.quoteAndEscape(param0);
    }

    @Override
    public void visitEnd(EndTag param0) {
        this.builder.append("END");
    }
}
