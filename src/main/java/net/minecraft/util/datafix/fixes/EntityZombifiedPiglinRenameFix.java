package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import java.util.Objects;

public class EntityZombifiedPiglinRenameFix extends SimplestEntityRenameFix {
    public static final Map<String, String> RENAMED_IDS = ImmutableMap.<String, String>builder()
        .put("minecraft:zombie_pigman_spawn_egg", "minecraft:zombified_piglin_spawn_egg")
        .build();

    public EntityZombifiedPiglinRenameFix(Schema param0) {
        super("EntityZombifiedPiglinRenameFix", param0, true);
    }

    @Override
    protected String rename(String param0) {
        return Objects.equals("minecraft:zombie_pigman", param0) ? "minecraft:zombified_piglin" : param0;
    }
}
