package net.minecraft.server.packs;

import java.util.Map;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public class BuiltInMetadata {
    private static final BuiltInMetadata EMPTY = new BuiltInMetadata(Map.of());
    private final Map<MetadataSectionSerializer<?>, ?> values;

    private BuiltInMetadata(Map<MetadataSectionSerializer<?>, ?> param0) {
        this.values = param0;
    }

    public <T> T get(MetadataSectionSerializer<T> param0) {
        return (T)this.values.get(param0);
    }

    public static BuiltInMetadata of() {
        return EMPTY;
    }

    public static <T> BuiltInMetadata of(MetadataSectionSerializer<T> param0, T param1) {
        return new BuiltInMetadata(Map.of(param0, param1));
    }

    public static <T1, T2> BuiltInMetadata of(MetadataSectionSerializer<T1> param0, T1 param1, MetadataSectionSerializer<T2> param2, T2 param3) {
        return new BuiltInMetadata(Map.of(param0, param1, param2, param3));
    }
}
