package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record NbtPredicate(CompoundTag tag) {
    public static final Codec<NbtPredicate> CODEC = TagParser.AS_CODEC.xmap(NbtPredicate::new, NbtPredicate::tag);

    public boolean matches(ItemStack param0) {
        return this.matches(param0.getTag());
    }

    public boolean matches(Entity param0) {
        return this.matches(getEntityTagToCompare(param0));
    }

    public boolean matches(@Nullable Tag param0) {
        return param0 != null && NbtUtils.compareNbt(this.tag, param0, true);
    }

    public static CompoundTag getEntityTagToCompare(Entity param0) {
        CompoundTag var0 = param0.saveWithoutId(new CompoundTag());
        if (param0 instanceof Player) {
            ItemStack var1 = ((Player)param0).getInventory().getSelected();
            if (!var1.isEmpty()) {
                var0.put("SelectedItem", var1.save(new CompoundTag()));
            }
        }

        return var0;
    }
}
