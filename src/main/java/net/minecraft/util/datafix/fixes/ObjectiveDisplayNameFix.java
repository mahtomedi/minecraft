package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class ObjectiveDisplayNameFix extends DataFix {
    public ObjectiveDisplayNameFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.OBJECTIVE);
        return this.fixTypeEverywhereTyped(
            "ObjectiveDisplayNameFix",
            var0,
            param0 -> param0.update(
                    DSL.remainderFinder(),
                    param0x -> param0x.update(
                            "DisplayName",
                            param1 -> DataFixUtils.orElse(
                                    param1.asString()
                                        .map(param0xxx -> Component.Serializer.toJson(new TextComponent(param0xxx)))
                                        .map(param0x::createString)
                                        .result(),
                                    param1
                                )
                        )
                )
        );
    }
}
