package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public class StructureTemplatePool {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SIZE_UNSET = Integer.MIN_VALUE;
    public static final Codec<StructureTemplatePool> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ResourceLocation.CODEC.fieldOf("name").forGetter(StructureTemplatePool::getName),
                    ResourceLocation.CODEC.fieldOf("fallback").forGetter(StructureTemplatePool::getFallback),
                    Codec.mapPair(StructurePoolElement.CODEC.fieldOf("element"), Codec.intRange(1, 150).fieldOf("weight"))
                        .codec()
                        .listOf()
                        .fieldOf("elements")
                        .forGetter(param0x -> param0x.rawTemplates)
                )
                .apply(param0, StructureTemplatePool::new)
    );
    public static final Codec<Holder<StructureTemplatePool>> CODEC = RegistryFileCodec.create(Registry.TEMPLATE_POOL_REGISTRY, DIRECT_CODEC);
    private final ResourceLocation name;
    private final List<Pair<StructurePoolElement, Integer>> rawTemplates;
    private final List<StructurePoolElement> templates;
    private final ResourceLocation fallback;
    private int maxSize = Integer.MIN_VALUE;

    public StructureTemplatePool(ResourceLocation param0, ResourceLocation param1, List<Pair<StructurePoolElement, Integer>> param2) {
        this.name = param0;
        this.rawTemplates = param2;
        this.templates = Lists.newArrayList();

        for(Pair<StructurePoolElement, Integer> var0 : param2) {
            StructurePoolElement var1 = var0.getFirst();

            for(int var2 = 0; var2 < var0.getSecond(); ++var2) {
                this.templates.add(var1);
            }
        }

        this.fallback = param1;
    }

    public StructureTemplatePool(
        ResourceLocation param0,
        ResourceLocation param1,
        List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> param2,
        StructureTemplatePool.Projection param3
    ) {
        this.name = param0;
        this.rawTemplates = Lists.newArrayList();
        this.templates = Lists.newArrayList();

        for(Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer> var0 : param2) {
            StructurePoolElement var1 = var0.getFirst().apply(param3);
            this.rawTemplates.add(Pair.of(var1, var0.getSecond()));

            for(int var2 = 0; var2 < var0.getSecond(); ++var2) {
                this.templates.add(var1);
            }
        }

        this.fallback = param1;
    }

    public int getMaxSize(StructureTemplateManager param0) {
        if (this.maxSize == Integer.MIN_VALUE) {
            this.maxSize = this.templates
                .stream()
                .filter(param0x -> param0x != EmptyPoolElement.INSTANCE)
                .mapToInt(param1 -> param1.getBoundingBox(param0, BlockPos.ZERO, Rotation.NONE).getYSpan())
                .max()
                .orElse(0);
        }

        return this.maxSize;
    }

    public ResourceLocation getFallback() {
        return this.fallback;
    }

    public StructurePoolElement getRandomTemplate(RandomSource param0) {
        return this.templates.get(param0.nextInt(this.templates.size()));
    }

    public List<StructurePoolElement> getShuffledTemplates(RandomSource param0) {
        return Util.shuffledCopy(this.templates, param0);
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public int size() {
        return this.templates.size();
    }

    public static enum Projection implements StringRepresentable {
        TERRAIN_MATCHING("terrain_matching", ImmutableList.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1))),
        RIGID("rigid", ImmutableList.of());

        public static final StringRepresentable.EnumCodec<StructureTemplatePool.Projection> CODEC = StringRepresentable.fromEnum(
            StructureTemplatePool.Projection::values
        );
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
            return CODEC.byName(param0);
        }

        public ImmutableList<StructureProcessor> getProcessors() {
            return this.processors;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
