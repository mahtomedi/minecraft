package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.Mth;

public class VillagerRebuildLevelAndXpFix extends DataFix {
    private static final int TRADES_PER_LEVEL = 2;
    private static final int[] LEVEL_XP_THRESHOLDS = new int[]{0, 10, 50, 100, 150};

    public static int getMinXpPerLevel(int param0) {
        return LEVEL_XP_THRESHOLDS[Mth.clamp(param0 - 1, 0, LEVEL_XP_THRESHOLDS.length - 1)];
    }

    public VillagerRebuildLevelAndXpFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getChoiceType(References.ENTITY, "minecraft:villager");
        OpticFinder<?> var1 = DSL.namedChoice("minecraft:villager", var0);
        OpticFinder<?> var2 = var0.findField("Offers");
        Type<?> var3 = var2.type();
        OpticFinder<?> var4 = var3.findField("Recipes");
        ListType<?> var5 = (ListType)var4.type();
        OpticFinder<?> var6 = var5.getElement().finder();
        return this.fixTypeEverywhereTyped(
            "Villager level and xp rebuild",
            this.getInputSchema().getType(References.ENTITY),
            param5 -> param5.updateTyped(
                    var1,
                    var0,
                    param3x -> {
                        Dynamic<?> var0x = param3x.get(DSL.remainderFinder());
                        int var1x = var0x.get("VillagerData").get("level").asInt(0);
                        Typed<?> var2x = param3x;
                        if (var1x == 0 || var1x == 1) {
                            int var3x = param3x.getOptionalTyped(var2)
                                .flatMap(param1x -> param1x.getOptionalTyped(var4))
                                .map(param1x -> param1x.getAllTyped(var6).size())
                                .orElse(0);
                            var1x = Mth.clamp(var3x / 2, 1, 5);
                            if (var1x > 1) {
                                var2x = addLevel(param3x, var1x);
                            }
                        }
        
                        Optional<Number> var4x = var0x.get("Xp").asNumber().result();
                        if (var4x.isEmpty()) {
                            var2x = addXpFromLevel(var2x, var1x);
                        }
        
                        return var2x;
                    }
                )
        );
    }

    private static Typed<?> addLevel(Typed<?> param0, int param1) {
        return param0.update(DSL.remainderFinder(), param1x -> param1x.update("VillagerData", param1xx -> param1xx.set("level", param1xx.createInt(param1))));
    }

    private static Typed<?> addXpFromLevel(Typed<?> param0, int param1) {
        int var0 = getMinXpPerLevel(param1);
        return param0.update(DSL.remainderFinder(), param1x -> param1x.set("Xp", param1x.createInt(var0)));
    }
}
