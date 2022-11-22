package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public class CollectFields extends CollectToTag {
    private int fieldsToGetCount;
    private final Set<TagType<?>> wantedTypes;
    private final Deque<FieldTree> stack = new ArrayDeque();

    public CollectFields(FieldSelector... param0) {
        this.fieldsToGetCount = param0.length;
        Builder<TagType<?>> var0 = ImmutableSet.builder();
        FieldTree var1 = FieldTree.createRoot();

        for(FieldSelector var2 : param0) {
            var1.addEntry(var2);
            var0.add(var2.type());
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
        FieldTree var0 = (FieldTree)this.stack.element();
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
        FieldTree var0 = (FieldTree)this.stack.element();
        if (this.depth() > var0.depth()) {
            return super.visitEntry(param0, param1);
        } else if (var0.selectedFields().remove(param1, param0)) {
            --this.fieldsToGetCount;
            return super.visitEntry(param0, param1);
        } else {
            if (param0 == CompoundTag.TYPE) {
                FieldTree var1 = (FieldTree)var0.fieldsToRecurse().get(param1);
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
        if (this.depth() == ((FieldTree)this.stack.element()).depth()) {
            this.stack.pop();
        }

        return super.visitContainerEnd();
    }

    public int getMissingFieldCount() {
        return this.fieldsToGetCount;
    }
}
