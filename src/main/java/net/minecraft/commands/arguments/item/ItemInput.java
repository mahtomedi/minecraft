package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemInput implements Predicate<ItemStack> {
    private static final Dynamic2CommandExceptionType ERROR_STACK_TOO_BIG = new Dynamic2CommandExceptionType(
        (param0, param1) -> new TranslatableComponent("arguments.item.overstacked", param0, param1)
    );
    private final Item item;
    @Nullable
    private final CompoundTag tag;

    public ItemInput(Item param0, @Nullable CompoundTag param1) {
        this.item = param0;
        this.tag = param1;
    }

    public Item getItem() {
        return this.item;
    }

    public boolean test(ItemStack param0) {
        return param0.getItem() == this.item && NbtUtils.compareNbt(this.tag, param0.getTag(), true);
    }

    public ItemStack createItemStack(int param0, boolean param1) throws CommandSyntaxException {
        ItemStack var0 = new ItemStack(this.item, param0);
        if (this.tag != null) {
            var0.setTag(this.tag);
        }

        if (param1 && param0 > var0.getMaxStackSize()) {
            throw ERROR_STACK_TOO_BIG.create(Registry.ITEM.getKey(this.item), var0.getMaxStackSize());
        } else {
            return var0;
        }
    }

    public String serialize() {
        StringBuilder var0 = new StringBuilder(Registry.ITEM.getId(this.item));
        if (this.tag != null) {
            var0.append(this.tag);
        }

        return var0.toString();
    }
}
