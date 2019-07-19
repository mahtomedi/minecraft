package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NbtPredicate {
    public static final NbtPredicate ANY = new NbtPredicate(null);
    @Nullable
    private final CompoundTag tag;

    public NbtPredicate(@Nullable CompoundTag param0) {
        this.tag = param0;
    }

    public boolean matches(ItemStack param0) {
        return this == ANY ? true : this.matches(param0.getTag());
    }

    public boolean matches(Entity param0) {
        return this == ANY ? true : this.matches(getEntityTagToCompare(param0));
    }

    public boolean matches(@Nullable Tag param0) {
        if (param0 == null) {
            return this == ANY;
        } else {
            return this.tag == null || NbtUtils.compareNbt(this.tag, param0, true);
        }
    }

    public JsonElement serializeToJson() {
        return (JsonElement)(this != ANY && this.tag != null ? new JsonPrimitive(this.tag.toString()) : JsonNull.INSTANCE);
    }

    public static NbtPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            CompoundTag var0;
            try {
                var0 = TagParser.parseTag(GsonHelper.convertToString(param0, "nbt"));
            } catch (CommandSyntaxException var3) {
                throw new JsonSyntaxException("Invalid nbt tag: " + var3.getMessage());
            }

            return new NbtPredicate(var0);
        } else {
            return ANY;
        }
    }

    public static CompoundTag getEntityTagToCompare(Entity param0) {
        CompoundTag var0 = param0.saveWithoutId(new CompoundTag());
        if (param0 instanceof Player) {
            ItemStack var1 = ((Player)param0).inventory.getSelected();
            if (!var1.isEmpty()) {
                var0.put("SelectedItem", var1.save(new CompoundTag()));
            }
        }

        return var0;
    }
}
