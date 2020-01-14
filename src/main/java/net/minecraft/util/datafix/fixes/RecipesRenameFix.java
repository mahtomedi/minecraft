package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;

public class RecipesRenameFix extends DataFix {
    private final String name;
    private final Function<String, String> renamer;

    public RecipesRenameFix(Schema param0, boolean param1, String param2, Function<String, String> param3) {
        super(param0, param1);
        this.name = param2;
        this.renamer = param3;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, String>> var0 = DSL.named(References.RECIPE.typeName(), DSL.namespacedString());
        if (!Objects.equals(var0, this.getInputSchema().getType(References.RECIPE))) {
            throw new IllegalStateException("Recipe type is not what was expected.");
        } else {
            return this.fixTypeEverywhere(this.name, var0, param0 -> param0x -> param0x.mapSecond(this.renamer));
        }
    }
}
