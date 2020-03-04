package net.minecraft.client.gui.components.events;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ContainerEventHandler extends GuiEventListener {
    List<? extends GuiEventListener> children();

    default Optional<GuiEventListener> getChildAt(double param0, double param1) {
        for(GuiEventListener var0 : this.children()) {
            if (var0.isMouseOver(param0, param1)) {
                return Optional.of(var0);
            }
        }

        return Optional.empty();
    }

    @Override
    default boolean mouseClicked(double param0, double param1, int param2) {
        for(GuiEventListener var0 : this.children()) {
            if (var0.mouseClicked(param0, param1, param2)) {
                this.setFocused(var0);
                if (param2 == 0) {
                    this.setDragging(true);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    default boolean mouseReleased(double param0, double param1, int param2) {
        this.setDragging(false);
        return this.getChildAt(param0, param1).filter(param3 -> param3.mouseReleased(param0, param1, param2)).isPresent();
    }

    @Override
    default boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        return this.getFocused() != null && this.isDragging() && param2 == 0 ? this.getFocused().mouseDragged(param0, param1, param2, param3, param4) : false;
    }

    boolean isDragging();

    void setDragging(boolean var1);

    @Override
    default boolean mouseScrolled(double param0, double param1, double param2) {
        return this.getChildAt(param0, param1).filter(param3 -> param3.mouseScrolled(param0, param1, param2)).isPresent();
    }

    @Override
    default boolean keyPressed(int param0, int param1, int param2) {
        return this.getFocused() != null && this.getFocused().keyPressed(param0, param1, param2);
    }

    @Override
    default boolean keyReleased(int param0, int param1, int param2) {
        return this.getFocused() != null && this.getFocused().keyReleased(param0, param1, param2);
    }

    @Override
    default boolean charTyped(char param0, int param1) {
        return this.getFocused() != null && this.getFocused().charTyped(param0, param1);
    }

    @Nullable
    GuiEventListener getFocused();

    void setFocused(@Nullable GuiEventListener var1);

    default void setInitialFocus(@Nullable GuiEventListener param0) {
        this.setFocused(param0);
        param0.changeFocus(true);
    }

    default void magicalSpecialHackyFocus(@Nullable GuiEventListener param0) {
        this.setFocused(param0);
    }

    @Override
    default boolean changeFocus(boolean param0) {
        GuiEventListener var0 = this.getFocused();
        boolean var1 = var0 != null;
        if (var1 && var0.changeFocus(param0)) {
            return true;
        } else {
            List<? extends GuiEventListener> var2 = this.children();
            int var3 = var2.indexOf(var0);
            int var4;
            if (var1 && var3 >= 0) {
                var4 = var3 + (param0 ? 1 : 0);
            } else if (param0) {
                var4 = 0;
            } else {
                var4 = var2.size();
            }

            ListIterator<? extends GuiEventListener> var7 = var2.listIterator(var4);
            BooleanSupplier var8 = param0 ? var7::hasNext : var7::hasPrevious;
            Supplier<? extends GuiEventListener> var9 = param0 ? var7::next : var7::previous;

            while(var8.getAsBoolean()) {
                GuiEventListener var10 = var9.get();
                if (var10.changeFocus(param0)) {
                    this.setFocused(var10);
                    return true;
                }
            }

            this.setFocused(null);
            return false;
        }
    }
}
