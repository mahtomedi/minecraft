package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class TeamDisplayNameFix extends DataFix {
    public TeamDisplayNameFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> var0 = DSL.named(References.TEAM.typeName(), DSL.remainderType());
        if (!Objects.equals(var0, this.getInputSchema().getType(References.TEAM))) {
            throw new IllegalStateException("Team type is not what was expected.");
        } else {
            return this.fixTypeEverywhere(
                "TeamDisplayNameFix",
                var0,
                param0 -> param0x -> param0x.mapSecond(
                            param0xx -> param0xx.update(
                                    "DisplayName",
                                    param1 -> DataFixUtils.orElse(
                                            param1.asString()
                                                .map(param0xxxx -> Component.Serializer.toJson(new TextComponent(param0xxxx)))
                                                .map(param0xx::createString)
                                                .result(),
                                            param1
                                        )
                                )
                        )
            );
        }
    }
}
