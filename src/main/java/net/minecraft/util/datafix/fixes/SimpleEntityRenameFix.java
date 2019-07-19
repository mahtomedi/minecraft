package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;

public abstract class SimpleEntityRenameFix extends EntityRenameFix {
    public SimpleEntityRenameFix(String param0, Schema param1, boolean param2) {
        super(param0, param1, param2);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String param0, Typed<?> param1) {
        Pair<String, Dynamic<?>> var0 = this.getNewNameAndTag(param0, param1.getOrCreate(DSL.remainderFinder()));
        return Pair.of(var0.getFirst(), param1.set(DSL.remainderFinder(), var0.getSecond()));
    }

    protected abstract Pair<String, Dynamic<?>> getNewNameAndTag(String var1, Dynamic<?> var2);
}
