package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Locale;
import java.util.Optional;

public class OptionsLowerCaseLanguageFix extends DataFix {
    public OptionsLowerCaseLanguageFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "OptionsLowerCaseLanguageFix", this.getInputSchema().getType(References.OPTIONS), param0 -> param0.update(DSL.remainderFinder(), param0x -> {
                    Optional<String> var0x = param0x.get("lang").asString().result();
                    return var0x.isPresent() ? param0x.set("lang", param0x.createString(((String)var0x.get()).toLowerCase(Locale.ROOT))) : param0x;
                })
        );
    }
}
