package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;

public class EntityTippedArrowFix extends SimplestEntityRenameFix {
    public EntityTippedArrowFix(Schema param0, boolean param1) {
        super("EntityTippedArrowFix", param0, param1);
    }

    @Override
    protected String rename(String param0) {
        return Objects.equals(param0, "TippedArrow") ? "Arrow" : param0;
    }
}
