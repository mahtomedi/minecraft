package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public class SkipFields extends CollectToTag {
    private final Deque<FieldTree> stack = new ArrayDeque<>();

    public SkipFields(FieldSelector... param0) {
        FieldTree var0 = FieldTree.createRoot();

        for(FieldSelector var1 : param0) {
            var0.addEntry(var1);
        }

        this.stack.push(var0);
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> param0, String param1) {
        FieldTree var0 = this.stack.element();
        if (var0.isSelected(param0, param1)) {
            return StreamTagVisitor.EntryResult.SKIP;
        } else {
            if (param0 == CompoundTag.TYPE) {
                FieldTree var1 = var0.fieldsToRecurse().get(param1);
                if (var1 != null) {
                    this.stack.push(var1);
                }
            }

            return super.visitEntry(param0, param1);
        }
    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        if (this.depth() == this.stack.element().depth()) {
            this.stack.pop();
        }

        return super.visitContainerEnd();
    }
}
