package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

public class Pools {
    public static final StructureTemplatePool EMPTY = register(
        new StructureTemplatePool(new ResourceLocation("empty"), new ResourceLocation("empty"), ImmutableList.of(), StructureTemplatePool.Projection.RIGID)
    );
    public static final StructureTemplatePool INVALID = register(
        new StructureTemplatePool(new ResourceLocation("invalid"), new ResourceLocation("invalid"), ImmutableList.of(), StructureTemplatePool.Projection.RIGID)
    );

    public static StructureTemplatePool register(StructureTemplatePool param0) {
        return BuiltinRegistries.register(BuiltinRegistries.TEMPLATE_POOL, param0.getName(), param0);
    }

    public static void bootstrap() {
        BastionPieces.bootstrap();
        PillagerOutpostPools.bootstrap();
        VillagePools.bootstrap();
    }

    static {
        bootstrap();
    }
}
