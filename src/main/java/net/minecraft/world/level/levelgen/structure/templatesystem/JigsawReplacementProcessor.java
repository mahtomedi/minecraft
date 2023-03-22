package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class JigsawReplacementProcessor extends StructureProcessor {
    private static final Logger LOGGER = LogUtils.getLogger();
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
        BlockState var0 = param4.state();
        if (var0.is(Blocks.JIGSAW)) {
            if (param4.nbt() == null) {
                LOGGER.warn("Jigsaw block at {} is missing nbt, will not replace", param1);
                return param4;
            } else {
                String var1 = param4.nbt().getString("final_state");

                BlockState var3;
                try {
                    BlockStateParser.BlockResult var2 = BlockStateParser.parseForBlock(param0.holderLookup(Registries.BLOCK), var1, true);
                    var3 = var2.blockState();
                } catch (CommandSyntaxException var11) {
                    throw new RuntimeException(var11);
                }

                return var3.is(Blocks.STRUCTURE_VOID) ? null : new StructureTemplate.StructureBlockInfo(param4.pos(), var3, null);
            }
        } else {
            return param4;
        }
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.JIGSAW_REPLACEMENT;
    }
}
