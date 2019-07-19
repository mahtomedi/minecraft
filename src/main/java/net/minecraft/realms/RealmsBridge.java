package net.minecraft.realms;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsBridge extends RealmsScreen {
    private Screen previousScreen;

    public void switchToRealms(Screen param0) {
        this.previousScreen = param0;
        Realms.setScreen(new RealmsMainScreen(this));
    }

    @Nullable
    public RealmsScreenProxy getNotificationScreen(Screen param0) {
        this.previousScreen = param0;
        return new RealmsNotificationsScreen(this).getProxy();
    }

    @Override
    public void init() {
        Minecraft.getInstance().setScreen(this.previousScreen);
    }
}
