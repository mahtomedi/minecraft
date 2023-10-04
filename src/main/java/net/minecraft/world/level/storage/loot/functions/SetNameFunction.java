package net.minecraft.world.level.storage.loot.functions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class SetNameFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<SetNameFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        ExtraCodecs.strictOptionalField(ComponentSerialization.CODEC, "name").forGetter(param0x -> param0x.name),
                        ExtraCodecs.strictOptionalField(LootContext.EntityTarget.CODEC, "entity").forGetter(param0x -> param0x.resolutionContext)
                    )
                )
                .apply(param0, SetNameFunction::new)
    );
    private final Optional<Component> name;
    private final Optional<LootContext.EntityTarget> resolutionContext;

    private SetNameFunction(List<LootItemCondition> param0, Optional<Component> param1, Optional<LootContext.EntityTarget> param2) {
        super(param0);
        this.name = param1;
        this.resolutionContext = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_NAME;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.resolutionContext.<Set<LootContextParam<?>>>map(param0 -> Set.of(param0.getParam())).orElse(Set.of());
    }

    public static UnaryOperator<Component> createResolver(LootContext param0, @Nullable LootContext.EntityTarget param1) {
        if (param1 != null) {
            Entity var0 = param0.getParamOrNull(param1.getParam());
            if (var0 != null) {
                CommandSourceStack var1 = var0.createCommandSourceStack().withPermission(2);
                return param2 -> {
                    try {
                        return ComponentUtils.updateForEntity(var1, param2, var0, 0);
                    } catch (CommandSyntaxException var4) {
                        LOGGER.warn("Failed to resolve text component", (Throwable)var4);
                        return param2;
                    }
                };
            }
        }

        return param0x -> param0x;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        this.name.ifPresent(param2 -> param0.setHoverName(createResolver(param1, this.resolutionContext.orElse(null)).apply(param2)));
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component param0) {
        return simpleBuilder(param1 -> new SetNameFunction(param1, Optional.of(param0), Optional.empty()));
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component param0, LootContext.EntityTarget param1) {
        return simpleBuilder(param2 -> new SetNameFunction(param2, Optional.of(param0), Optional.of(param1)));
    }
}
