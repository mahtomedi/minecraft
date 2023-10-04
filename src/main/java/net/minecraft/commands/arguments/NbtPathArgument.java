package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NbtPathArgument implements ArgumentType<NbtPathArgument.NbtPath> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar", "foo[0]", "[0]", "[]", "{foo=bar}");
    public static final SimpleCommandExceptionType ERROR_INVALID_NODE = new SimpleCommandExceptionType(Component.translatable("arguments.nbtpath.node.invalid"));
    public static final SimpleCommandExceptionType ERROR_DATA_TOO_DEEP = new SimpleCommandExceptionType(Component.translatable("arguments.nbtpath.too_deep"));
    public static final DynamicCommandExceptionType ERROR_NOTHING_FOUND = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("arguments.nbtpath.nothing_found", param0)
    );
    static final DynamicCommandExceptionType ERROR_EXPECTED_LIST = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("commands.data.modify.expected_list", param0)
    );
    static final DynamicCommandExceptionType ERROR_INVALID_INDEX = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("commands.data.modify.invalid_index", param0)
    );
    private static final char INDEX_MATCH_START = '[';
    private static final char INDEX_MATCH_END = ']';
    private static final char KEY_MATCH_START = '{';
    private static final char KEY_MATCH_END = '}';
    private static final char QUOTED_KEY_START = '"';
    private static final char SINGLE_QUOTED_KEY_START = '\'';

    public static NbtPathArgument nbtPath() {
        return new NbtPathArgument();
    }

    public static NbtPathArgument.NbtPath getPath(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, NbtPathArgument.NbtPath.class);
    }

    public NbtPathArgument.NbtPath parse(StringReader param0) throws CommandSyntaxException {
        List<NbtPathArgument.Node> var0 = Lists.newArrayList();
        int var1 = param0.getCursor();
        Object2IntMap<NbtPathArgument.Node> var2 = new Object2IntOpenHashMap<>();
        boolean var3 = true;

        while(param0.canRead() && param0.peek() != ' ') {
            NbtPathArgument.Node var4 = parseNode(param0, var3);
            var0.add(var4);
            var2.put(var4, param0.getCursor() - var1);
            var3 = false;
            if (param0.canRead()) {
                char var5 = param0.peek();
                if (var5 != ' ' && var5 != '[' && var5 != '{') {
                    param0.expect('.');
                }
            }
        }

        return new NbtPathArgument.NbtPath(param0.getString().substring(var1, param0.getCursor()), var0.toArray(new NbtPathArgument.Node[0]), var2);
    }

    private static NbtPathArgument.Node parseNode(StringReader param0, boolean param1) throws CommandSyntaxException {
        Object var10000;
        switch(param0.peek()) {
            case '"':
            case '\'':
                var10000 = readObjectNode(param0, param0.readString());
                break;
            case '[':
                param0.skip();
                int var1 = param0.peek();
                if (var1 == 123) {
                    CompoundTag var2 = new TagParser(param0).readStruct();
                    param0.expect(']');
                    var10000 = new NbtPathArgument.MatchElementNode(var2);
                } else if (var1 == 93) {
                    param0.skip();
                    var10000 = NbtPathArgument.AllElementsNode.INSTANCE;
                } else {
                    int var3 = param0.readInt();
                    param0.expect(']');
                    var10000 = new NbtPathArgument.IndexedElementNode(var3);
                }
                break;
            case '{':
                if (!param1) {
                    throw ERROR_INVALID_NODE.createWithContext(param0);
                }

                CompoundTag var0 = new TagParser(param0).readStruct();
                var10000 = new NbtPathArgument.MatchRootObjectNode(var0);
                break;
            default:
                var10000 = readObjectNode(param0, readUnquotedName(param0));
        }

        return (NbtPathArgument.Node)var10000;
    }

    private static NbtPathArgument.Node readObjectNode(StringReader param0, String param1) throws CommandSyntaxException {
        if (param0.canRead() && param0.peek() == '{') {
            CompoundTag var0 = new TagParser(param0).readStruct();
            return new NbtPathArgument.MatchObjectNode(param1, var0);
        } else {
            return new NbtPathArgument.CompoundChildNode(param1);
        }
    }

    private static String readUnquotedName(StringReader param0) throws CommandSyntaxException {
        int var0 = param0.getCursor();

        while(param0.canRead() && isAllowedInUnquotedName(param0.peek())) {
            param0.skip();
        }

        if (param0.getCursor() == var0) {
            throw ERROR_INVALID_NODE.createWithContext(param0);
        } else {
            return param0.getString().substring(var0, param0.getCursor());
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static boolean isAllowedInUnquotedName(char param0) {
        return param0 != ' ' && param0 != '"' && param0 != '\'' && param0 != '[' && param0 != ']' && param0 != '.' && param0 != '{' && param0 != '}';
    }

    static Predicate<Tag> createTagPredicate(CompoundTag param0) {
        return param1 -> NbtUtils.compareNbt(param0, param1, true);
    }

    static class AllElementsNode implements NbtPathArgument.Node {
        public static final NbtPathArgument.AllElementsNode INSTANCE = new NbtPathArgument.AllElementsNode();

        private AllElementsNode() {
        }

        @Override
        public void getTag(Tag param0, List<Tag> param1) {
            if (param0 instanceof CollectionTag) {
                param1.addAll((CollectionTag)param0);
            }

        }

        @Override
        public void getOrCreateTag(Tag param0, Supplier<Tag> param1, List<Tag> param2) {
            if (param0 instanceof CollectionTag var0) {
                if (var0.isEmpty()) {
                    Tag var1 = param1.get();
                    if (var0.addTag(0, var1)) {
                        param2.add(var1);
                    }
                } else {
                    param2.addAll(var0);
                }
            }

        }

        @Override
        public Tag createPreferredParentTag() {
            return new ListTag();
        }

        @Override
        public int setTag(Tag param0, Supplier<Tag> param1) {
            if (!(param0 instanceof CollectionTag)) {
                return 0;
            } else {
                CollectionTag<?> var0 = (CollectionTag)param0;
                int var1 = var0.size();
                if (var1 == 0) {
                    var0.addTag(0, param1.get());
                    return 1;
                } else {
                    Tag var2 = param1.get();
                    int var3 = var1 - (int)var0.stream().filter(var2::equals).count();
                    if (var3 == 0) {
                        return 0;
                    } else {
                        var0.clear();
                        if (!var0.addTag(0, var2)) {
                            return 0;
                        } else {
                            for(int var4 = 1; var4 < var1; ++var4) {
                                var0.addTag(var4, param1.get());
                            }

                            return var3;
                        }
                    }
                }
            }
        }

        @Override
        public int removeTag(Tag param0) {
            if (param0 instanceof CollectionTag var0) {
                int var1 = var0.size();
                if (var1 > 0) {
                    var0.clear();
                    return var1;
                }
            }

            return 0;
        }
    }

    static class CompoundChildNode implements NbtPathArgument.Node {
        private final String name;

        public CompoundChildNode(String param0) {
            this.name = param0;
        }

        @Override
        public void getTag(Tag param0, List<Tag> param1) {
            if (param0 instanceof CompoundTag) {
                Tag var0 = ((CompoundTag)param0).get(this.name);
                if (var0 != null) {
                    param1.add(var0);
                }
            }

        }

        @Override
        public void getOrCreateTag(Tag param0, Supplier<Tag> param1, List<Tag> param2) {
            if (param0 instanceof CompoundTag var0) {
                Tag var1;
                if (var0.contains(this.name)) {
                    var1 = var0.get(this.name);
                } else {
                    var1 = param1.get();
                    var0.put(this.name, var1);
                }

                param2.add(var1);
            }

        }

        @Override
        public Tag createPreferredParentTag() {
            return new CompoundTag();
        }

        @Override
        public int setTag(Tag param0, Supplier<Tag> param1) {
            if (param0 instanceof CompoundTag var0) {
                Tag var1 = param1.get();
                Tag var2 = var0.put(this.name, var1);
                if (!var1.equals(var2)) {
                    return 1;
                }
            }

            return 0;
        }

        @Override
        public int removeTag(Tag param0) {
            if (param0 instanceof CompoundTag var0 && var0.contains(this.name)) {
                var0.remove(this.name);
                return 1;
            }

            return 0;
        }
    }

    static class IndexedElementNode implements NbtPathArgument.Node {
        private final int index;

        public IndexedElementNode(int param0) {
            this.index = param0;
        }

        @Override
        public void getTag(Tag param0, List<Tag> param1) {
            if (param0 instanceof CollectionTag var0) {
                int var1 = var0.size();
                int var2 = this.index < 0 ? var1 + this.index : this.index;
                if (0 <= var2 && var2 < var1) {
                    param1.add(var0.get(var2));
                }
            }

        }

        @Override
        public void getOrCreateTag(Tag param0, Supplier<Tag> param1, List<Tag> param2) {
            this.getTag(param0, param2);
        }

        @Override
        public Tag createPreferredParentTag() {
            return new ListTag();
        }

        @Override
        public int setTag(Tag param0, Supplier<Tag> param1) {
            if (param0 instanceof CollectionTag var0) {
                int var1 = var0.size();
                int var2 = this.index < 0 ? var1 + this.index : this.index;
                if (0 <= var2 && var2 < var1) {
                    Tag var3 = var0.get(var2);
                    Tag var4 = param1.get();
                    if (!var4.equals(var3) && var0.setTag(var2, var4)) {
                        return 1;
                    }
                }
            }

            return 0;
        }

        @Override
        public int removeTag(Tag param0) {
            if (param0 instanceof CollectionTag var0) {
                int var1 = var0.size();
                int var2 = this.index < 0 ? var1 + this.index : this.index;
                if (0 <= var2 && var2 < var1) {
                    var0.remove(var2);
                    return 1;
                }
            }

            return 0;
        }
    }

    static class MatchElementNode implements NbtPathArgument.Node {
        private final CompoundTag pattern;
        private final Predicate<Tag> predicate;

        public MatchElementNode(CompoundTag param0) {
            this.pattern = param0;
            this.predicate = NbtPathArgument.createTagPredicate(param0);
        }

        @Override
        public void getTag(Tag param0, List<Tag> param1) {
            if (param0 instanceof ListTag var0) {
                var0.stream().filter(this.predicate).forEach(param1::add);
            }

        }

        @Override
        public void getOrCreateTag(Tag param0, Supplier<Tag> param1, List<Tag> param2) {
            MutableBoolean var0 = new MutableBoolean();
            if (param0 instanceof ListTag var1) {
                var1.stream().filter(this.predicate).forEach(param2x -> {
                    param2.add(param2x);
                    var0.setTrue();
                });
                if (var0.isFalse()) {
                    CompoundTag var2 = this.pattern.copy();
                    var1.add(var2);
                    param2.add(var2);
                }
            }

        }

        @Override
        public Tag createPreferredParentTag() {
            return new ListTag();
        }

        @Override
        public int setTag(Tag param0, Supplier<Tag> param1) {
            int var0 = 0;
            if (param0 instanceof ListTag var1) {
                int var2 = var1.size();
                if (var2 == 0) {
                    var1.add(param1.get());
                    ++var0;
                } else {
                    for(int var3 = 0; var3 < var2; ++var3) {
                        Tag var4 = var1.get(var3);
                        if (this.predicate.test(var4)) {
                            Tag var5 = param1.get();
                            if (!var5.equals(var4) && var1.setTag(var3, var5)) {
                                ++var0;
                            }
                        }
                    }
                }
            }

            return var0;
        }

        @Override
        public int removeTag(Tag param0) {
            int var0 = 0;
            if (param0 instanceof ListTag var1) {
                for(int var2 = var1.size() - 1; var2 >= 0; --var2) {
                    if (this.predicate.test(var1.get(var2))) {
                        var1.remove(var2);
                        ++var0;
                    }
                }
            }

            return var0;
        }
    }

    static class MatchObjectNode implements NbtPathArgument.Node {
        private final String name;
        private final CompoundTag pattern;
        private final Predicate<Tag> predicate;

        public MatchObjectNode(String param0, CompoundTag param1) {
            this.name = param0;
            this.pattern = param1;
            this.predicate = NbtPathArgument.createTagPredicate(param1);
        }

        @Override
        public void getTag(Tag param0, List<Tag> param1) {
            if (param0 instanceof CompoundTag) {
                Tag var0 = ((CompoundTag)param0).get(this.name);
                if (this.predicate.test(var0)) {
                    param1.add(var0);
                }
            }

        }

        @Override
        public void getOrCreateTag(Tag param0, Supplier<Tag> param1, List<Tag> param2) {
            if (param0 instanceof CompoundTag var0) {
                Tag var1 = var0.get(this.name);
                if (var1 == null) {
                    Tag var6 = this.pattern.copy();
                    var0.put(this.name, var6);
                    param2.add(var6);
                } else if (this.predicate.test(var1)) {
                    param2.add(var1);
                }
            }

        }

        @Override
        public Tag createPreferredParentTag() {
            return new CompoundTag();
        }

        @Override
        public int setTag(Tag param0, Supplier<Tag> param1) {
            if (param0 instanceof CompoundTag var0) {
                Tag var1 = var0.get(this.name);
                if (this.predicate.test(var1)) {
                    Tag var2 = param1.get();
                    if (!var2.equals(var1)) {
                        var0.put(this.name, var2);
                        return 1;
                    }
                }
            }

            return 0;
        }

        @Override
        public int removeTag(Tag param0) {
            if (param0 instanceof CompoundTag var0) {
                Tag var1 = var0.get(this.name);
                if (this.predicate.test(var1)) {
                    var0.remove(this.name);
                    return 1;
                }
            }

            return 0;
        }
    }

    static class MatchRootObjectNode implements NbtPathArgument.Node {
        private final Predicate<Tag> predicate;

        public MatchRootObjectNode(CompoundTag param0) {
            this.predicate = NbtPathArgument.createTagPredicate(param0);
        }

        @Override
        public void getTag(Tag param0, List<Tag> param1) {
            if (param0 instanceof CompoundTag && this.predicate.test(param0)) {
                param1.add(param0);
            }

        }

        @Override
        public void getOrCreateTag(Tag param0, Supplier<Tag> param1, List<Tag> param2) {
            this.getTag(param0, param2);
        }

        @Override
        public Tag createPreferredParentTag() {
            return new CompoundTag();
        }

        @Override
        public int setTag(Tag param0, Supplier<Tag> param1) {
            return 0;
        }

        @Override
        public int removeTag(Tag param0) {
            return 0;
        }
    }

    public static class NbtPath {
        private final String original;
        private final Object2IntMap<NbtPathArgument.Node> nodeToOriginalPosition;
        private final NbtPathArgument.Node[] nodes;

        public NbtPath(String param0, NbtPathArgument.Node[] param1, Object2IntMap<NbtPathArgument.Node> param2) {
            this.original = param0;
            this.nodes = param1;
            this.nodeToOriginalPosition = param2;
        }

        public List<Tag> get(Tag param0) throws CommandSyntaxException {
            List<Tag> var0 = Collections.singletonList(param0);

            for(NbtPathArgument.Node var1 : this.nodes) {
                var0 = var1.get(var0);
                if (var0.isEmpty()) {
                    throw this.createNotFoundException(var1);
                }
            }

            return var0;
        }

        public int countMatching(Tag param0) {
            List<Tag> var0 = Collections.singletonList(param0);

            for(NbtPathArgument.Node var1 : this.nodes) {
                var0 = var1.get(var0);
                if (var0.isEmpty()) {
                    return 0;
                }
            }

            return var0.size();
        }

        private List<Tag> getOrCreateParents(Tag param0) throws CommandSyntaxException {
            List<Tag> var0 = Collections.singletonList(param0);

            for(int var1 = 0; var1 < this.nodes.length - 1; ++var1) {
                NbtPathArgument.Node var2 = this.nodes[var1];
                int var3 = var1 + 1;
                var0 = var2.getOrCreate(var0, this.nodes[var3]::createPreferredParentTag);
                if (var0.isEmpty()) {
                    throw this.createNotFoundException(var2);
                }
            }

            return var0;
        }

        public List<Tag> getOrCreate(Tag param0, Supplier<Tag> param1) throws CommandSyntaxException {
            List<Tag> var0 = this.getOrCreateParents(param0);
            NbtPathArgument.Node var1 = this.nodes[this.nodes.length - 1];
            return var1.getOrCreate(var0, param1);
        }

        private static int apply(List<Tag> param0, Function<Tag, Integer> param1) {
            return param0.stream().map(param1).reduce(0, (param0x, param1x) -> param0x + param1x);
        }

        public static boolean isTooDeep(Tag param0, int param1) {
            if (param1 >= 512) {
                return true;
            } else {
                if (param0 instanceof CompoundTag var0) {
                    for(String var1 : var0.getAllKeys()) {
                        Tag var2 = var0.get(var1);
                        if (var2 != null && isTooDeep(var2, param1 + 1)) {
                            return true;
                        }
                    }
                } else if (param0 instanceof ListTag) {
                    for(Tag var4 : (ListTag)param0) {
                        if (isTooDeep(var4, param1 + 1)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }

        public int set(Tag param0, Tag param1) throws CommandSyntaxException {
            if (isTooDeep(param1, this.estimatePathDepth())) {
                throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
            } else {
                Tag var0 = param1.copy();
                List<Tag> var1 = this.getOrCreateParents(param0);
                if (var1.isEmpty()) {
                    return 0;
                } else {
                    NbtPathArgument.Node var2 = this.nodes[this.nodes.length - 1];
                    MutableBoolean var3 = new MutableBoolean(false);
                    return apply(var1, param3 -> var2.setTag(param3, () -> {
                            if (var3.isFalse()) {
                                var3.setTrue();
                                return var0;
                            } else {
                                return var0.copy();
                            }
                        }));
                }
            }
        }

        private int estimatePathDepth() {
            return this.nodes.length;
        }

        public int insert(int param0, CompoundTag param1, List<Tag> param2) throws CommandSyntaxException {
            List<Tag> var0 = new ArrayList<>(param2.size());

            for(Tag var1 : param2) {
                Tag var2 = var1.copy();
                var0.add(var2);
                if (isTooDeep(var2, this.estimatePathDepth())) {
                    throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
                }
            }

            Collection<Tag> var3 = this.getOrCreate(param1, ListTag::new);
            int var4 = 0;
            boolean var5 = false;

            for(Tag var6 : var3) {
                if (!(var6 instanceof CollectionTag)) {
                    throw NbtPathArgument.ERROR_EXPECTED_LIST.create(var6);
                }

                CollectionTag<?> var7 = (CollectionTag)var6;
                boolean var8 = false;
                int var9 = param0 < 0 ? var7.size() + param0 + 1 : param0;

                for(Tag var10 : var0) {
                    try {
                        if (var7.addTag(var9, var5 ? var10.copy() : var10)) {
                            ++var9;
                            var8 = true;
                        }
                    } catch (IndexOutOfBoundsException var16) {
                        throw NbtPathArgument.ERROR_INVALID_INDEX.create(var9);
                    }
                }

                var5 = true;
                var4 += var8 ? 1 : 0;
            }

            return var4;
        }

        public int remove(Tag param0) {
            List<Tag> var0 = Collections.singletonList(param0);

            for(int var1 = 0; var1 < this.nodes.length - 1; ++var1) {
                var0 = this.nodes[var1].get(var0);
            }

            NbtPathArgument.Node var2 = this.nodes[this.nodes.length - 1];
            return apply(var0, var2::removeTag);
        }

        private CommandSyntaxException createNotFoundException(NbtPathArgument.Node param0) {
            int var0 = this.nodeToOriginalPosition.getInt(param0);
            return NbtPathArgument.ERROR_NOTHING_FOUND.create(this.original.substring(0, var0));
        }

        @Override
        public String toString() {
            return this.original;
        }

        public String asString() {
            return this.original;
        }
    }

    interface Node {
        void getTag(Tag var1, List<Tag> var2);

        void getOrCreateTag(Tag var1, Supplier<Tag> var2, List<Tag> var3);

        Tag createPreferredParentTag();

        int setTag(Tag var1, Supplier<Tag> var2);

        int removeTag(Tag var1);

        default List<Tag> get(List<Tag> param0) {
            return this.collect(param0, this::getTag);
        }

        default List<Tag> getOrCreate(List<Tag> param0, Supplier<Tag> param1) {
            return this.collect(param0, (param1x, param2) -> this.getOrCreateTag(param1x, param1, param2));
        }

        default List<Tag> collect(List<Tag> param0, BiConsumer<Tag, List<Tag>> param1) {
            List<Tag> var0 = Lists.newArrayList();

            for(Tag var1 : param0) {
                param1.accept(var1, var0);
            }

            return var0;
        }
    }
}
