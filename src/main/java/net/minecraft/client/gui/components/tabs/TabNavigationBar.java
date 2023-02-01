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
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TabNavigationBar extends GridLayout {
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

    public void setInitialTab(Tab param0) {
        this.selectTab(Optional.ofNullable(this.tabButtons.get(param0)), param0);
    }

    public void setInitialTab(int param0) {
        this.setInitialTab(this.tabs.get(param0));
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
