package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public class EntityPaintingMotiveFix extends NamedEntityFix {
    private static final Map<String, String> MAP = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        param0.put("donkeykong", "donkey_kong");
        param0.put("burningskull", "burning_skull");
        param0.put("skullandroses", "skull_and_roses");
    });

    public EntityPaintingMotiveFix(Schema param0, boolean param1) {
        super(param0, param1, "EntityPaintingMotiveFix", References.ENTITY, "minecraft:painting");
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        Optional<String> var0 = param0.get("Motive").asString().result();
        if (var0.isPresent()) {
            String var1 = var0.get().toLowerCase(Locale.ROOT);
            return param0.set("Motive", param0.createString(new ResourceLocation(MAP.getOrDefault(var1, var1)).toString()));
        } else {
            return param0;
        }
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }
}
