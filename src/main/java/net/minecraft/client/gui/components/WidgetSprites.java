package net.minecraft.client.gui.components;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record WidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused, ResourceLocation disabledFocused) {
    public WidgetSprites(ResourceLocation param0, ResourceLocation param1) {
        this(param0, param0, param1, param1);
    }

    public WidgetSprites(ResourceLocation param0, ResourceLocation param1, ResourceLocation param2) {
        this(param0, param1, param2, param1);
    }

    public ResourceLocation get(boolean param0, boolean param1) {
        if (param0) {
            return param1 ? this.enabledFocused : this.enabled;
        } else {
            return param1 ? this.disabledFocused : this.disabled;
        }
    }
}
