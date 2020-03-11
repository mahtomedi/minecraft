package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Objects;

public class RenameBiomesFix extends DataFix {
    private final String name;
    private final Map<String, String> biomes;

    public RenameBiomesFix(Schema param0, boolean param1, String param2, Map<String, String> param3) {
        super(param0, param1);
        this.biomes = param3;
        this.name = param2;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, String>> var0 = DSL.named(References.BIOME.typeName(), DSL.namespacedString());
        if (!Objects.equals(var0, this.getInputSchema().getType(References.BIOME))) {
            throw new IllegalStateException("Biome type is not what was expected.");
        } else {
            return this.fixTypeEverywhere(this.name, var0, param0 -> param0x -> param0x.mapSecond(param0xx -> this.biomes.getOrDefault(param0xx, param0xx)));
        }
    }
}
