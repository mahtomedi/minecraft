package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class JigsawReplacementProcessor extends StructureProcessor {
    public static final Codec<JigsawReplacementProcessor> CODEC = Codec.unit(() -> JigsawReplacementProcessor.INSTANCE);
    public static final JigsawReplacementProcessor INSTANCE = new JigsawReplacementProcessor();

    private JigsawReplacementProcessor() {
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
        BlockState var0 = param4.state;
        if (var0.is(Blocks.JIGSAW)) {
            String var1 = param4.nbt.getString("final_state");
            BlockStateParser var2 = new BlockStateParser(new StringReader(var1), false);

            try {
                var2.parse(true);
            } catch (CommandSyntaxException var11) {
                throw new RuntimeException(var11);
            }

            return var2.getState().is(Blocks.STRUCTURE_VOID) ? null : new StructureTemplate.StructureBlockInfo(param4.pos, var2.getState(), null);
        } else {
            return param4;
        }
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.JIGSAW_REPLACEMENT;
    }
}
