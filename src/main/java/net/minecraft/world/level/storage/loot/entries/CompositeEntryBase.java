package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase extends LootPoolEntryContainer {
    protected final List<LootPoolEntryContainer> children;
    private final ComposableEntryContainer composedChildren;

    protected CompositeEntryBase(List<LootPoolEntryContainer> param0, List<LootItemCondition> param1) {
        super(param1);
        this.children = param0;
        this.composedChildren = this.compose(param0);
    }

    @Override
    public void validate(ValidationContext param0) {
        super.validate(param0);
        if (this.children.isEmpty()) {
            param0.reportProblem("Empty children list");
        }

        for(int var0 = 0; var0 < this.children.size(); ++var0) {
            this.children.get(var0).validate(param0.forChild(".entry[" + var0 + "]"));
        }

    }

    protected abstract ComposableEntryContainer compose(List<? extends ComposableEntryContainer> var1);

    @Override
    public final boolean expand(LootContext param0, Consumer<LootPoolEntry> param1) {
        return !this.canRun(param0) ? false : this.composedChildren.expand(param0, param1);
    }

    public static <T extends CompositeEntryBase> Codec<T> createCodec(CompositeEntryBase.CompositeEntryConstructor<T> param0) {
        return RecordCodecBuilder.create(
            param1 -> param1.group(
                        ExtraCodecs.strictOptionalField(LootPoolEntries.CODEC.listOf(), "children", List.of()).forGetter(param0x -> param0x.children)
                    )
                    .and(commonFields(param1).t1())
                    .apply(param1, param0::create)
        );
    }

    @FunctionalInterface
    public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
        T create(List<LootPoolEntryContainer> var1, List<LootItemCondition> var2);
    }
}
