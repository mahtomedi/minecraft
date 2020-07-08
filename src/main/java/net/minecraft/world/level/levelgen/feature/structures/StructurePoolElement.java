package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public abstract class StructurePoolElement {
    public static final Codec<StructurePoolElement> CODEC = Registry.STRUCTURE_POOL_ELEMENT
        .dispatch("element_type", StructurePoolElement::getType, StructurePoolElementType::codec);
    @Nullable
    private volatile StructureTemplatePool.Projection projection;

    protected static <E extends StructurePoolElement> RecordCodecBuilder<E, StructureTemplatePool.Projection> projectionCodec() {
        return StructureTemplatePool.Projection.CODEC.fieldOf("projection").forGetter(StructurePoolElement::getProjection);
    }

    protected StructurePoolElement(StructureTemplatePool.Projection param0) {
        this.projection = param0;
    }

    public abstract List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager var1, BlockPos var2, Rotation var3, Random var4);

    public abstract BoundingBox getBoundingBox(StructureManager var1, BlockPos var2, Rotation var3);

    public abstract boolean place(
        StructureManager var1,
        WorldGenLevel var2,
        StructureFeatureManager var3,
        ChunkGenerator var4,
        BlockPos var5,
        BlockPos var6,
        Rotation var7,
        BoundingBox var8,
        Random var9,
        boolean var10
    );

    public abstract StructurePoolElementType<?> getType();

    public void handleDataMarker(
        LevelAccessor param0, StructureTemplate.StructureBlockInfo param1, BlockPos param2, Rotation param3, Random param4, BoundingBox param5
    ) {
    }

    public StructurePoolElement setProjection(StructureTemplatePool.Projection param0) {
        this.projection = param0;
        return this;
    }

    public StructureTemplatePool.Projection getProjection() {
        StructureTemplatePool.Projection var0 = this.projection;
        if (var0 == null) {
            throw new IllegalStateException();
        } else {
            return var0;
        }
    }

    public int getGroundLevelDelta() {
        return 1;
    }

    public static Function<StructureTemplatePool.Projection, EmptyPoolElement> empty() {
        return param0 -> EmptyPoolElement.INSTANCE;
    }

    public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String param0) {
        return param1 -> new LegacySinglePoolElement(Either.left(new ResourceLocation(param0)), ImmutableList::of, param1);
    }

    public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String param0, ImmutableList<StructureProcessor> param1) {
        return param2 -> new LegacySinglePoolElement(Either.left(new ResourceLocation(param0)), () -> param1, param2);
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String param0) {
        return param1 -> new SinglePoolElement(Either.left(new ResourceLocation(param0)), ImmutableList::of, param1);
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String param0, ImmutableList<StructureProcessor> param1) {
        return param2 -> new SinglePoolElement(Either.left(new ResourceLocation(param0)), () -> param1, param2);
    }

    public static Function<StructureTemplatePool.Projection, FeaturePoolElement> feature(ConfiguredFeature<?, ?> param0) {
        return param1 -> new FeaturePoolElement(() -> param0, param1);
    }

    public static Function<StructureTemplatePool.Projection, ListPoolElement> list(
        List<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>> param0
    ) {
        return param1 -> new ListPoolElement(param0.stream().map(param1x -> param1x.apply(param1)).collect(Collectors.toList()), param1);
    }
}
