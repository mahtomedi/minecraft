package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.Passthrough;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifier;

public class ProcessorRule {
    public static final Passthrough DEFAULT_BLOCK_ENTITY_MODIFIER = Passthrough.INSTANCE;
    public static final Codec<ProcessorRule> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    RuleTest.CODEC.fieldOf("input_predicate").forGetter(param0x -> param0x.inputPredicate),
                    RuleTest.CODEC.fieldOf("location_predicate").forGetter(param0x -> param0x.locPredicate),
                    PosRuleTest.CODEC.optionalFieldOf("position_predicate", PosAlwaysTrueTest.INSTANCE).forGetter(param0x -> param0x.posPredicate),
                    BlockState.CODEC.fieldOf("output_state").forGetter(param0x -> param0x.outputState),
                    RuleBlockEntityModifier.CODEC
                        .optionalFieldOf("block_entity_modifier", DEFAULT_BLOCK_ENTITY_MODIFIER)
                        .forGetter(param0x -> param0x.blockEntityModifier)
                )
                .apply(param0, ProcessorRule::new)
    );
    private final RuleTest inputPredicate;
    private final RuleTest locPredicate;
    private final PosRuleTest posPredicate;
    private final BlockState outputState;
    private final RuleBlockEntityModifier blockEntityModifier;

    public ProcessorRule(RuleTest param0, RuleTest param1, BlockState param2) {
        this(param0, param1, PosAlwaysTrueTest.INSTANCE, param2);
    }

    public ProcessorRule(RuleTest param0, RuleTest param1, PosRuleTest param2, BlockState param3) {
        this(param0, param1, param2, param3, DEFAULT_BLOCK_ENTITY_MODIFIER);
    }

    public ProcessorRule(RuleTest param0, RuleTest param1, PosRuleTest param2, BlockState param3, RuleBlockEntityModifier param4) {
        this.inputPredicate = param0;
        this.locPredicate = param1;
        this.posPredicate = param2;
        this.outputState = param3;
        this.blockEntityModifier = param4;
    }

    public boolean test(BlockState param0, BlockState param1, BlockPos param2, BlockPos param3, BlockPos param4, RandomSource param5) {
        return this.inputPredicate.test(param0, param5) && this.locPredicate.test(param1, param5) && this.posPredicate.test(param2, param3, param4, param5);
    }

    public BlockState getOutputState() {
        return this.outputState;
    }

    @Nullable
    public CompoundTag getOutputTag(RandomSource param0, @Nullable CompoundTag param1) {
        return this.blockEntityModifier.apply(param0, param1);
    }
}
