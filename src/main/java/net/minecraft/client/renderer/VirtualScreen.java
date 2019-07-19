package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class VirtualScreen implements AutoCloseable {
    private final Minecraft minecraft;
    private final ScreenManager screenManager;

    public VirtualScreen(Minecraft param0) {
        this.minecraft = param0;
        this.screenManager = new ScreenManager(Monitor::new);
    }

    public Window newWindow(DisplayData param0, String param1, String param2) {
        return new Window(this.minecraft, this.screenManager, param0, param1, param2);
    }

    @Override
    public void close() {
        this.screenManager.shutdown();
    }
}
