package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
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
        Type<Pair<String, Dynamic<?>>> var0 = DSL.named(References.OBJECTIVE.typeName(), DSL.remainderType());
        if (!Objects.equals(var0, this.getInputSchema().getType(References.OBJECTIVE))) {
            throw new IllegalStateException("Objective type is not what was expected.");
        } else {
            return this.fixTypeEverywhere("ObjectiveRenderTypeFix", var0, param0 -> param0x -> param0x.mapSecond(param0xx -> {
                        Optional<String> var0x = param0xx.get("RenderType").asString();
                        if (!var0x.isPresent()) {
                            String var1x = param0xx.get("CriteriaName").asString("");
                            ObjectiveCriteria.RenderType var2 = getRenderType(var1x);
                            return param0xx.set("RenderType", param0xx.createString(var2.getId()));
                        } else {
                            return param0xx;
                        }
                    }));
        }
    }
}
