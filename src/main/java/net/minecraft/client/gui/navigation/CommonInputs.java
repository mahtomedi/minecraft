package net.minecraft.client.gui.navigation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommonInputs {
    public static boolean selected(int param0) {
        return param0 == 257 || param0 == 32 || param0 == 335;
    }
}
