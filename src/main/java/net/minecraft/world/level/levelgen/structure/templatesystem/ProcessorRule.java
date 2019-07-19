package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Deserializer;
import net.minecraft.world.level.block.state.BlockState;

public class ProcessorRule {
    private final RuleTest inputPredicate;
    private final RuleTest locPredicate;
    private final BlockState outputState;
    @Nullable
    private final CompoundTag outputTag;

    public ProcessorRule(RuleTest param0, RuleTest param1, BlockState param2) {
        this(param0, param1, param2, null);
    }

    public ProcessorRule(RuleTest param0, RuleTest param1, BlockState param2, @Nullable CompoundTag param3) {
        this.inputPredicate = param0;
        this.locPredicate = param1;
        this.outputState = param2;
        this.outputTag = param3;
    }

    public boolean test(BlockState param0, BlockState param1, Random param2) {
        return this.inputPredicate.test(param0, param2) && this.locPredicate.test(param1, param2);
    }

    public BlockState getOutputState() {
        return this.outputState;
    }

    @Nullable
    public CompoundTag getOutputTag() {
        return this.outputTag;
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        T var0 = param0.createMap(
            ImmutableMap.of(
                param0.createString("input_predicate"),
                this.inputPredicate.serialize(param0).getValue(),
                param0.createString("location_predicate"),
                this.locPredicate.serialize(param0).getValue(),
                param0.createString("output_state"),
                BlockState.serialize(param0, this.outputState).getValue()
            )
        );
        return this.outputTag == null
            ? new Dynamic<>(param0, var0)
            : new Dynamic<>(
                param0, param0.mergeInto(var0, param0.createString("output_nbt"), new Dynamic<>(NbtOps.INSTANCE, this.outputTag).convert(param0).getValue())
            );
    }

    public static <T> ProcessorRule deserialize(Dynamic<T> param0) {
        Dynamic<T> var0 = param0.get("input_predicate").orElseEmptyMap();
        Dynamic<T> var1 = param0.get("location_predicate").orElseEmptyMap();
        RuleTest var2 = Deserializer.deserialize(var0, Registry.RULE_TEST, "predicate_type", AlwaysTrueTest.INSTANCE);
        RuleTest var3 = Deserializer.deserialize(var1, Registry.RULE_TEST, "predicate_type", AlwaysTrueTest.INSTANCE);
        BlockState var4 = BlockState.deserialize(param0.get("output_state").orElseEmptyMap());
        CompoundTag var5 = (CompoundTag)param0.get("output_nbt").map(param0x -> param0x.convert(NbtOps.INSTANCE).getValue()).orElse(null);
        return new ProcessorRule(var2, var3, var4, var5);
    }
}
