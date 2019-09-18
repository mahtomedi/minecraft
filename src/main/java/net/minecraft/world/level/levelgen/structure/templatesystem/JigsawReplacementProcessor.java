package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class JigsawReplacementProcessor extends StructureProcessor {
    public static final JigsawReplacementProcessor INSTANCE = new JigsawReplacementProcessor();

    private JigsawReplacementProcessor() {
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader param0,
        BlockPos param1,
        StructureTemplate.StructureBlockInfo param2,
        StructureTemplate.StructureBlockInfo param3,
        StructurePlaceSettings param4
    ) {
        Block var0 = param3.state.getBlock();
        if (var0 != Blocks.JIGSAW) {
            return param3;
        } else {
            String var1 = param3.nbt.getString("final_state");
            BlockStateParser var2 = new BlockStateParser(new StringReader(var1), false);

            try {
                var2.parse(true);
            } catch (CommandSyntaxException var10) {
                throw new RuntimeException(var10);
            }

            return var2.getState().getBlock() == Blocks.STRUCTURE_VOID ? null : new StructureTemplate.StructureBlockInfo(param3.pos, var2.getState(), null);
        }
    }

    @Override
    protected StructureProcessorType getType() {
        return StructureProcessorType.JIGSAW_REPLACEMENT;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.emptyMap());
    }
}
