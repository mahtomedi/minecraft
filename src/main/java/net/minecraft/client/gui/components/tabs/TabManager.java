package net.minecraft.client.gui.components.tabs;

import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TabManager {
    private final Consumer<AbstractWidget> addWidget;
    private final Consumer<AbstractWidget> removeWidget;
    @Nullable
    private Tab currentTab;
    @Nullable
    private ScreenRectangle tabArea;

    public TabManager(Consumer<AbstractWidget> param0, Consumer<AbstractWidget> param1) {
        this.addWidget = param0;
        this.removeWidget = param1;
    }

    public void setTabArea(ScreenRectangle param0) {
        this.tabArea = param0;
        Tab var0 = this.getCurrentTab();
        if (var0 != null) {
            var0.doLayout(param0);
        }

    }

    public void setCurrentTab(Tab param0, boolean param1) {
        if (!Objects.equals(this.currentTab, param0)) {
            if (this.currentTab != null) {
                this.currentTab.visitChildren(this.removeWidget);
            }

            this.currentTab = param0;
            param0.visitChildren(this.addWidget);
            if (this.tabArea != null) {
                param0.doLayout(this.tabArea);
            }

            if (param1) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }

    }

    @Nullable
    public Tab getCurrentTab() {
        return this.currentTab;
    }
}
