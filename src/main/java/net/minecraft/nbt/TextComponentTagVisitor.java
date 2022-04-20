package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;

public class TextComponentTagVisitor implements TagVisitor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int INLINE_LIST_THRESHOLD = 8;
    private static final ByteCollection INLINE_ELEMENT_TYPES = new ByteOpenHashSet(Arrays.asList((byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6));
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_KEY = ChatFormatting.AQUA;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_STRING = ChatFormatting.GREEN;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER = ChatFormatting.GOLD;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER_TYPE = ChatFormatting.RED;
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String NAME_VALUE_SEPARATOR = String.valueOf(':');
    private static final String ELEMENT_SEPARATOR = String.valueOf(',');
    private static final String LIST_OPEN = "[";
    private static final String LIST_CLOSE = "]";
    private static final String LIST_TYPE_SEPARATOR = ";";
    private static final String ELEMENT_SPACING = " ";
    private static final String STRUCT_OPEN = "{";
    private static final String STRUCT_CLOSE = "}";
    private static final String NEWLINE = "\n";
    private final String indentation;
    private final int depth;
    private Component result = CommonComponents.EMPTY;

    public TextComponentTagVisitor(String param0, int param1) {
        this.indentation = param0;
        this.depth = param1;
    }

    public Component visit(Tag param0) {
        param0.accept(this);
        return this.result;
    }

    @Override
    public void visitString(StringTag param0) {
        String var0 = StringTag.quoteAndEscape(param0.getAsString());
        String var1 = var0.substring(0, 1);
        Component var2 = Component.literal(var0.substring(1, var0.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_STRING);
        this.result = Component.literal(var1).append(var2).append(var1);
    }

    @Override
    public void visitByte(ByteTag param0) {
        Component var0 = Component.literal("b").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = Component.literal(String.valueOf(param0.getAsNumber())).append(var0).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitShort(ShortTag param0) {
        Component var0 = Component.literal("s").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = Component.literal(String.valueOf(param0.getAsNumber())).append(var0).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitInt(IntTag param0) {
        this.result = Component.literal(String.valueOf(param0.getAsNumber())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitLong(LongTag param0) {
        Component var0 = Component.literal("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = Component.literal(String.valueOf(param0.getAsNumber())).append(var0).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitFloat(FloatTag param0) {
        Component var0 = Component.literal("f").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = Component.literal(String.valueOf(param0.getAsFloat())).append(var0).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitDouble(DoubleTag param0) {
        Component var0 = Component.literal("d").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = Component.literal(String.valueOf(param0.getAsDouble())).append(var0).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitByteArray(ByteArrayTag param0) {
        Component var0 = Component.literal("B").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent var1 = Component.literal("[").append(var0).append(";");
        byte[] var2 = param0.getAsByteArray();

        for(int var3 = 0; var3 < var2.length; ++var3) {
            MutableComponent var4 = Component.literal(String.valueOf(var2[var3])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            var1.append(" ").append(var4).append(var0);
            if (var3 != var2.length - 1) {
                var1.append(ELEMENT_SEPARATOR);
            }
        }

        var1.append("]");
        this.result = var1;
    }

    @Override
    public void visitIntArray(IntArrayTag param0) {
        Component var0 = Component.literal("I").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent var1 = Component.literal("[").append(var0).append(";");
        int[] var2 = param0.getAsIntArray();

        for(int var3 = 0; var3 < var2.length; ++var3) {
            var1.append(" ").append(Component.literal(String.valueOf(var2[var3])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
            if (var3 != var2.length - 1) {
                var1.append(ELEMENT_SEPARATOR);
            }
        }

        var1.append("]");
        this.result = var1;
    }

    @Override
    public void visitLongArray(LongArrayTag param0) {
        Component var0 = Component.literal("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent var1 = Component.literal("[").append(var0).append(";");
        long[] var2 = param0.getAsLongArray();

        for(int var3 = 0; var3 < var2.length; ++var3) {
            Component var4 = Component.literal(String.valueOf(var2[var3])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            var1.append(" ").append(var4).append(var0);
            if (var3 != var2.length - 1) {
                var1.append(ELEMENT_SEPARATOR);
            }
        }

        var1.append("]");
        this.result = var1;
    }

    @Override
    public void visitList(ListTag param0) {
        if (param0.isEmpty()) {
            this.result = Component.literal("[]");
        } else if (INLINE_ELEMENT_TYPES.contains(param0.getElementType()) && param0.size() <= 8) {
            String var0 = ELEMENT_SEPARATOR + " ";
            MutableComponent var1 = Component.literal("[");

            for(int var2 = 0; var2 < param0.size(); ++var2) {
                if (var2 != 0) {
                    var1.append(var0);
                }

                var1.append(new TextComponentTagVisitor(this.indentation, this.depth).visit(param0.get(var2)));
            }

            var1.append("]");
            this.result = var1;
        } else {
            MutableComponent var3 = Component.literal("[");
            if (!this.indentation.isEmpty()) {
                var3.append("\n");
            }

            for(int var4 = 0; var4 < param0.size(); ++var4) {
                MutableComponent var5 = Component.literal(Strings.repeat(this.indentation, this.depth + 1));
                var5.append(new TextComponentTagVisitor(this.indentation, this.depth + 1).visit(param0.get(var4)));
                if (var4 != param0.size() - 1) {
                    var5.append(ELEMENT_SEPARATOR).append(this.indentation.isEmpty() ? " " : "\n");
                }

                var3.append(var5);
            }

            if (!this.indentation.isEmpty()) {
                var3.append("\n").append(Strings.repeat(this.indentation, this.depth));
            }

            var3.append("]");
            this.result = var3;
        }
    }

    @Override
    public void visitCompound(CompoundTag param0) {
        if (param0.isEmpty()) {
            this.result = Component.literal("{}");
        } else {
            MutableComponent var0 = Component.literal("{");
            Collection<String> var1 = param0.getAllKeys();
            if (LOGGER.isDebugEnabled()) {
                List<String> var2 = Lists.newArrayList(param0.getAllKeys());
                Collections.sort(var2);
                var1 = var2;
            }

            if (!this.indentation.isEmpty()) {
                var0.append("\n");
            }

            MutableComponent var5;
            for(Iterator<String> var3 = var1.iterator(); var3.hasNext(); var0.append(var5)) {
                String var4 = var3.next();
                var5 = Component.literal(Strings.repeat(this.indentation, this.depth + 1))
                    .append(handleEscapePretty(var4))
                    .append(NAME_VALUE_SEPARATOR)
                    .append(" ")
                    .append(new TextComponentTagVisitor(this.indentation, this.depth + 1).visit(param0.get(var4)));
                if (var3.hasNext()) {
                    var5.append(ELEMENT_SEPARATOR).append(this.indentation.isEmpty() ? " " : "\n");
                }
            }

            if (!this.indentation.isEmpty()) {
                var0.append("\n").append(Strings.repeat(this.indentation, this.depth));
            }

            var0.append("}");
            this.result = var0;
        }
    }

    protected static Component handleEscapePretty(String param0) {
        if (SIMPLE_VALUE.matcher(param0).matches()) {
            return Component.literal(param0).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        } else {
            String var0 = StringTag.quoteAndEscape(param0);
            String var1 = var0.substring(0, 1);
            Component var2 = Component.literal(var0.substring(1, var0.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
            return Component.literal(var1).append(var2).append(var1);
        }
    }

    @Override
    public void visitEnd(EndTag param0) {
        this.result = CommonComponents.EMPTY;
    }
}
