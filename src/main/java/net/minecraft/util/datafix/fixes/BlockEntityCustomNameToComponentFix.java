package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;
import java.util.Optional;

public class BlockEntityCustomNameToComponentFix extends DataFix {
    public BlockEntityCustomNameToComponentFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<String> var0 = DSL.fieldFinder("id", DSL.namespacedString());
        return this.fixTypeEverywhereTyped(
            "BlockEntityCustomNameToComponentFix",
            this.getInputSchema().getType(References.BLOCK_ENTITY),
            param1 -> param1.update(
                    DSL.remainderFinder(),
                    param2 -> {
                        Optional<String> var0x = param1.getOptional(var0);
                        return var0x.isPresent() && Objects.equals(var0x.get(), "minecraft:command_block")
                            ? param2
                            : EntityCustomNameToComponentFix.fixTagCustomName(param2);
                    }
                )
        );
    }
}
