package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.Util;

public class SnbtPrinterTagVisitor implements TagVisitor {
    private static final Map<String, List<String>> KEY_ORDER = Util.make(Maps.newHashMap(), param0 -> {
        param0.put("{}", Lists.newArrayList("DataVersion", "author", "size", "data", "entities", "palette", "palettes"));
        param0.put("{}.data.[].{}", Lists.newArrayList("pos", "state", "nbt"));
        param0.put("{}.entities.[].{}", Lists.newArrayList("blockPos", "pos"));
    });
    private static final Set<String> NO_INDENTATION = Sets.newHashSet("{}.size.[]", "{}.data.[].{}", "{}.palette.[].{}", "{}.entities.[].{}");
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
    private final List<String> path;
    private String result = "";

    public SnbtPrinterTagVisitor() {
        this("    ", 0, Lists.newArrayList());
    }

    public SnbtPrinterTagVisitor(String param0, int param1, List<String> param2) {
        this.indentation = param0;
        this.depth = param1;
        this.path = param2;
    }

    public String visit(Tag param0) {
        param0.accept(this);
        return this.result;
    }

    @Override
    public void visitString(StringTag param0) {
        this.result = StringTag.quoteAndEscape(param0.getAsString());
    }

    @Override
    public void visitByte(ByteTag param0) {
        this.result = param0.getAsNumber() + "b";
    }

    @Override
    public void visitShort(ShortTag param0) {
        this.result = param0.getAsNumber() + "s";
    }

    @Override
    public void visitInt(IntTag param0) {
        this.result = String.valueOf(param0.getAsNumber());
    }

    @Override
    public void visitLong(LongTag param0) {
        this.result = param0.getAsNumber() + "L";
    }

    @Override
    public void visitFloat(FloatTag param0) {
        this.result = param0.getAsFloat() + "f";
    }

    @Override
    public void visitDouble(DoubleTag param0) {
        this.result = param0.getAsDouble() + "d";
    }

    @Override
    public void visitByteArray(ByteArrayTag param0) {
        StringBuilder var0 = new StringBuilder("[").append("B").append(";");
        byte[] var1 = param0.getAsByteArray();

        for(int var2 = 0; var2 < var1.length; ++var2) {
            var0.append(" ").append(var1[var2]).append("B");
            if (var2 != var1.length - 1) {
                var0.append(ELEMENT_SEPARATOR);
            }
        }

        var0.append("]");
        this.result = var0.toString();
    }

    @Override
    public void visitIntArray(IntArrayTag param0) {
        StringBuilder var0 = new StringBuilder("[").append("I").append(";");
        int[] var1 = param0.getAsIntArray();

        for(int var2 = 0; var2 < var1.length; ++var2) {
            var0.append(" ").append(var1[var2]);
            if (var2 != var1.length - 1) {
                var0.append(ELEMENT_SEPARATOR);
            }
        }

        var0.append("]");
        this.result = var0.toString();
    }

    @Override
    public void visitLongArray(LongArrayTag param0) {
        String var0 = "L";
        StringBuilder var1 = new StringBuilder("[").append("L").append(";");
        long[] var2 = param0.getAsLongArray();

        for(int var3 = 0; var3 < var2.length; ++var3) {
            var1.append(" ").append(var2[var3]).append("L");
            if (var3 != var2.length - 1) {
                var1.append(ELEMENT_SEPARATOR);
            }
        }

        var1.append("]");
        this.result = var1.toString();
    }

    @Override
    public void visitList(ListTag param0) {
        if (param0.isEmpty()) {
            this.result = "[]";
        } else {
            StringBuilder var0 = new StringBuilder("[");
            this.pushPath("[]");
            String var1 = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
            if (!var1.isEmpty()) {
                var0.append("\n");
            }

            for(int var2 = 0; var2 < param0.size(); ++var2) {
                var0.append(Strings.repeat(var1, this.depth + 1));
                var0.append(new SnbtPrinterTagVisitor(var1, this.depth + 1, this.path).visit(param0.get(var2)));
                if (var2 != param0.size() - 1) {
                    var0.append(ELEMENT_SEPARATOR).append(var1.isEmpty() ? " " : "\n");
                }
            }

            if (!var1.isEmpty()) {
                var0.append("\n").append(Strings.repeat(var1, this.depth));
            }

            var0.append("]");
            this.result = var0.toString();
            this.popPath();
        }
    }

    @Override
    public void visitCompound(CompoundTag param0) {
        if (param0.isEmpty()) {
            this.result = "{}";
        } else {
            StringBuilder var0 = new StringBuilder("{");
            this.pushPath("{}");
            String var1 = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
            if (!var1.isEmpty()) {
                var0.append("\n");
            }

            Collection<String> var2 = this.getKeys(param0);
            Iterator<String> var3 = var2.iterator();

            while(var3.hasNext()) {
                String var4 = var3.next();
                Tag var5 = param0.get(var4);
                this.pushPath(var4);
                var0.append(Strings.repeat(var1, this.depth + 1))
                    .append(handleEscapePretty(var4))
                    .append(NAME_VALUE_SEPARATOR)
                    .append(" ")
                    .append(new SnbtPrinterTagVisitor(var1, this.depth + 1, this.path).visit(var5));
                this.popPath();
                if (var3.hasNext()) {
                    var0.append(ELEMENT_SEPARATOR).append(var1.isEmpty() ? " " : "\n");
                }
            }

            if (!var1.isEmpty()) {
                var0.append("\n").append(Strings.repeat(var1, this.depth));
            }

            var0.append("}");
            this.result = var0.toString();
            this.popPath();
        }
    }

    private void popPath() {
        this.path.remove(this.path.size() - 1);
    }

    private void pushPath(String param0) {
        this.path.add(param0);
    }

    protected List<String> getKeys(CompoundTag param0) {
        Set<String> var0 = Sets.newHashSet(param0.getAllKeys());
        List<String> var1 = Lists.newArrayList();
        List<String> var2 = KEY_ORDER.get(this.pathString());
        if (var2 != null) {
            for(String var3 : var2) {
                if (var0.remove(var3)) {
                    var1.add(var3);
                }
            }

            if (!var0.isEmpty()) {
                var0.stream().sorted().forEach(var1::add);
            }
        } else {
            var1.addAll(var0);
            Collections.sort(var1);
        }

        return var1;
    }

    public String pathString() {
        return String.join(".", this.path);
    }

    protected static String handleEscapePretty(String param0) {
        return SIMPLE_VALUE.matcher(param0).matches() ? param0 : StringTag.quoteAndEscape(param0);
    }

    @Override
    public void visitEnd(EndTag param0) {
    }
}
