package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FillPlayerHead extends LootItemConditionalFunction {
    public static final Codec<FillPlayerHead> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(param0x -> param0x.entityTarget))
                .apply(param0, FillPlayerHead::new)
    );
    private final LootContext.EntityTarget entityTarget;

    public FillPlayerHead(List<LootItemCondition> param0, LootContext.EntityTarget param1) {
        super(param0);
        this.entityTarget = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.FILL_PLAYER_HEAD;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.entityTarget.getParam());
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (param0.is(Items.PLAYER_HEAD)) {
            Object var1 = param1.getParamOrNull(this.entityTarget.getParam());
            if (var1 instanceof Player var0) {
                GameProfile var1x = var0.getGameProfile();
                param0.getOrCreateTag().put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), var1x));
            }
        }

        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> fillPlayerHead(LootContext.EntityTarget param0) {
        return simpleBuilder(param1 -> new FillPlayerHead(param1, param0));
    }
}
