package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import java.util.Objects;

public class EntityPufferfishRenameFix extends SimplestEntityRenameFix {
    public static final Map<String, String> RENAMED_IDS = ImmutableMap.<String, String>builder()
        .put("minecraft:puffer_fish_spawn_egg", "minecraft:pufferfish_spawn_egg")
        .build();

    public EntityPufferfishRenameFix(Schema param0, boolean param1) {
        super("EntityPufferfishRenameFix", param0, param1);
    }

    @Override
    protected String rename(String param0) {
        return Objects.equals("minecraft:puffer_fish", param0) ? "minecraft:pufferfish" : param0;
    }
}
