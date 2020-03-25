package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class RuleProcessor extends StructureProcessor {
    private final ImmutableList<ProcessorRule> rules;

    public RuleProcessor(List<ProcessorRule> param0) {
        this.rules = ImmutableList.copyOf(param0);
    }

    public RuleProcessor(Dynamic<?> param0) {
        this(param0.get("rules").asList(ProcessorRule::deserialize));
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
        Random var0 = new Random(Mth.getSeed(param4.pos));
        BlockState var1 = param0.getBlockState(param4.pos);

        for(ProcessorRule var2 : this.rules) {
            if (var2.test(param4.state, var1, param3.pos, param4.pos, param2, var0)) {
                return new StructureTemplate.StructureBlockInfo(param4.pos, var2.getOutputState(), var2.getOutputTag());
            }
        }

        return param4;
    }

    @Override
    protected StructureProcessorType getType() {
        return StructureProcessorType.RULE;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(param0.createString("rules"), param0.createList(this.rules.stream().map(param1 -> param1.serialize(param0).getValue())))
            )
        );
    }
}
