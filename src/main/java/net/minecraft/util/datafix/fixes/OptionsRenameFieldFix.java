package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsRenameFieldFix extends DataFix {
    private final String fixName;
    private final String fieldFrom;
    private final String fieldTo;

    public OptionsRenameFieldFix(Schema param0, boolean param1, String param2, String param3, String param4) {
        super(param0, param1);
        this.fixName = param2;
        this.fieldFrom = param3;
        this.fieldTo = param4;
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            this.fixName,
            this.getInputSchema().getType(References.OPTIONS),
            param0 -> param0.update(
                    DSL.remainderFinder(),
                    param0x -> DataFixUtils.orElse(
                            param0x.get(this.fieldFrom).result().map(param1 -> param0x.set(this.fieldTo, param1).remove(this.fieldFrom)), param0x
                        )
                )
        );
    }
}
