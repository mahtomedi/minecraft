package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Optional;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ObjectiveRenderTypeFix extends DataFix {
    public ObjectiveRenderTypeFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    private static ObjectiveCriteria.RenderType getRenderType(String param0) {
        return param0.equals("health") ? ObjectiveCriteria.RenderType.HEARTS : ObjectiveCriteria.RenderType.INTEGER;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.OBJECTIVE);
        return this.fixTypeEverywhereTyped("ObjectiveRenderTypeFix", var0, param0 -> param0.update(DSL.remainderFinder(), param0x -> {
                Optional<String> var0x = param0x.get("RenderType").asString().result();
                if (!var0x.isPresent()) {
                    String var1x = param0x.get("CriteriaName").asString("");
                    ObjectiveCriteria.RenderType var2 = getRenderType(var1x);
                    return param0x.set("RenderType", param0x.createString(var2.getId()));
                } else {
                    return param0x;
                }
            }));
    }
}
