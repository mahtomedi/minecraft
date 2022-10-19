package net.minecraft.world.item;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Objects;

public final class ItemStackLinkedSet extends ObjectLinkedOpenCustomHashSet<ItemStack> {
    private static final Strategy<? super ItemStack> STRATEGY = new Strategy<ItemStack>() {
        public int hashCode(ItemStack param0) {
            return param0 != null ? Objects.hash(param0.getItem(), param0.getTag()) : 0;
        }

        public boolean equals(ItemStack param0, ItemStack param1) {
            return param0 == param1 || param0 != null && param1 != null && ItemStack.matches(param0, param1);
        }
    };

    public ItemStackLinkedSet() {
        super(STRATEGY);
    }
}
