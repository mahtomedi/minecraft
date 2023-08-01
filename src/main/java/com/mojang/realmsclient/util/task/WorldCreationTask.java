package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldCreationTask extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;
    private final String motd;
    private final long worldId;
    private final Screen lastScreen;

    public WorldCreationTask(long param0, String param1, String param2, Screen param3) {
        this.worldId = param0;
        this.name = param1;
        this.motd = param2;
        this.lastScreen = param3;
    }

    @Override
    public void run() {
        this.setTitle(Component.translatable("mco.create.world.wait"));
        RealmsClient var0 = RealmsClient.create();

        try {
            var0.initializeWorld(this.worldId, this.name, this.motd);
            setScreen(this.lastScreen);
        } catch (RealmsServiceException var3) {
            LOGGER.error("Couldn't create world", (Throwable)var3);
            this.error(var3);
        } catch (Exception var4) {
            LOGGER.error("Could not create world", (Throwable)var4);
            this.error(var4);
        }

    }
}
