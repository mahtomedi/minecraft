package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<FunctionReference> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0).and(ResourceLocation.CODEC.fieldOf("name").forGetter(param0x -> param0x.name)).apply(param0, FunctionReference::new)
    );
    private final ResourceLocation name;

    private FunctionReference(List<LootItemCondition> param0, ResourceLocation param1) {
        super(param0);
        this.name = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.REFERENCE;
    }

    @Override
    public void validate(ValidationContext param0) {
        LootDataId<LootItemFunction> var0 = new LootDataId<>(LootDataType.MODIFIER, this.name);
        if (param0.hasVisitedElement(var0)) {
            param0.reportProblem("Function " + this.name + " is recursively called");
        } else {
            super.validate(param0);
            param0.resolver()
                .getElementOptional(var0)
                .ifPresentOrElse(
                    param2 -> param2.validate(param0.enterElement(".{" + this.name + "}", var0)),
                    () -> param0.reportProblem("Unknown function table called " + this.name)
                );
        }
    }

    @Override
    protected ItemStack run(ItemStack param0, LootContext param1) {
        LootItemFunction var0 = param1.getResolver().getElement(LootDataType.MODIFIER, this.name);
        if (var0 == null) {
            LOGGER.warn("Unknown function: {}", this.name);
            return param0;
        } else {
            LootContext.VisitedEntry<?> var1 = LootContext.createVisitedEntry(var0);
            if (param1.pushVisitedElement(var1)) {
                ItemStack var5;
                try {
                    var5 = var0.apply(param0, param1);
                } finally {
                    param1.popVisitedElement(var1);
                }

                return var5;
            } else {
                LOGGER.warn("Detected infinite loop in loot tables");
                return param0;
            }
        }
    }

    public static LootItemConditionalFunction.Builder<?> functionReference(ResourceLocation param0) {
        return simpleBuilder(param1 -> new FunctionReference(param1, param0));
    }
}
