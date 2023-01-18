package net.minecraft.client.gui.components.events;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2i;

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
        GuiEventListener var0 = null;

        for(GuiEventListener var2 : List.copyOf(this.children())) {
            if (var2.mouseClicked(param0, param1, param2)) {
                var0 = var2;
            }
        }

        if (var0 != null) {
            this.setFocused(var0);
            if (param2 == 0) {
                this.setDragging(true);
            }

            return true;
        } else {
            return false;
        }
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

    @Override
    default void setFocused(boolean param0) {
    }

    @Override
    default boolean isFocused() {
        return this.getFocused() != null;
    }

    @Nullable
    @Override
    default ComponentPath getCurrentFocusPath() {
        GuiEventListener var0 = this.getFocused();
        return var0 != null ? ComponentPath.path(this, var0.getCurrentFocusPath()) : null;
    }

    default void magicalSpecialHackyFocus(@Nullable GuiEventListener param0) {
        this.setFocused(param0);
    }

    @Nullable
    @Override
    default ComponentPath nextFocusPath(FocusNavigationEvent param0) {
        GuiEventListener var0 = this.getFocused();
        if (var0 != null) {
            ComponentPath var1 = var0.nextFocusPath(param0);
            if (var1 != null) {
                return ComponentPath.path(this, var1);
            }
        }

        if (param0 instanceof FocusNavigationEvent.TabNavigation var2) {
            return this.handleTabNavigation(var2);
        } else {
            return param0 instanceof FocusNavigationEvent.ArrowNavigation var3 ? this.handleArrowNavigation(var3) : null;
        }
    }

    @Nullable
    private ComponentPath handleTabNavigation(FocusNavigationEvent.TabNavigation param0) {
        boolean var0 = param0.forward();
        GuiEventListener var1 = this.getFocused();
        List<? extends GuiEventListener> var2 = this.children();
        int var3 = var2.indexOf(var1);
        int var4;
        if (var1 != null && var3 >= 0) {
            var4 = var3 + (var0 ? 1 : 0);
        } else if (var0) {
            var4 = 0;
        } else {
            var4 = var2.size();
        }

        ListIterator<? extends GuiEventListener> var7 = var2.listIterator(var4);
        BooleanSupplier var8 = var0 ? var7::hasNext : var7::hasPrevious;
        Supplier<? extends GuiEventListener> var9 = var0 ? var7::next : var7::previous;

        while(var8.getAsBoolean()) {
            GuiEventListener var10 = var9.get();
            ComponentPath var11 = var10.nextFocusPath(param0);
            if (var11 != null) {
                return ComponentPath.path(this, var11);
            }
        }

        return null;
    }

    @Nullable
    private ComponentPath handleArrowNavigation(FocusNavigationEvent.ArrowNavigation param0) {
        GuiEventListener var0 = this.getFocused();
        if (var0 == null) {
            ScreenDirection var1 = param0.direction();
            ScreenRectangle var2 = this.getRectangle().getBorder(var1.getOpposite());
            return ComponentPath.path(this, this.nextFocusPathInDirection(var2, var1, null, param0));
        } else {
            ScreenRectangle var3 = var0.getRectangle();
            return ComponentPath.path(this, this.nextFocusPathInDirection(var3, param0.direction(), var0, param0));
        }
    }

    @Nullable
    private ComponentPath nextFocusPathInDirection(
        ScreenRectangle param0, ScreenDirection param1, @Nullable GuiEventListener param2, FocusNavigationEvent param3
    ) {
        ScreenAxis var0 = param1.getAxis();
        ScreenAxis var1 = var0.orthogonal();
        ScreenDirection var2 = var1.getPositive();
        int var3 = param0.getBoundInDirection(param1.getOpposite());
        List<GuiEventListener> var4 = new ArrayList<>();

        for(GuiEventListener var5 : this.children()) {
            if (var5 != param2) {
                ScreenRectangle var6 = var5.getRectangle();
                if (var6.overlapsInAxis(param0, var1)) {
                    int var7 = var6.getBoundInDirection(param1.getOpposite());
                    if (param1.isAfter(var7, var3)) {
                        var4.add(var5);
                    } else if (var7 == var3 && param1.isAfter(var6.getBoundInDirection(param1), param0.getBoundInDirection(param1))) {
                        var4.add(var5);
                    }
                }
            }
        }

        Comparator<GuiEventListener> var8 = Comparator.comparing(
            param1x -> param1x.getRectangle().getBoundInDirection(param1.getOpposite()), param1.coordinateValueComparator()
        );
        Comparator<GuiEventListener> var9 = Comparator.comparing(
            param1x -> param1x.getRectangle().getBoundInDirection(var2.getOpposite()), var2.coordinateValueComparator()
        );
        var4.sort(var8.thenComparing(var9));

        for(GuiEventListener var10 : var4) {
            ComponentPath var11 = var10.nextFocusPath(param3);
            if (var11 != null) {
                return var11;
            }
        }

        return this.nextFocusPathVaguelyInDirection(param0, param1, param2, param3);
    }

    @Nullable
    private ComponentPath nextFocusPathVaguelyInDirection(
        ScreenRectangle param0, ScreenDirection param1, @Nullable GuiEventListener param2, FocusNavigationEvent param3
    ) {
        ScreenAxis var0 = param1.getAxis();
        ScreenAxis var1 = var0.orthogonal();
        List<Pair<GuiEventListener, Long>> var2 = new ArrayList<>();
        ScreenPosition var3 = ScreenPosition.of(var0, param0.getBoundInDirection(param1), param0.getCenterInAxis(var1));

        for(GuiEventListener var4 : this.children()) {
            if (var4 != param2) {
                ScreenRectangle var5 = var4.getRectangle();
                ScreenPosition var6 = ScreenPosition.of(var0, var5.getBoundInDirection(param1.getOpposite()), var5.getCenterInAxis(var1));
                if (param1.isAfter(var6.getCoordinate(var0), var3.getCoordinate(var0))) {
                    long var7 = Vector2i.distanceSquared(var3.x(), var3.y(), var6.x(), var6.y());
                    var2.add(Pair.of(var4, var7));
                }
            }
        }

        var2.sort(Comparator.comparingDouble(Pair::getSecond));

        for(Pair<GuiEventListener, Long> var8 : var2) {
            ComponentPath var9 = var8.getFirst().nextFocusPath(param3);
            if (var9 != null) {
                return var9;
            }
        }

        return null;
    }
}
