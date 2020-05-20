package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList.CompoundListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class NewVillageFix extends DataFix {
    public NewVillageFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        CompoundListType<String, ?> var0 = DSL.compoundList(DSL.string(), this.getInputSchema().getType(References.STRUCTURE_FEATURE));
        OpticFinder<? extends List<? extends Pair<String, ?>>> var1 = var0.finder();
        return this.cap(var0);
    }

    private <SF> TypeRewriteRule cap(CompoundListType<String, SF> param0) {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        Type<?> var1 = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
        OpticFinder<?> var2 = var0.findField("Level");
        OpticFinder<?> var3 = var2.type().findField("Structures");
        OpticFinder<?> var4 = var3.type().findField("Starts");
        OpticFinder<List<Pair<String, SF>>> var5 = param0.finder();
        return TypeRewriteRule.seq(
            this.fixTypeEverywhereTyped(
                "NewVillageFix",
                var0,
                param4 -> param4.updateTyped(
                        var2,
                        param3x -> param3x.updateTyped(
                                var3,
                                param2x -> param2x.updateTyped(
                                            var4,
                                            param1x -> param1x.update(
                                                    var5,
                                                    param0x -> param0x.stream()
                                                            .filter(param0xx -> !Objects.equals(param0xx.getFirst(), "Village"))
                                                            .map(
                                                                param0xx -> param0xx.mapFirst(
                                                                        param0xxx -> param0xxx.equals("New_Village") ? "Village" : param0xxx
                                                                    )
                                                            )
                                                            .collect(Collectors.toList())
                                                )
                                        )
                                        .update(
                                            DSL.remainderFinder(),
                                            param0x -> param0x.update(
                                                    "References",
                                                    param0xx -> {
                                                        Optional<? extends Dynamic<?>> var0x = param0xx.get("New_Village").result();
                                                        return DataFixUtils.orElse(
                                                                var0x.map(param1x -> param0xx.remove("New_Village").set("Village", param1x)), param0xx
                                                            )
                                                            .remove("Village");
                                                    }
                                                )
                                        )
                            )
                    )
            ),
            this.fixTypeEverywhereTyped(
                "NewVillageStartFix",
                var1,
                param0x -> param0x.update(
                        DSL.remainderFinder(),
                        param0xx -> param0xx.update(
                                "id",
                                param0xxx -> Objects.equals(NamespacedSchema.ensureNamespaced(param0xxx.asString("")), "minecraft:new_village")
                                        ? param0xxx.createString("minecraft:village")
                                        : param0xxx
                            )
                    )
            )
        );
    }
}
