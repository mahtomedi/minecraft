package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldCreationTask extends LongRunningTask {
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
        this.setTitle(new TranslatableComponent("mco.create.world.wait"));
        RealmsClient var0 = RealmsClient.create();

        try {
            var0.initializeWorld(this.worldId, this.name, this.motd);
            setScreen(this.lastScreen);
        } catch (RealmsServiceException var3) {
            LOGGER.error("Couldn't create world");
            this.error(var3.toString());
        } catch (Exception var4) {
            LOGGER.error("Could not create world");
            this.error(var4.getLocalizedMessage());
        }

    }
}
