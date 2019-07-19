package net.minecraft.client.player.inventory;

import com.google.common.collect.ForwardingList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Hotbar extends ForwardingList<ItemStack> {
    private final NonNullList<ItemStack> items = NonNullList.withSize(Inventory.getSelectionSize(), ItemStack.EMPTY);

    @Override
    protected List<ItemStack> delegate() {
        return this.items;
    }

    public ListTag createTag() {
        ListTag var0 = new ListTag();

        for(ItemStack var1 : this.delegate()) {
            var0.add(var1.save(new CompoundTag()));
        }

        return var0;
    }

    public void fromTag(ListTag param0) {
        List<ItemStack> var0 = this.delegate();

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            var0.set(var1, ItemStack.of(param0.getCompound(var1)));
        }

    }

    @Override
    public boolean isEmpty() {
        for(ItemStack var0 : this.delegate()) {
            if (!var0.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
