package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public class CollectFields extends CollectToTag {
    private int fieldsToGetCount;
    private final Set<TagType<?>> wantedTypes;
    private final Deque<CollectFields.StackFrame> stack = new ArrayDeque<>();

    public CollectFields(CollectFields.WantedField... param0) {
        this.fieldsToGetCount = param0.length;
        Builder<TagType<?>> var0 = ImmutableSet.builder();
        CollectFields.StackFrame var1 = new CollectFields.StackFrame(1);

        for(CollectFields.WantedField var2 : param0) {
            var1.addEntry(var2);
            var0.add(var2.type);
        }

        this.stack.push(var1);
        var0.add(CompoundTag.TYPE);
        this.wantedTypes = var0.build();
    }

    @Override
    public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> param0) {
        return param0 != CompoundTag.TYPE ? StreamTagVisitor.ValueResult.HALT : super.visitRootEntry(param0);
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> param0) {
        CollectFields.StackFrame var0 = this.stack.element();
        if (this.depth() > var0.depth()) {
            return super.visitEntry(param0);
        } else if (this.fieldsToGetCount <= 0) {
            return StreamTagVisitor.EntryResult.HALT;
        } else {
            return !this.wantedTypes.contains(param0) ? StreamTagVisitor.EntryResult.SKIP : super.visitEntry(param0);
        }
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> param0, String param1) {
        CollectFields.StackFrame var0 = this.stack.element();
        if (this.depth() > var0.depth()) {
            return super.visitEntry(param0, param1);
        } else if (var0.fieldsToGet.remove(param1, param0)) {
            --this.fieldsToGetCount;
            return super.visitEntry(param0, param1);
        } else {
            if (param0 == CompoundTag.TYPE) {
                CollectFields.StackFrame var1 = var0.fieldsToRecurse.get(param1);
                if (var1 != null) {
                    this.stack.push(var1);
                    return super.visitEntry(param0, param1);
                }
            }

            return StreamTagVisitor.EntryResult.SKIP;
        }
    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        if (this.depth() == this.stack.element().depth()) {
            this.stack.pop();
        }

        return super.visitContainerEnd();
    }

    public int getMissingFieldCount() {
        return this.fieldsToGetCount;
    }

    static record StackFrame(int depth, Map<String, TagType<?>> fieldsToGet, Map<String, CollectFields.StackFrame> fieldsToRecurse) {
        public StackFrame(int param0) {
            this(param0, new HashMap<>(), new HashMap<>());
        }

        public void addEntry(CollectFields.WantedField param0) {
            if (this.depth <= param0.path.size()) {
                this.fieldsToRecurse.computeIfAbsent(param0.path.get(this.depth - 1), param0x -> new CollectFields.StackFrame(this.depth + 1)).addEntry(param0);
            } else {
                this.fieldsToGet.put(param0.name, param0.type);
            }

        }
    }

    public static record WantedField(List<String> path, TagType<?> type, String name) {
        public WantedField(TagType<?> param0, String param1) {
            this(List.of(), param0, param1);
        }

        public WantedField(String param0, TagType<?> param1, String param2) {
            this(List.of(param0), param1, param2);
        }

        public WantedField(String param0, String param1, TagType<?> param2, String param3) {
            this(List.of(param0, param1), param2, param3);
        }
    }
}
