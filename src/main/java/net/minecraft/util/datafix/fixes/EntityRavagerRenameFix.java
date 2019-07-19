package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import java.util.Objects;

public class EntityRavagerRenameFix extends SimplestEntityRenameFix {
    public static final Map<String, String> RENAMED_IDS = ImmutableMap.<String, String>builder()
        .put("minecraft:illager_beast_spawn_egg", "minecraft:ravager_spawn_egg")
        .build();

    public EntityRavagerRenameFix(Schema param0, boolean param1) {
        super("EntityRavagerRenameFix", param0, param1);
    }

    @Override
    protected String rename(String param0) {
        return Objects.equals("minecraft:illager_beast", param0) ? "minecraft:ravager" : param0;
    }
}
