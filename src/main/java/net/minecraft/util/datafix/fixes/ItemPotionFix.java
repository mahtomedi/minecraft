package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;

public class ItemPotionFix extends DataFix {
    private static final String[] POTIONS = DataFixUtils.make(new String[128], param0 -> {
        param0[0] = "minecraft:water";
        param0[1] = "minecraft:regeneration";
        param0[2] = "minecraft:swiftness";
        param0[3] = "minecraft:fire_resistance";
        param0[4] = "minecraft:poison";
        param0[5] = "minecraft:healing";
        param0[6] = "minecraft:night_vision";
        param0[7] = null;
        param0[8] = "minecraft:weakness";
        param0[9] = "minecraft:strength";
        param0[10] = "minecraft:slowness";
        param0[11] = "minecraft:leaping";
        param0[12] = "minecraft:harming";
        param0[13] = "minecraft:water_breathing";
        param0[14] = "minecraft:invisibility";
        param0[15] = null;
        param0[16] = "minecraft:awkward";
        param0[17] = "minecraft:regeneration";
        param0[18] = "minecraft:swiftness";
        param0[19] = "minecraft:fire_resistance";
        param0[20] = "minecraft:poison";
        param0[21] = "minecraft:healing";
        param0[22] = "minecraft:night_vision";
        param0[23] = null;
        param0[24] = "minecraft:weakness";
        param0[25] = "minecraft:strength";
        param0[26] = "minecraft:slowness";
        param0[27] = "minecraft:leaping";
        param0[28] = "minecraft:harming";
        param0[29] = "minecraft:water_breathing";
        param0[30] = "minecraft:invisibility";
        param0[31] = null;
        param0[32] = "minecraft:thick";
        param0[33] = "minecraft:strong_regeneration";
        param0[34] = "minecraft:strong_swiftness";
        param0[35] = "minecraft:fire_resistance";
        param0[36] = "minecraft:strong_poison";
        param0[37] = "minecraft:strong_healing";
        param0[38] = "minecraft:night_vision";
        param0[39] = null;
        param0[40] = "minecraft:weakness";
        param0[41] = "minecraft:strong_strength";
        param0[42] = "minecraft:slowness";
        param0[43] = "minecraft:strong_leaping";
        param0[44] = "minecraft:strong_harming";
        param0[45] = "minecraft:water_breathing";
        param0[46] = "minecraft:invisibility";
        param0[47] = null;
        param0[48] = null;
        param0[49] = "minecraft:strong_regeneration";
        param0[50] = "minecraft:strong_swiftness";
        param0[51] = "minecraft:fire_resistance";
        param0[52] = "minecraft:strong_poison";
        param0[53] = "minecraft:strong_healing";
        param0[54] = "minecraft:night_vision";
        param0[55] = null;
        param0[56] = "minecraft:weakness";
        param0[57] = "minecraft:strong_strength";
        param0[58] = "minecraft:slowness";
        param0[59] = "minecraft:strong_leaping";
        param0[60] = "minecraft:strong_harming";
        param0[61] = "minecraft:water_breathing";
        param0[62] = "minecraft:invisibility";
        param0[63] = null;
        param0[64] = "minecraft:mundane";
        param0[65] = "minecraft:long_regeneration";
        param0[66] = "minecraft:long_swiftness";
        param0[67] = "minecraft:long_fire_resistance";
        param0[68] = "minecraft:long_poison";
        param0[69] = "minecraft:healing";
        param0[70] = "minecraft:long_night_vision";
        param0[71] = null;
        param0[72] = "minecraft:long_weakness";
        param0[73] = "minecraft:long_strength";
        param0[74] = "minecraft:long_slowness";
        param0[75] = "minecraft:long_leaping";
        param0[76] = "minecraft:harming";
        param0[77] = "minecraft:long_water_breathing";
        param0[78] = "minecraft:long_invisibility";
        param0[79] = null;
        param0[80] = "minecraft:awkward";
        param0[81] = "minecraft:long_regeneration";
        param0[82] = "minecraft:long_swiftness";
        param0[83] = "minecraft:long_fire_resistance";
        param0[84] = "minecraft:long_poison";
        param0[85] = "minecraft:healing";
        param0[86] = "minecraft:long_night_vision";
        param0[87] = null;
        param0[88] = "minecraft:long_weakness";
        param0[89] = "minecraft:long_strength";
        param0[90] = "minecraft:long_slowness";
        param0[91] = "minecraft:long_leaping";
        param0[92] = "minecraft:harming";
        param0[93] = "minecraft:long_water_breathing";
        param0[94] = "minecraft:long_invisibility";
        param0[95] = null;
        param0[96] = "minecraft:thick";
        param0[97] = "minecraft:regeneration";
        param0[98] = "minecraft:swiftness";
        param0[99] = "minecraft:long_fire_resistance";
        param0[100] = "minecraft:poison";
        param0[101] = "minecraft:strong_healing";
        param0[102] = "minecraft:long_night_vision";
        param0[103] = null;
        param0[104] = "minecraft:long_weakness";
        param0[105] = "minecraft:strength";
        param0[106] = "minecraft:long_slowness";
        param0[107] = "minecraft:leaping";
        param0[108] = "minecraft:strong_harming";
        param0[109] = "minecraft:long_water_breathing";
        param0[110] = "minecraft:long_invisibility";
        param0[111] = null;
        param0[112] = null;
        param0[113] = "minecraft:regeneration";
        param0[114] = "minecraft:swiftness";
        param0[115] = "minecraft:long_fire_resistance";
        param0[116] = "minecraft:poison";
        param0[117] = "minecraft:strong_healing";
        param0[118] = "minecraft:long_night_vision";
        param0[119] = null;
        param0[120] = "minecraft:long_weakness";
        param0[121] = "minecraft:strength";
        param0[122] = "minecraft:long_slowness";
        param0[123] = "minecraft:leaping";
        param0[124] = "minecraft:strong_harming";
        param0[125] = "minecraft:long_water_breathing";
        param0[126] = "minecraft:long_invisibility";
        param0[127] = null;
    });

    public ItemPotionFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), DSL.namespacedString()));
        OpticFinder<?> var2 = var0.findField("tag");
        return this.fixTypeEverywhereTyped("ItemPotionFix", var0, param2 -> {
            Optional<Pair<String, String>> var0x = param2.getOptional(var1);
            if (var0x.isPresent() && Objects.equals(var0x.get().getSecond(), "minecraft:potion")) {
                Dynamic<?> var1x = param2.get(DSL.remainderFinder());
                Optional<? extends Typed<?>> var2x = param2.getOptionalTyped(var2);
                short var3x = var1x.get("Damage").asShort((short)0);
                if (var2x.isPresent()) {
                    Typed<?> var4 = param2;
                    Dynamic<?> var5 = var2x.get().get(DSL.remainderFinder());
                    Optional<String> var6 = var5.get("Potion").asString();
                    if (!var6.isPresent()) {
                        String var7 = POTIONS[var3x & 127];
                        Typed<?> var8 = var2x.get().set(DSL.remainderFinder(), var5.set("Potion", var5.createString(var7 == null ? "minecraft:water" : var7)));
                        var4 = param2.set(var2, var8);
                        if ((var3x & 16384) == 16384) {
                            var4 = var4.set(var1, Pair.of(References.ITEM_NAME.typeName(), "minecraft:splash_potion"));
                        }
                    }

                    if (var3x != 0) {
                        var1x = var1x.set("Damage", var1x.createShort((short)0));
                    }

                    return var4.set(DSL.remainderFinder(), var1x);
                }
            }

            return param2;
        });
    }
}
