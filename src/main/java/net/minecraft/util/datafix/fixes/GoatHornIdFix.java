package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class GoatHornIdFix extends DataFix {
    private static final String[] INSTRUMENTS = new String[]{
        "minecraft:ponder_goat_horn",
        "minecraft:sing_goat_horn",
        "minecraft:seek_goat_horn",
        "minecraft:feel_goat_horn",
        "minecraft:admire_goat_horn",
        "minecraft:call_goat_horn",
        "minecraft:yearn_goat_horn",
        "minecraft:dream_goat_horn"
    };

    public GoatHornIdFix(Schema param0) {
        super(param0, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> var2 = var0.findField("tag");
        return this.fixTypeEverywhereTyped(
            "GoatHornIdFix",
            var0,
            param2 -> {
                Optional<Pair<String, String>> var0x = param2.getOptional(var1);
                return var0x.isPresent() && Objects.equals(var0x.get().getSecond(), "minecraft:goat_horn")
                    ? param2.updateTyped(var2, param0x -> param0x.update(DSL.remainderFinder(), param0xx -> {
                            int var0xx = param0xx.get("SoundVariant").asInt(0);
                            String var1x = INSTRUMENTS[var0xx >= 0 && var0xx < INSTRUMENTS.length ? var0xx : 0];
                            return param0xx.remove("SoundVariant").set("instrument", param0xx.createString(var1x));
                        }))
                    : param2;
            }
        );
    }
}
