package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class AddFlagIfNotPresentFix extends DataFix {
    private final String name;
    private final boolean flagValue;
    private final String flagKey;
    private final TypeReference typeReference;

    public AddFlagIfNotPresentFix(Schema param0, TypeReference param1, String param2, boolean param3) {
        super(param0, true);
        this.flagValue = param3;
        this.flagKey = param2;
        this.name = "AddFlagIfNotPresentFix_" + this.flagKey + "=" + this.flagValue + " for " + param0.getVersionKey();
        this.typeReference = param1;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(this.typeReference);
        return this.fixTypeEverywhereTyped(
            this.name,
            var0,
            param0 -> param0.update(
                    DSL.remainderFinder(),
                    param0x -> param0x.set(
                            this.flagKey, DataFixUtils.orElseGet(param0x.get(this.flagKey).result(), () -> param0x.createBoolean(this.flagValue))
                        )
                )
        );
    }
}
