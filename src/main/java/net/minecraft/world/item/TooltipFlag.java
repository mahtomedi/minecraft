package net.minecraft.world.item;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface TooltipFlag {
    boolean isAdvanced();

    @OnlyIn(Dist.CLIENT)
    public static enum Default implements TooltipFlag {
        NORMAL(false),
        ADVANCED(true);

        private final boolean advanced;

        private Default(boolean param0) {
            this.advanced = param0;
        }

        @Override
        public boolean isAdvanced() {
            return this.advanced;
        }
    }
}
