package net.minecraft.client.gui.components.tabs;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class TabNavigationBar extends AbstractContainerEventHandler implements Renderable, GuiEventListener, NarratableEntry {
    private static final int NO_TAB = -1;
    private static final int MAX_WIDTH = 400;
    private static final int HEIGHT = 24;
    private static final int MARGIN = 14;
    private static final Component USAGE_NARRATION = Component.translatable("narration.tab_navigation.usage");
    private final GridLayout layout;
    private int width;
    private final TabManager tabManager;
    private final ImmutableList<Tab> tabs;
    private final ImmutableList<TabButton> tabButtons;

    TabNavigationBar(int param0, TabManager param1, Iterable<Tab> param2) {
        this.width = param0;
        this.tabManager = param1;
        this.tabs = ImmutableList.copyOf(param2);
        this.layout = new GridLayout(0, 0);
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        ImmutableList.Builder<TabButton> var0 = ImmutableList.builder();
        int var1 = 0;

        for(Tab var2 : param2) {
            var0.add(this.layout.addChild(new TabButton(param1, var2, 0, 24), 0, var1++));
        }

        this.tabButtons = var0.build();
    }

    public static TabNavigationBar.Builder builder(TabManager param0, int param1) {
        return new TabNavigationBar.Builder(param0, param1);
    }

    public void setWidth(int param0) {
        this.width = param0;
    }

    @Override
    public void setFocused(boolean param0) {
        super.setFocused(param0);
        if (this.getFocused() != null) {
            this.getFocused().setFocused(param0);
        }

    }

    @Override
    public void setFocused(@Nullable GuiEventListener param0) {
        super.setFocused(param0);
        if (param0 instanceof TabButton var0) {
            this.tabManager.setCurrentTab(var0.tab(), true);
        }

    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent param0) {
        if (!this.isFocused()) {
            TabButton var0 = this.currentTabButton();
            if (var0 != null) {
                return ComponentPath.path(this, ComponentPath.leaf(var0));
            }
        }

        return param0 instanceof FocusNavigationEvent.TabNavigation ? null : super.nextFocusPath(param0);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.tabButtons;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.tabButtons.stream().map(AbstractWidget::narrationPriority).max(Comparator.naturalOrder()).orElse(NarratableEntry.NarrationPriority.NONE);
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
        Optional<TabButton> var0 = this.tabButtons
            .stream()
            .filter(AbstractWidget::isHovered)
            .findFirst()
            .or(() -> Optional.ofNullable(this.currentTabButton()));
        var0.ifPresent(param1 -> {
            this.narrateListElementPosition(param0.nest(), param1);
            param1.updateNarration(param0);
        });
        if (this.isFocused()) {
            param0.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }

    }

    protected void narrateListElementPosition(NarrationElementOutput param0, TabButton param1) {
        if (this.tabs.size() > 1) {
            int var0 = this.tabButtons.indexOf(param1);
            if (var0 != -1) {
                param0.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.tab", var0 + 1, this.tabs.size()));
            }
        }

    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        param0.fill(0, 0, this.width, 24, -16777216);
        param0.blit(CreateWorldScreen.HEADER_SEPERATOR, 0, this.layout.getY() + this.layout.getHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);

        for(TabButton var0 : this.tabButtons) {
            var0.render(param0, param1, param2, param3);
        }

    }

    @Override
    public ScreenRectangle getRectangle() {
        return this.layout.getRectangle();
    }

    public void arrangeElements() {
        int var0 = Math.min(400, this.width) - 28;
        int var1 = Mth.roundToward(var0 / this.tabs.size(), 2);

        for(TabButton var2 : this.tabButtons) {
            var2.setWidth(var1);
        }

        this.layout.arrangeElements();
        this.layout.setX(Mth.roundToward((this.width - var0) / 2, 2));
        this.layout.setY(0);
    }

    public void selectTab(int param0, boolean param1) {
        if (this.isFocused()) {
            this.setFocused(this.tabButtons.get(param0));
        } else {
            this.tabManager.setCurrentTab(this.tabs.get(param0), param1);
        }

    }

    public boolean keyPressed(int param0) {
        if (Screen.hasControlDown()) {
            int var0 = this.getNextTabIndex(param0);
            if (var0 != -1) {
                this.selectTab(Mth.clamp(var0, 0, this.tabs.size() - 1), true);
                return true;
            }
        }

        return false;
    }

    private int getNextTabIndex(int param0) {
        if (param0 >= 49 && param0 <= 57) {
            return param0 - 49;
        } else {
            if (param0 == 258) {
                int var0 = this.currentTabIndex();
                if (var0 != -1) {
                    int var1 = Screen.hasShiftDown() ? var0 - 1 : var0 + 1;
                    return Math.floorMod(var1, this.tabs.size());
                }
            }

            return -1;
        }
    }

    private int currentTabIndex() {
        Tab var0 = this.tabManager.getCurrentTab();
        int var1 = this.tabs.indexOf(var0);
        return var1 != -1 ? var1 : -1;
    }

    @Nullable
    private TabButton currentTabButton() {
        int var0 = this.currentTabIndex();
        return var0 != -1 ? this.tabButtons.get(var0) : null;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final int width;
        private final TabManager tabManager;
        private final List<Tab> tabs = new ArrayList<>();

        Builder(TabManager param0, int param1) {
            this.tabManager = param0;
            this.width = param1;
        }

        public TabNavigationBar.Builder addTabs(Tab... param0) {
            Collections.addAll(this.tabs, param0);
            return this;
        }

        public TabNavigationBar build() {
            return new TabNavigationBar(this.width, this.tabManager, this.tabs);
        }
    }
}
