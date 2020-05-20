package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class ItemRenameFix extends DataFix {
    private final String name;

    public ItemRenameFix(Schema param0, String param1) {
        super(param0, false);
        this.name = param1;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<Pair<String, String>> var0 = DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(this.getInputSchema().getType(References.ITEM_NAME), var0)) {
            throw new IllegalStateException("item name type is not what was expected.");
        } else {
            return this.fixTypeEverywhere(this.name, var0, param0 -> param0x -> param0x.mapSecond(this::fixItem));
        }
    }

    protected abstract String fixItem(String var1);

    public static DataFix create(Schema param0, String param1, final Function<String, String> param2) {
        return new ItemRenameFix(param0, param1) {
            @Override
            protected String fixItem(String param0) {
                return param2.apply(param0);
            }
        };
    }
}
