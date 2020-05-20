package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class BlockRenameFix extends DataFix {
    private final String name;

    public BlockRenameFix(Schema param0, String param1) {
        super(param0, false);
        this.name = param1;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.BLOCK_NAME);
        Type<Pair<String, String>> var1 = DSL.named(References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(var0, var1)) {
            throw new IllegalStateException("block type is not what was expected.");
        } else {
            TypeRewriteRule var2 = this.fixTypeEverywhere(this.name + " for block", var1, param0 -> param0x -> param0x.mapSecond(this::fixBlock));
            TypeRewriteRule var3 = this.fixTypeEverywhereTyped(
                this.name + " for block_state",
                this.getInputSchema().getType(References.BLOCK_STATE),
                param0 -> param0.update(DSL.remainderFinder(), param0x -> {
                        Optional<String> var0x = param0x.get("Name").asString().result();
                        return var0x.isPresent() ? param0x.set("Name", param0x.createString(this.fixBlock(var0x.get()))) : param0x;
                    })
            );
            return TypeRewriteRule.seq(var2, var3);
        }
    }

    protected abstract String fixBlock(String var1);

    public static DataFix create(Schema param0, String param1, final Function<String, String> param2) {
        return new BlockRenameFix(param0, param1) {
            @Override
            protected String fixBlock(String param0) {
                return param2.apply(param0);
            }
        };
    }
}
