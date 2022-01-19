package net.minecraft.nbt.visitors;

import java.util.List;
import net.minecraft.nbt.TagType;

public record FieldSelector(List<String> path, TagType<?> type, String name) {
    public FieldSelector(TagType<?> param0, String param1) {
        this(List.of(), param0, param1);
    }

    public FieldSelector(String param0, TagType<?> param1, String param2) {
        this(List.of(param0), param1, param2);
    }

    public FieldSelector(String param0, String param1, TagType<?> param2, String param3) {
        this(List.of(param0, param1), param2, param3);
    }
}
