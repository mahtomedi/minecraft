package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyNameFunction extends LootItemConditionalFunction {
    public static final Codec<CopyNameFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(CopyNameFunction.NameSource.CODEC.fieldOf("source").forGetter(param0x -> param0x.source))
                .apply(param0, CopyNameFunction::new)
    );
    private final CopyNameFunction.NameSource source;

    private CopyNameFunction(List<LootItemCondition> param0, CopyNameFunction.NameSource param1) {
        super(param0);
        this.source = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.COPY_NAME;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.source.param);
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        Object var0 = param1.getParamOrNull(this.source.param);
        if (var0 instanceof Nameable var1 && var1.hasCustomName()) {
            param0.setHoverName(var1.getDisplayName());
        }

        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> copyName(CopyNameFunction.NameSource param0) {
        return simpleBuilder(param1 -> new CopyNameFunction(param1, param0));
    }

    public static enum NameSource implements StringRepresentable {
        THIS("this", LootContextParams.THIS_ENTITY),
        KILLER("killer", LootContextParams.KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER),
        BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY);

        public static final Codec<CopyNameFunction.NameSource> CODEC = StringRepresentable.fromEnum(CopyNameFunction.NameSource::values);
        private final String name;
        final LootContextParam<?> param;

        private NameSource(String param0, LootContextParam<?> param1) {
            this.name = param0;
            this.param = param1;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
