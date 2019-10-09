package net.minecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.BooleanSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToggleKeyMapping extends KeyMapping {
    private final BooleanSupplier needsToggle;

    public ToggleKeyMapping(String param0, int param1, String param2, BooleanSupplier param3) {
        super(param0, InputConstants.Type.KEYSYM, param1, param2);
        this.needsToggle = param3;
    }

    @Override
    public void setDown(boolean param0) {
        if (this.needsToggle.getAsBoolean()) {
            if (param0) {
                super.setDown(!this.isDown());
            }
        } else {
            super.setDown(param0);
        }

    }
}
