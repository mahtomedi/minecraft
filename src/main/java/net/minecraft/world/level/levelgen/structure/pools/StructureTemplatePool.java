package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.commons.lang3.mutable.MutableObject;

public class StructureTemplatePool {
    private static final int SIZE_UNSET = Integer.MIN_VALUE;
    private static final MutableObject<Codec<Holder<StructureTemplatePool>>> CODEC_REFERENCE = new MutableObject<>();
    public static final Codec<StructureTemplatePool> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.lazyInitializedCodec(CODEC_REFERENCE::getValue).fieldOf("fallback").forGetter(StructureTemplatePool::getFallback),
                    Codec.mapPair(StructurePoolElement.CODEC.fieldOf("element"), Codec.intRange(1, 150).fieldOf("weight"))
                        .codec()
                        .listOf()
                        .fieldOf("elements")
                        .forGetter(param0x -> param0x.rawTemplates)
                )
                .apply(param0, StructureTemplatePool::new)
    );
    public static final Codec<Holder<StructureTemplatePool>> CODEC = Util.make(
        RegistryFileCodec.create(Registries.TEMPLATE_POOL, DIRECT_CODEC), CODEC_REFERENCE::setValue
    );
    private final List<Pair<StructurePoolElement, Integer>> rawTemplates;
    private final ObjectArrayList<StructurePoolElement> templates;
    private final Holder<StructureTemplatePool> fallback;
    private int maxSize = Integer.MIN_VALUE;

    public StructureTemplatePool(Holder<StructureTemplatePool> param0, List<Pair<StructurePoolElement, Integer>> param1) {
        this.rawTemplates = param1;
        this.templates = new ObjectArrayList<>();

        for(Pair<StructurePoolElement, Integer> var0 : param1) {
            StructurePoolElement var1 = var0.getFirst();

            for(int var2 = 0; var2 < var0.getSecond(); ++var2) {
                this.templates.add(var1);
            }
        }

        this.fallback = param0;
    }

    public StructureTemplatePool(
        Holder<StructureTemplatePool> param0,
        List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> param1,
        StructureTemplatePool.Projection param2
    ) {
        this.rawTemplates = Lists.newArrayList();
        this.templates = new ObjectArrayList<>();

        for(Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer> var0 : param1) {
            StructurePoolElement var1 = var0.getFirst().apply(param2);
            this.rawTemplates.add(Pair.of(var1, var0.getSecond()));

            for(int var2 = 0; var2 < var0.getSecond(); ++var2) {
                this.templates.add(var1);
            }
        }

        this.fallback = param0;
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

    public Holder<StructureTemplatePool> getFallback() {
        return this.fallback;
    }

    public StructurePoolElement getRandomTemplate(RandomSource param0) {
        return this.templates.get(param0.nextInt(this.templates.size()));
    }

    public List<StructurePoolElement> getShuffledTemplates(RandomSource param0) {
        return Util.shuffledCopy(this.templates, param0);
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
