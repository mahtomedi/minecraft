package net.minecraft.client.gui.components.tabs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TabNavigationBar extends GridLayout {
    private static final int NO_TAB = -1;
    private int width;
    private final TabManager tabManager;
    private final ImmutableList<Tab> tabs;
    private final ImmutableMap<Tab, Button> tabButtons;

    public void setWidth(int param0) {
        this.width = param0;
    }

    public static TabNavigationBar.Builder builder(TabManager param0, int param1) {
        return new TabNavigationBar.Builder(param0, param1);
    }

    TabNavigationBar(int param0, int param1, int param2, TabManager param3, Iterable<Tab> param4) {
        super(param0, param1);
        this.width = param2;
        this.tabManager = param3;
        this.tabs = ImmutableList.copyOf(param4);
        ImmutableMap.Builder<Tab, Button> var0 = ImmutableMap.builder();
        int var1 = 0;

        for(Tab var2 : param4) {
            Button var3 = Button.builder(var2.getTabTitle(), param1x -> this.selectTab(Optional.of(param1x), var2))
                .createNarration(param1x -> Component.translatable("gui.narrate.tab", var2.getTabTitle()))
                .build();
            var0.put(var2, this.addChild(var3, 0, var1++));
        }

        this.tabButtons = var0.build();
        this.arrangeElements();
    }

    @Override
    public void arrangeElements() {
        Divisor var0 = new Divisor(this.width, this.tabs.size());

        for(Button var1 : this.tabButtons.values()) {
            var1.setWidth(var0.nextInt());
        }

        super.arrangeElements();
    }

    private void selectTab(Optional<Button> param0, Tab param1) {
        this.tabButtons.values().forEach(param0x -> param0x.active = true);
        param0.ifPresent(param0x -> param0x.active = false);
        this.tabManager.setCurrentTab(param1);
    }

    public void selectTab(Tab param0) {
        this.selectTab(Optional.ofNullable(this.tabButtons.get(param0)), param0);
    }

    public void selectTab(int param0) {
        this.selectTab(this.tabs.get(param0));
    }

    public boolean keyPressed(int param0) {
        if (Screen.hasControlDown()) {
            int var0 = this.getNextTabIndex(param0);
            if (var0 != -1) {
                this.selectTab(Mth.clamp(var0, 0, this.tabs.size() - 1));
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

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private int x = 0;
        private int y = 0;
        private int width;
        private final TabManager tabManager;
        private final List<Tab> tabs = new ArrayList<>();

        Builder(TabManager param0, int param1) {
            this.tabManager = param0;
            this.width = param1;
        }

        public TabNavigationBar.Builder addTab(Tab param0) {
            this.tabs.add(param0);
            return this;
        }

        public TabNavigationBar.Builder addTabs(Tab... param0) {
            Collections.addAll(this.tabs, param0);
            return this;
        }

        public TabNavigationBar.Builder setX(int param0) {
            this.x = param0;
            return this;
        }

        public TabNavigationBar.Builder setY(int param0) {
            this.y = param0;
            return this;
        }

        public TabNavigationBar.Builder setPosition(int param0, int param1) {
            return this.setX(param0).setY(param1);
        }

        public TabNavigationBar.Builder setWidth(int param0) {
            this.width = param0;
            return this;
        }

        public TabNavigationBar build() {
            return new TabNavigationBar(this.x, this.y, this.width, this.tabManager, this.tabs);
        }
    }
}
