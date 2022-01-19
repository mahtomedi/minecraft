package net.minecraft.nbt.visitors;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.TagType;

public record FieldTree(int depth, Map<String, TagType<?>> selectedFields, Map<String, FieldTree> fieldsToRecurse) {
    private FieldTree(int param0) {
        this(param0, new HashMap<>(), new HashMap<>());
    }

    public static FieldTree createRoot() {
        return new FieldTree(1);
    }

    public void addEntry(FieldSelector param0) {
        if (this.depth <= param0.path().size()) {
            this.fieldsToRecurse.computeIfAbsent(param0.path().get(this.depth - 1), param0x -> new FieldTree(this.depth + 1)).addEntry(param0);
        } else {
            this.selectedFields.put(param0.name(), param0.type());
        }

    }

    public boolean isSelected(TagType<?> param0, String param1) {
        return param0.equals(this.selectedFields().get(param1));
    }
}
