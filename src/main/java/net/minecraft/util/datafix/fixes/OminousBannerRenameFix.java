package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class OminousBannerRenameFix extends ItemStackTagFix {
    public OminousBannerRenameFix(Schema param0) {
        super(param0, "OminousBannerRenameFix", param0x -> param0x.equals("minecraft:white_banner"));
    }

    @Override
    protected <T> Dynamic<T> fixItemStackTag(Dynamic<T> param0) {
        Optional<? extends Dynamic<?>> var0 = param0.get("display").result();
        if (var0.isPresent()) {
            Dynamic<?> var1 = var0.get();
            Optional<String> var2 = var1.get("Name").asString().result();
            if (var2.isPresent()) {
                String var3 = var2.get();
                var3 = var3.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
                var1 = var1.set("Name", var1.createString(var3));
            }

            return param0.set("display", var1);
        } else {
            return param0;
        }
    }
}
