package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class FeaturePoolElement extends StructurePoolElement {
    public static final Codec<FeaturePoolElement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(PlacedFeature.CODEC.fieldOf("feature").forGetter(param0x -> param0x.feature), projectionCodec())
                .apply(param0, FeaturePoolElement::new)
    );
    private final Supplier<PlacedFeature> feature;
    private final CompoundTag defaultJigsawNBT;

    protected FeaturePoolElement(Supplier<PlacedFeature> param0, StructureTemplatePool.Projection param1) {
        super(param1);
        this.feature = param0;
        this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
    }

    private CompoundTag fillDefaultJigsawNBT() {
        CompoundTag var0 = new CompoundTag();
        var0.putString("name", "minecraft:bottom");
        var0.putString("final_state", "minecraft:air");
        var0.putString("pool", "minecraft:empty");
        var0.putString("target", "minecraft:empty");
        var0.putString("joint", JigsawBlockEntity.JointType.ROLLABLE.getSerializedName());
        return var0;
    }

    @Override
    public Vec3i getSize(StructureManager param0, Rotation param1) {
        return Vec3i.ZERO;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager param0, BlockPos param1, Rotation param2, Random param3) {
        List<StructureTemplate.StructureBlockInfo> var0 = Lists.newArrayList();
        var0.add(
            new StructureTemplate.StructureBlockInfo(
                param1,
                Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(Direction.DOWN, Direction.SOUTH)),
                this.defaultJigsawNBT
            )
        );
        return var0;
    }

    @Override
    public BoundingBox getBoundingBox(StructureManager param0, BlockPos param1, Rotation param2) {
        Vec3i var0 = this.getSize(param0, param2);
        return new BoundingBox(
            param1.getX(), param1.getY(), param1.getZ(), param1.getX() + var0.getX(), param1.getY() + var0.getY(), param1.getZ() + var0.getZ()
        );
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
        return this.feature.get().place(param1, param3, param8, param4);
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.FEATURE;
    }

    @Override
    public String toString() {
        return "Feature[" + this.feature.get() + "]";
    }
}
