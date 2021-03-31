package net.minecraft.client.main;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SilentInitException extends RuntimeException {
    public SilentInitException(String param0) {
        super(param0);
    }

    public SilentInitException(String param0, Throwable param1) {
        super(param0, param1);
    }
}
