package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;

public class EntityCatSplitFix extends SimpleEntityRenameFix {
    public EntityCatSplitFix(Schema param0, boolean param1) {
        super("EntityCatSplitFix", param0, param1);
    }

    @Override
    protected Pair<String, Dynamic<?>> getNewNameAndTag(String param0, Dynamic<?> param1) {
        if (Objects.equals("minecraft:ocelot", param0)) {
            int var0 = param1.get("CatType").asInt(0);
            if (var0 == 0) {
                String var1 = param1.get("Owner").asString("");
                String var2 = param1.get("OwnerUUID").asString("");
                if (var1.length() > 0 || var2.length() > 0) {
                    param1.set("Trusting", param1.createBoolean(true));
                }
            } else if (var0 > 0 && var0 < 4) {
                param1 = param1.set("CatType", param1.createInt(var0));
                param1 = param1.set("OwnerUUID", param1.createString(param1.get("OwnerUUID").asString("")));
                return Pair.of("minecraft:cat", param1);
            }
        }

        return Pair.of(param0, param1);
    }
}
