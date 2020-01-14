package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.function.Function;

public class AdvancementsRenameFix extends DataFix {
    private final String name;
    private final Function<String, String> renamer;

    public AdvancementsRenameFix(Schema param0, boolean param1, String param2, Function<String, String> param3) {
        super(param0, param1);
        this.name = param2;
        this.renamer = param3;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            this.name,
            this.getInputSchema().getType(References.ADVANCEMENTS),
            param0 -> param0.update(DSL.remainderFinder(), param0x -> param0x.updateMapValues(param1 -> {
                        String var0 = param1.getFirst().asString("");
                        return param1.mapFirst(param2 -> param0x.createString(this.renamer.apply(var0)));
                    }))
        );
    }
}
