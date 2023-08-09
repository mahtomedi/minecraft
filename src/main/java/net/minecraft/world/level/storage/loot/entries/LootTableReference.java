package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootTableReference extends LootPoolSingletonContainer {
    public static final Codec<LootTableReference> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(ResourceLocation.CODEC.fieldOf("name").forGetter(param0x -> param0x.name))
                .and(singletonFields(param0))
                .apply(param0, LootTableReference::new)
    );
    private final ResourceLocation name;

    private LootTableReference(ResourceLocation param0, int param1, int param2, List<LootItemCondition> param3, List<LootItemFunction> param4) {
        super(param1, param2, param3, param4);
        this.name = param0;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.REFERENCE;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
        LootTable var0 = param1.getResolver().getLootTable(this.name);
        var0.getRandomItemsRaw(param1, param0);
    }

    @Override
    public void validate(ValidationContext param0) {
        LootDataId<LootTable> var0 = new LootDataId<>(LootDataType.TABLE, this.name);
        if (param0.hasVisitedElement(var0)) {
            param0.reportProblem("Table " + this.name + " is recursively called");
        } else {
            super.validate(param0);
            param0.resolver()
                .getElementOptional(var0)
                .ifPresentOrElse(
                    param2 -> param2.validate(param0.enterElement("->{" + this.name + "}", var0)),
                    () -> param0.reportProblem("Unknown loot table called " + this.name)
                );
        }
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation param0) {
        return simpleBuilder((param1, param2, param3, param4) -> new LootTableReference(param0, param1, param2, param3, param4));
    }
}
