package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FeatureFlagRemoveFix extends DataFix {
    private final String name;
    private final Set<String> flagsToRemove;

    public FeatureFlagRemoveFix(Schema param0, String param1, Set<String> param2) {
        super(param0, false);
        this.name = param1;
        this.flagsToRemove = param2;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            this.name, this.getInputSchema().getType(References.LEVEL), param0 -> param0.update(DSL.remainderFinder(), this::fixTag)
        );
    }

    private <T> Dynamic<T> fixTag(Dynamic<T> param0) {
        List<Dynamic<T>> var0 = param0.get("removed_features").asStream().collect(Collectors.toCollection(ArrayList::new));
        Dynamic<T> var1 = param0.update(
            "enabled_features", param2 -> DataFixUtils.orElse(param2.asStreamOpt().result().map(param2x -> param2x.filter(param2xx -> {
                        Optional<String> var0x = param2xx.asString().result();
                        if (var0x.isEmpty()) {
                            return true;
                        } else {
                            boolean var1x = this.flagsToRemove.contains(var0x.get());
                            if (var1x) {
                                var0.add(param0.createString(var0x.get()));
                            }
    
                            return !var1x;
                        }
                    })).map(param0::createList), param2)
        );
        if (!var0.isEmpty()) {
            var1 = var1.set("removed_features", param0.createList(var0.stream()));
        }

        return var1;
    }
}
