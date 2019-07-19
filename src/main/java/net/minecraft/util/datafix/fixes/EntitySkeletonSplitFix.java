package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;

public class EntitySkeletonSplitFix extends SimpleEntityRenameFix {
    public EntitySkeletonSplitFix(Schema param0, boolean param1) {
        super("EntitySkeletonSplitFix", param0, param1);
    }

    @Override
    protected Pair<String, Dynamic<?>> getNewNameAndTag(String param0, Dynamic<?> param1) {
        if (Objects.equals(param0, "Skeleton")) {
            int var0 = param1.get("SkeletonType").asInt(0);
            if (var0 == 1) {
                param0 = "WitherSkeleton";
            } else if (var0 == 2) {
                param0 = "Stray";
            }
        }

        return Pair.of(param0, param1);
    }
}
