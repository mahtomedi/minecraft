package com.mojang.realmsclient.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsConstants {
    public static int row(int param0) {
        return 40 + param0 * 13;
    }
}
