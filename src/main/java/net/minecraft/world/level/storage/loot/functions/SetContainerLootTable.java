package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable extends LootItemConditionalFunction {
    public static final Codec<SetContainerLootTable> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        ResourceLocation.CODEC.fieldOf("name").forGetter(param0x -> param0x.name),
                        ExtraCodecs.strictOptionalField(Codec.LONG, "seed", 0L).forGetter(param0x -> param0x.seed),
                        BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec().fieldOf("type").forGetter(param0x -> param0x.type)
                    )
                )
                .apply(param0, SetContainerLootTable::new)
    );
    private final ResourceLocation name;
    private final long seed;
    private final Holder<BlockEntityType<?>> type;

    private SetContainerLootTable(List<LootItemCondition> param0, ResourceLocation param1, long param2, Holder<BlockEntityType<?>> param3) {
        super(param0);
        this.name = param1;
        this.seed = param2;
        this.type = param3;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_LOOT_TABLE;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (param0.isEmpty()) {
            return param0;
        } else {
            CompoundTag var0 = BlockItem.getBlockEntityData(param0);
            if (var0 == null) {
                var0 = new CompoundTag();
            }

            var0.putString("LootTable", this.name.toString());
            if (this.seed != 0L) {
                var0.putLong("LootTableSeed", this.seed);
            }

            BlockItem.setBlockEntityData(param0, this.type.value(), var0);
            return param0;
        }
    }

    @Override
    public void validate(ValidationContext param0) {
        super.validate(param0);
        LootDataId<LootTable> var0 = new LootDataId<>(LootDataType.TABLE, this.name);
        if (param0.resolver().getElementOptional(var0).isEmpty()) {
            param0.reportProblem("Missing loot table used for container: " + this.name);
        }

    }

    public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> param0, ResourceLocation param1) {
        return simpleBuilder(param2 -> new SetContainerLootTable(param2, param1, 0L, param0.builtInRegistryHolder()));
    }

    public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> param0, ResourceLocation param1, long param2) {
        return simpleBuilder(param3 -> new SetContainerLootTable(param3, param1, param2, param0.builtInRegistryHolder()));
    }
}
