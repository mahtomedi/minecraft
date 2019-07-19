package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class OminousBannerBlockEntityRenameFix extends NamedEntityFix {
    public OminousBannerBlockEntityRenameFix(Schema param0, boolean param1) {
        super(param0, param1, "OminousBannerBlockEntityRenameFix", References.BLOCK_ENTITY, "minecraft:banner");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }

    private Dynamic<?> fixTag(Dynamic<?> param0x) {
        Optional<String> var0 = param0x.get("CustomName").asString();
        if (var0.isPresent()) {
            String var1 = var0.get();
            var1 = var1.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
            return param0x.set("CustomName", param0x.createString(var1));
        } else {
            return param0x;
        }
    }
}
