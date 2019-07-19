package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class StructureTemplatePool {
    public static final StructureTemplatePool EMPTY = new StructureTemplatePool(
        new ResourceLocation("empty"), new ResourceLocation("empty"), ImmutableList.of(), StructureTemplatePool.Projection.RIGID
    );
    public static final StructureTemplatePool INVALID = new StructureTemplatePool(
        new ResourceLocation("invalid"), new ResourceLocation("invalid"), ImmutableList.of(), StructureTemplatePool.Projection.RIGID
    );
    private final ResourceLocation name;
    private final ImmutableList<Pair<StructurePoolElement, Integer>> rawTemplates;
    private final List<StructurePoolElement> templates;
    private final ResourceLocation fallback;
    private final StructureTemplatePool.Projection projection;
    private int maxSize = Integer.MIN_VALUE;

    public StructureTemplatePool(
        ResourceLocation param0, ResourceLocation param1, List<Pair<StructurePoolElement, Integer>> param2, StructureTemplatePool.Projection param3
    ) {
        this.name = param0;
        this.rawTemplates = ImmutableList.copyOf(param2);
        this.templates = Lists.newArrayList();

        for(Pair<StructurePoolElement, Integer> var0 : param2) {
            for(Integer var1 = 0; var1 < var0.getSecond(); var1 = var1 + 1) {
                this.templates.add(var0.getFirst().setProjection(param3));
            }
        }

        this.fallback = param1;
        this.projection = param3;
    }

    public int getMaxSize(StructureManager param0) {
        if (this.maxSize == Integer.MIN_VALUE) {
            this.maxSize = this.templates.stream().mapToInt(param1 -> param1.getBoundingBox(param0, BlockPos.ZERO, Rotation.NONE).getYSpan()).max().orElse(0);
        }

        return this.maxSize;
    }

    public ResourceLocation getFallback() {
        return this.fallback;
    }

    public StructurePoolElement getRandomTemplate(Random param0) {
        return this.templates.get(param0.nextInt(this.templates.size()));
    }

    public List<StructurePoolElement> getShuffledTemplates(Random param0) {
        return ImmutableList.copyOf(ObjectArrays.shuffle(this.templates.toArray(new StructurePoolElement[0]), param0));
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public int size() {
        return this.templates.size();
    }

    public static enum Projection {
        TERRAIN_MATCHING("terrain_matching", ImmutableList.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1))),
        RIGID("rigid", ImmutableList.of());

        private static final Map<String, StructureTemplatePool.Projection> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(StructureTemplatePool.Projection::getName, param0 -> param0));
        private final String name;
        private final ImmutableList<StructureProcessor> processors;

        private Projection(String param0, ImmutableList<StructureProcessor> param1) {
            this.name = param0;
            this.processors = param1;
        }

        public String getName() {
            return this.name;
        }

        public static StructureTemplatePool.Projection byName(String param0) {
            return BY_NAME.get(param0);
        }

        public ImmutableList<StructureProcessor> getProcessors() {
            return this.processors;
        }
    }
}
