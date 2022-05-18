package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class FilteredBooksFix extends ItemStackTagFix {
    public FilteredBooksFix(Schema param0) {
        super(param0, "Remove filtered text from books", param0x -> param0x.equals("minecraft:writable_book") || param0x.equals("minecraft:written_book"));
    }

    @Override
    protected <T> Dynamic<T> fixItemStackTag(Dynamic<T> param0) {
        return param0.remove("filtered_title").remove("filtered_pages");
    }
}
