package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class StructureTemplatePools {
    private final Map<ResourceLocation, StructureTemplatePool> pools = Maps.newHashMap();

    public StructureTemplatePools() {
        this.register(StructureTemplatePool.EMPTY);
    }

    public void register(StructureTemplatePool param0) {
        this.pools.put(param0.getName(), param0);
    }

    public StructureTemplatePool getPool(ResourceLocation param0) {
        StructureTemplatePool var0 = this.pools.get(param0);
        return var0 != null ? var0 : StructureTemplatePool.INVALID;
    }
}
