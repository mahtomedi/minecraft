package net.minecraft.realms;

import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RealmListEntry extends ObjectSelectionList.Entry<RealmListEntry> {
    @Override
    public abstract void render(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8, float var9);

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return false;
    }
}
