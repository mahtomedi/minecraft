package com.mojang.realmsclient.gui;

import java.util.List;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RowButton {
    public final int width;
    public final int height;
    public final int xOffset;
    public final int yOffset;

    public RowButton(int param0, int param1, int param2, int param3) {
        this.width = param0;
        this.height = param1;
        this.xOffset = param2;
        this.yOffset = param3;
    }

    public void drawForRowAt(int param0, int param1, int param2, int param3) {
        int var0 = param0 + this.xOffset;
        int var1 = param1 + this.yOffset;
        boolean var2 = false;
        if (param2 >= var0 && param2 <= var0 + this.width && param3 >= var1 && param3 <= var1 + this.height) {
            var2 = true;
        }

        this.draw(var0, var1, var2);
    }

    protected abstract void draw(int var1, int var2, boolean var3);

    public int getRight() {
        return this.xOffset + this.width;
    }

    public int getBottom() {
        return this.yOffset + this.height;
    }

    public abstract void onClick(int var1);

    public static void drawButtonsInRow(List<RowButton> param0, RealmsObjectSelectionList<?> param1, int param2, int param3, int param4, int param5) {
        for(RowButton var0 : param0) {
            if (param1.getRowWidth() > var0.getRight()) {
                var0.drawForRowAt(param2, param3, param4, param5);
            }
        }

    }

    public static void rowButtonMouseClicked(
        RealmsObjectSelectionList<?> param0, ObjectSelectionList.Entry<?> param1, List<RowButton> param2, int param3, double param4, double param5
    ) {
        if (param3 == 0) {
            int var0 = param0.children().indexOf(param1);
            if (var0 > -1) {
                param0.selectItem(var0);
                int var1 = param0.getRowLeft();
                int var2 = param0.getRowTop(var0);
                int var3 = (int)(param4 - (double)var1);
                int var4 = (int)(param5 - (double)var2);

                for(RowButton var5 : param2) {
                    if (var3 >= var5.xOffset && var3 <= var5.getRight() && var4 >= var5.yOffset && var4 <= var5.getBottom()) {
                        var5.onClick(var0);
                    }
                }
            }
        }

    }
}
