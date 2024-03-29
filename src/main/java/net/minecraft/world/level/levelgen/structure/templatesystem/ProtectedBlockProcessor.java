package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ProtectedBlockProcessor extends StructureProcessor {
    public final TagKey<Block> cannotReplace;
    public static final Codec<ProtectedBlockProcessor> CODEC = TagKey.hashedCodec(Registries.BLOCK)
        .xmap(ProtectedBlockProcessor::new, param0 -> param0.cannotReplace);

    public ProtectedBlockProcessor(TagKey<Block> param0) {
        this.cannotReplace = param0;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader param0,
        BlockPos param1,
        BlockPos param2,
        StructureTemplate.StructureBlockInfo param3,
        StructureTemplate.StructureBlockInfo param4,
        StructurePlaceSettings param5
    ) {
        return Feature.isReplaceable(this.cannotReplace).test(param0.getBlockState(param4.pos())) ? param4 : null;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.PROTECTED_BLOCKS;
    }
}
