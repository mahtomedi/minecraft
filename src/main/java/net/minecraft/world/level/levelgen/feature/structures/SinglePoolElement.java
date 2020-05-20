package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SinglePoolElement extends StructurePoolElement {
    private static final Codec<Either<ResourceLocation, StructureTemplate>> TEMPLATE_CODEC = Codec.of(
        SinglePoolElement::encodeTemplate, ResourceLocation.CODEC.map(Either::left)
    );
    public static final Codec<SinglePoolElement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(templateCodec(), processorsCodec(), projectionCodec()).apply(param0, SinglePoolElement::new)
    );
    protected final Either<ResourceLocation, StructureTemplate> template;
    protected final ImmutableList<StructureProcessor> processors;

    private static <T> DataResult<T> encodeTemplate(Either<ResourceLocation, StructureTemplate> param0, DynamicOps<T> param1, T param2) {
        Optional<ResourceLocation> var0 = param0.left();
        return !var0.isPresent() ? DataResult.error("Can not serialize a runtime pool element") : ResourceLocation.CODEC.encode(var0.get(), param1, param2);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, List<StructureProcessor>> processorsCodec() {
        return StructureProcessorType.CODEC.listOf().fieldOf("processors").forGetter(param0 -> param0.processors);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<ResourceLocation, StructureTemplate>> templateCodec() {
        return TEMPLATE_CODEC.fieldOf("location").forGetter(param0 -> param0.template);
    }

    @Deprecated
    public SinglePoolElement(String param0, List<StructureProcessor> param1) {
        this(Either.left(new ResourceLocation(param0)), param1, StructureTemplatePool.Projection.RIGID);
    }

    protected SinglePoolElement(Either<ResourceLocation, StructureTemplate> param0, List<StructureProcessor> param1, StructureTemplatePool.Projection param2) {
        super(param2);
        this.template = param0;
        this.processors = ImmutableList.copyOf(param1);
    }

    public SinglePoolElement(StructureTemplate param0, List<StructureProcessor> param1, StructureTemplatePool.Projection param2) {
        this(Either.right(param0), param1, param2);
    }

    @Deprecated
    public SinglePoolElement(String param0) {
        this(param0, ImmutableList.of());
    }

    private StructureTemplate getTemplate(StructureManager param0) {
        return this.template.map(param0::getOrCreate, Function.identity());
    }

    public List<StructureTemplate.StructureBlockInfo> getDataMarkers(StructureManager param0, BlockPos param1, Rotation param2, boolean param3) {
        StructureTemplate var0 = this.getTemplate(param0);
        List<StructureTemplate.StructureBlockInfo> var1 = var0.filterBlocks(
            param1, new StructurePlaceSettings().setRotation(param2), Blocks.STRUCTURE_BLOCK, param3
        );
        List<StructureTemplate.StructureBlockInfo> var2 = Lists.newArrayList();

        for(StructureTemplate.StructureBlockInfo var3 : var1) {
            if (var3.nbt != null) {
                StructureMode var4 = StructureMode.valueOf(var3.nbt.getString("mode"));
                if (var4 == StructureMode.DATA) {
                    var2.add(var3);
                }
            }
        }

        return var2;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager param0, BlockPos param1, Rotation param2, Random param3) {
        StructureTemplate var0 = this.getTemplate(param0);
        List<StructureTemplate.StructureBlockInfo> var1 = var0.filterBlocks(param1, new StructurePlaceSettings().setRotation(param2), Blocks.JIGSAW, true);
        Collections.shuffle(var1, param3);
        return var1;
    }

    @Override
    public BoundingBox getBoundingBox(StructureManager param0, BlockPos param1, Rotation param2) {
        StructureTemplate var0 = this.getTemplate(param0);
        return var0.getBoundingBox(new StructurePlaceSettings().setRotation(param2), param1);
    }

    @Override
    public boolean place(
        StructureManager param0,
        WorldGenLevel param1,
        StructureFeatureManager param2,
        ChunkGenerator param3,
        BlockPos param4,
        BlockPos param5,
        Rotation param6,
        BoundingBox param7,
        Random param8,
        boolean param9
    ) {
        StructureTemplate var0 = this.getTemplate(param0);
        StructurePlaceSettings var1 = this.getSettings(param6, param7, param9);
        if (!var0.placeInWorld(param1, param4, param5, var1, 18)) {
            return false;
        } else {
            for(StructureTemplate.StructureBlockInfo var3 : StructureTemplate.processBlockInfos(
                param1, param4, param5, var1, this.getDataMarkers(param0, param4, param6, false)
            )) {
                this.handleDataMarker(param1, var3, param4, param6, param8, param7);
            }

            return true;
        }
    }

    protected StructurePlaceSettings getSettings(Rotation param0, BoundingBox param1, boolean param2) {
        StructurePlaceSettings var0 = new StructurePlaceSettings();
        var0.setBoundingBox(param1);
        var0.setRotation(param0);
        var0.setKnownShape(true);
        var0.setIgnoreEntities(false);
        var0.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        var0.setFinalizeEntities(true);
        if (!param2) {
            var0.addProcessor(JigsawReplacementProcessor.INSTANCE);
        }

        this.processors.forEach(var0::addProcessor);
        this.getProjection().getProcessors().forEach(var0::addProcessor);
        return var0;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.SINGLE;
    }

    @Override
    public String toString() {
        return "Single[" + this.template + "]";
    }
}
