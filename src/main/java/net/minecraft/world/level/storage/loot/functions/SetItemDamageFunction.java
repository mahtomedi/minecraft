package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.slf4j.Logger;

public class SetItemDamageFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<SetItemDamageFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        NumberProviders.CODEC.fieldOf("damage").forGetter(param0x -> param0x.damage),
                        Codec.BOOL.fieldOf("add").orElse(false).forGetter(param0x -> param0x.add)
                    )
                )
                .apply(param0, SetItemDamageFunction::new)
    );
    private final NumberProvider damage;
    private final boolean add;

    private SetItemDamageFunction(List<LootItemCondition> param0, NumberProvider param1, boolean param2) {
        super(param0);
        this.damage = param1;
        this.add = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_DAMAGE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.damage.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (param0.isDamageableItem()) {
            int var0 = param0.getMaxDamage();
            float var1 = this.add ? 1.0F - (float)param0.getDamageValue() / (float)var0 : 0.0F;
            float var2 = 1.0F - Mth.clamp(this.damage.getFloat(param1) + var1, 0.0F, 1.0F);
            param0.setDamageValue(Mth.floor(var2 * (float)var0));
        } else {
            LOGGER.warn("Couldn't set damage of loot item {}", param0);
        }

        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider param0) {
        return simpleBuilder(param1 -> new SetItemDamageFunction(param1, param0, false));
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider param0, boolean param1) {
        return simpleBuilder(param2 -> new SetItemDamageFunction(param2, param0, param1));
    }
}
