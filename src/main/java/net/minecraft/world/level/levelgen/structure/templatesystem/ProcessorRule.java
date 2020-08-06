package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class ProcessorRule {
    public static final Codec<ProcessorRule> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    RuleTest.CODEC.fieldOf("input_predicate").forGetter(param0x -> param0x.inputPredicate),
                    RuleTest.CODEC.fieldOf("location_predicate").forGetter(param0x -> param0x.locPredicate),
                    PosRuleTest.CODEC.optionalFieldOf("position_predicate", PosAlwaysTrueTest.INSTANCE).forGetter(param0x -> param0x.posPredicate),
                    BlockState.CODEC.fieldOf("output_state").forGetter(param0x -> param0x.outputState),
                    CompoundTag.CODEC.optionalFieldOf("output_nbt").forGetter(param0x -> Optional.ofNullable(param0x.outputTag))
                )
                .apply(param0, ProcessorRule::new)
    );
    private final RuleTest inputPredicate;
    private final RuleTest locPredicate;
    private final PosRuleTest posPredicate;
    private final BlockState outputState;
    @Nullable
    private final CompoundTag outputTag;

    public ProcessorRule(RuleTest param0, RuleTest param1, BlockState param2) {
        this(param0, param1, PosAlwaysTrueTest.INSTANCE, param2, Optional.empty());
    }

    public ProcessorRule(RuleTest param0, RuleTest param1, PosRuleTest param2, BlockState param3) {
        this(param0, param1, param2, param3, Optional.empty());
    }

    public ProcessorRule(RuleTest param0, RuleTest param1, PosRuleTest param2, BlockState param3, Optional<CompoundTag> param4) {
        this.inputPredicate = param0;
        this.locPredicate = param1;
        this.posPredicate = param2;
        this.outputState = param3;
        this.outputTag = param4.orElse(null);
    }

    public boolean test(BlockState param0, BlockState param1, BlockPos param2, BlockPos param3, BlockPos param4, Random param5) {
        return this.inputPredicate.test(param0, param5) && this.locPredicate.test(param1, param5) && this.posPredicate.test(param2, param3, param4, param5);
    }

    public BlockState getOutputState() {
        return this.outputState;
    }

    @Nullable
    public CompoundTag getOutputTag() {
        return this.outputTag;
    }
}
