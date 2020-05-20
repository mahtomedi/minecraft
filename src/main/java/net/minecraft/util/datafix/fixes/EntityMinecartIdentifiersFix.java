package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;

public class EntityMinecartIdentifiersFix extends DataFix {
    private static final List<String> MINECART_BY_ID = Lists.newArrayList("MinecartRideable", "MinecartChest", "MinecartFurnace");

    public EntityMinecartIdentifiersFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        TaggedChoiceType<String> var0 = this.getInputSchema().findChoiceType(References.ENTITY);
        TaggedChoiceType<String> var1 = this.getOutputSchema().findChoiceType(References.ENTITY);
        return this.fixTypeEverywhere(
            "EntityMinecartIdentifiersFix",
            var0,
            var1,
            param2 -> param3 -> {
                    if (!Objects.equals(param3.getFirst(), "Minecart")) {
                        return param3;
                    } else {
                        Typed<? extends Pair<String, ?>> var0x = var0.point(param2, "Minecart", param3.getSecond()).orElseThrow(IllegalStateException::new);
                        Dynamic<?> var1x = var0x.getOrCreate(DSL.remainderFinder());
                        int var2x = var1x.get("Type").asInt(0);
                        String var3;
                        if (var2x > 0 && var2x < MINECART_BY_ID.size()) {
                            var3 = MINECART_BY_ID.get(var2x);
                        } else {
                            var3 = "MinecartRideable";
                        }
    
                        return Pair.of(
                            var3,
                            var0x.write()
                                .map(param2x -> var1.types().get(var3).read(param2x))
                                .result()
                                .orElseThrow(() -> new IllegalStateException("Could not read the new minecart."))
                        );
                    }
                }
        );
    }
}
