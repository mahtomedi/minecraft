package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityElderGuardianSplitFix extends SimpleEntityRenameFix {
    public EntityElderGuardianSplitFix(Schema param0, boolean param1) {
        super("EntityElderGuardianSplitFix", param0, param1);
    }

    @Override
    protected Pair<String, Dynamic<?>> getNewNameAndTag(String param0, Dynamic<?> param1) {
        return Pair.of(Objects.equals(param0, "Guardian") && param1.get("Elder").asBoolean(false) ? "ElderGuardian" : param0, param1);
    }
}
