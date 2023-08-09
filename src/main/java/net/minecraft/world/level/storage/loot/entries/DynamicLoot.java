package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DynamicLoot extends LootPoolSingletonContainer {
    public static final Codec<DynamicLoot> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(ResourceLocation.CODEC.fieldOf("name").forGetter(param0x -> param0x.name))
                .and(singletonFields(param0))
                .apply(param0, DynamicLoot::new)
    );
    private final ResourceLocation name;

    private DynamicLoot(ResourceLocation param0, int param1, int param2, List<LootItemCondition> param3, List<LootItemFunction> param4) {
        super(param1, param2, param3, param4);
        this.name = param0;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.DYNAMIC;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
        param1.addDynamicDrops(this.name, param0);
    }

    public static LootPoolSingletonContainer.Builder<?> dynamicEntry(ResourceLocation param0) {
        return simpleBuilder((param1, param2, param3, param4) -> new DynamicLoot(param0, param1, param2, param3, param4));
    }
}
