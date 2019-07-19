package net.minecraft.client.searchtree;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface MutableSearchTree<T> extends SearchTree<T> {
    void add(T var1);

    void clear();

    void refresh();
}
