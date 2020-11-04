package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.authlib.GameProfile;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FillPlayerHead extends LootItemConditionalFunction {
    private final LootContext.EntityTarget entityTarget;

    public FillPlayerHead(LootItemCondition[] param0, LootContext.EntityTarget param1) {
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
            Entity var0 = param1.getParamOrNull(this.entityTarget.getParam());
            if (var0 instanceof Player) {
                GameProfile var1 = ((Player)var0).getGameProfile();
                param0.getOrCreateTag().put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), var1));
            }
        }

        return param0;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<FillPlayerHead> {
        public void serialize(JsonObject param0, FillPlayerHead param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.add("entity", param2.serialize(param1.entityTarget));
        }

        public FillPlayerHead deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            LootContext.EntityTarget var0 = GsonHelper.getAsObject(param0, "entity", param1, LootContext.EntityTarget.class);
            return new FillPlayerHead(param2, var0);
        }
    }
}
