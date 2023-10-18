package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;

public class PrimedTntBlockStateFixer extends NamedEntityWriteReadFix {
    public PrimedTntBlockStateFixer(Schema param0) {
        super(param0, true, "PrimedTnt BlockState fixer", References.ENTITY, "minecraft:tnt");
    }

    private static <T> Dynamic<T> renameFuse(Dynamic<T> param0) {
        Optional<Dynamic<T>> var0 = param0.get("Fuse").get().result();
        return var0.isPresent() ? param0.set("fuse", var0.get()) : param0;
    }

    private static <T> Dynamic<T> insertBlockState(Dynamic<T> param0) {
        return param0.set("block_state", param0.createMap(Map.of(param0.createString("Name"), param0.createString("minecraft:tnt"))));
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> param0) {
        return renameFuse(insertBlockState(param0));
    }
}
