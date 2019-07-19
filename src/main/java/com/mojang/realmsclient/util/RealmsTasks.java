package com.mojang.realmsclient.util;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.LongRunningTask;
import com.mojang.realmsclient.gui.screens.RealmsBrokenWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsResourcePackScreen;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsConfirmResultListener;
import net.minecraft.realms.RealmsConnect;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsTasks {
    private static final Logger LOGGER = LogManager.getLogger();

    private static void pause(int param0) {
        try {
            Thread.sleep((long)(param0 * 1000));
        } catch (InterruptedException var2) {
            LOGGER.error("", (Throwable)var2);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class CloseServerTask extends LongRunningTask {
        private final RealmsServer serverData;
        private final RealmsConfigureWorldScreen configureScreen;

        public CloseServerTask(RealmsServer param0, RealmsConfigureWorldScreen param1) {
            this.serverData = param0;
            this.configureScreen = param1;
        }

        @Override
        public void run() {
            this.setTitle(RealmsScreen.getLocalizedString("mco.configure.world.closing"));
            RealmsClient var0 = RealmsClient.createRealmsClient();

            for(int var1 = 0; var1 < 25; ++var1) {
                if (this.aborted()) {
                    return;
                }

                try {
                    boolean var2 = var0.close(this.serverData.id);
                    if (var2) {
                        this.configureScreen.stateChanged();
                        this.serverData.state = RealmsServer.State.CLOSED;
                        Realms.setScreen(this.configureScreen);
                        break;
                    }
                } catch (RetryCallException var41) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.pause(var41.delaySeconds);
                } catch (Exception var5) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.LOGGER.error("Failed to close server", (Throwable)var5);
                    this.error("Failed to close the server");
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class DownloadTask extends LongRunningTask {
        private final long worldId;
        private final int slot;
        private final RealmsScreen lastScreen;
        private final String downloadName;

        public DownloadTask(long param0, int param1, String param2, RealmsScreen param3) {
            this.worldId = param0;
            this.slot = param1;
            this.lastScreen = param3;
            this.downloadName = param2;
        }

        @Override
        public void run() {
            this.setTitle(RealmsScreen.getLocalizedString("mco.download.preparing"));
            RealmsClient var0 = RealmsClient.createRealmsClient();
            int var1 = 0;

            while(var1 < 25) {
                try {
                    if (this.aborted()) {
                        return;
                    }

                    WorldDownload var2 = var0.download(this.worldId, this.slot);
                    RealmsTasks.pause(1);
                    if (this.aborted()) {
                        return;
                    }

                    Realms.setScreen(new RealmsDownloadLatestWorldScreen(this.lastScreen, var2, this.downloadName));
                    return;
                } catch (RetryCallException var4) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.pause(var4.delaySeconds);
                    ++var1;
                } catch (RealmsServiceException var51) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.LOGGER.error("Couldn't download world data");
                    Realms.setScreen(new RealmsGenericErrorScreen(var51, this.lastScreen));
                    return;
                } catch (Exception var6) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.LOGGER.error("Couldn't download world data", (Throwable)var6);
                    this.error(var6.getLocalizedMessage());
                    return;
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class OpenServerTask extends LongRunningTask {
        private final RealmsServer serverData;
        private final RealmsScreen returnScreen;
        private final boolean join;
        private final RealmsScreen mainScreen;

        public OpenServerTask(RealmsServer param0, RealmsScreen param1, RealmsScreen param2, boolean param3) {
            this.serverData = param0;
            this.returnScreen = param1;
            this.join = param3;
            this.mainScreen = param2;
        }

        @Override
        public void run() {
            this.setTitle(RealmsScreen.getLocalizedString("mco.configure.world.opening"));
            RealmsClient var0 = RealmsClient.createRealmsClient();

            for(int var1 = 0; var1 < 25; ++var1) {
                if (this.aborted()) {
                    return;
                }

                try {
                    boolean var2 = var0.open(this.serverData.id);
                    if (var2) {
                        if (this.returnScreen instanceof RealmsConfigureWorldScreen) {
                            ((RealmsConfigureWorldScreen)this.returnScreen).stateChanged();
                        }

                        this.serverData.state = RealmsServer.State.OPEN;
                        if (this.join) {
                            ((RealmsMainScreen)this.mainScreen).play(this.serverData, this.returnScreen);
                        } else {
                            Realms.setScreen(this.returnScreen);
                        }
                        break;
                    }
                } catch (RetryCallException var41) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.pause(var41.delaySeconds);
                } catch (Exception var5) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.LOGGER.error("Failed to open server", (Throwable)var5);
                    this.error("Failed to open the server");
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class RealmsConnectTask extends LongRunningTask {
        private final RealmsConnect realmsConnect;
        private final RealmsServerAddress a;

        public RealmsConnectTask(RealmsScreen param0, RealmsServerAddress param1) {
            this.a = param1;
            this.realmsConnect = new RealmsConnect(param0);
        }

        @Override
        public void run() {
            this.setTitle(RealmsScreen.getLocalizedString("mco.connect.connecting"));
            net.minecraft.realms.RealmsServerAddress var0 = net.minecraft.realms.RealmsServerAddress.parseString(this.a.address);
            this.realmsConnect.connect(var0.getHost(), var0.getPort());
        }

        @Override
        public void abortTask() {
            this.realmsConnect.abort();
            Realms.clearResourcePack();
        }

        @Override
        public void tick() {
            this.realmsConnect.tick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class RealmsGetServerDetailsTask extends LongRunningTask {
        private final RealmsServer server;
        private final RealmsScreen lastScreen;
        private final RealmsMainScreen mainScreen;
        private final ReentrantLock connectLock;

        public RealmsGetServerDetailsTask(RealmsMainScreen param0, RealmsScreen param1, RealmsServer param2, ReentrantLock param3) {
            this.lastScreen = param1;
            this.mainScreen = param0;
            this.server = param2;
            this.connectLock = param3;
        }

        @Override
        public void run() {
            this.setTitle(RealmsScreen.getLocalizedString("mco.connect.connecting"));
            RealmsClient var0 = RealmsClient.createRealmsClient();
            boolean var1 = false;
            boolean var2 = false;
            int var3 = 5;
            RealmsServerAddress var4 = null;
            boolean var5 = false;
            boolean var6 = false;

            for(int var7 = 0; var7 < 40 && !this.aborted(); ++var7) {
                try {
                    var4 = var0.join(this.server.id);
                    var1 = true;
                } catch (RetryCallException var10) {
                    var3 = var10.delaySeconds;
                } catch (RealmsServiceException var11) {
                    if (var11.errorCode == 6002) {
                        var5 = true;
                    } else if (var11.errorCode == 6006) {
                        var6 = true;
                    } else {
                        var2 = true;
                        this.error(var11.toString());
                        RealmsTasks.LOGGER.error("Couldn't connect to world", (Throwable)var11);
                    }
                    break;
                } catch (IOException var12) {
                    RealmsTasks.LOGGER.error("Couldn't parse response connecting to world", (Throwable)var12);
                } catch (Exception var13) {
                    var2 = true;
                    RealmsTasks.LOGGER.error("Couldn't connect to world", (Throwable)var13);
                    this.error(var13.getLocalizedMessage());
                    break;
                }

                if (var1) {
                    break;
                }

                this.sleep(var3);
            }

            if (var5) {
                Realms.setScreen(new RealmsTermsScreen(this.lastScreen, this.mainScreen, this.server));
            } else if (var6) {
                if (this.server.ownerUUID.equals(Realms.getUUID())) {
                    RealmsBrokenWorldScreen var12 = new RealmsBrokenWorldScreen(this.lastScreen, this.mainScreen, this.server.id);
                    if (this.server.worldType.equals(RealmsServer.WorldType.MINIGAME)) {
                        var12.setTitle(RealmsScreen.getLocalizedString("mco.brokenworld.minigame.title"));
                    }

                    Realms.setScreen(var12);
                } else {
                    Realms.setScreen(
                        new RealmsGenericErrorScreen(
                            RealmsScreen.getLocalizedString("mco.brokenworld.nonowner.title"),
                            RealmsScreen.getLocalizedString("mco.brokenworld.nonowner.error"),
                            this.lastScreen
                        )
                    );
                }
            } else if (!this.aborted() && !var2) {
                if (var1) {
                    if (var4.resourcePackUrl != null && var4.resourcePackHash != null) {
                        String var13 = RealmsScreen.getLocalizedString("mco.configure.world.resourcepack.question.line1");
                        String var14 = RealmsScreen.getLocalizedString("mco.configure.world.resourcepack.question.line2");
                        Realms.setScreen(
                            new RealmsLongConfirmationScreen(
                                new RealmsResourcePackScreen(this.lastScreen, var4, this.connectLock),
                                RealmsLongConfirmationScreen.Type.Info,
                                var13,
                                var14,
                                true,
                                100
                            )
                        );
                    } else {
                        RealmsLongRunningMcoTaskScreen var15 = new RealmsLongRunningMcoTaskScreen(
                            this.lastScreen, new RealmsTasks.RealmsConnectTask(this.lastScreen, var4)
                        );
                        var15.start();
                        Realms.setScreen(var15);
                    }
                } else {
                    this.error(RealmsScreen.getLocalizedString("mco.errorMessage.connectionFailure"));
                }
            }

        }

        private void sleep(int param0) {
            try {
                Thread.sleep((long)(param0 * 1000));
            } catch (InterruptedException var3) {
                RealmsTasks.LOGGER.warn(var3.getLocalizedMessage());
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ResettingWorldTask extends LongRunningTask {
        private final String seed;
        private final WorldTemplate worldTemplate;
        private final int levelType;
        private final boolean generateStructures;
        private final long serverId;
        private final RealmsScreen lastScreen;
        private int confirmationId = -1;
        private String title = RealmsScreen.getLocalizedString("mco.reset.world.resetting.screen.title");

        public ResettingWorldTask(long param0, RealmsScreen param1, WorldTemplate param2) {
            this.seed = null;
            this.worldTemplate = param2;
            this.levelType = -1;
            this.generateStructures = true;
            this.serverId = param0;
            this.lastScreen = param1;
        }

        public ResettingWorldTask(long param0, RealmsScreen param1, String param2, int param3, boolean param4) {
            this.seed = param2;
            this.worldTemplate = null;
            this.levelType = param3;
            this.generateStructures = param4;
            this.serverId = param0;
            this.lastScreen = param1;
        }

        public void setConfirmationId(int param0) {
            this.confirmationId = param0;
        }

        public void setResetTitle(String param0) {
            this.title = param0;
        }

        @Override
        public void run() {
            RealmsClient var0 = RealmsClient.createRealmsClient();
            this.setTitle(this.title);
            int var1 = 0;

            while(var1 < 25) {
                try {
                    if (this.aborted()) {
                        return;
                    }

                    if (this.worldTemplate != null) {
                        var0.resetWorldWithTemplate(this.serverId, this.worldTemplate.id);
                    } else {
                        var0.resetWorldWithSeed(this.serverId, this.seed, this.levelType, this.generateStructures);
                    }

                    if (this.aborted()) {
                        return;
                    }

                    if (this.confirmationId == -1) {
                        Realms.setScreen(this.lastScreen);
                    } else {
                        this.lastScreen.confirmResult(true, this.confirmationId);
                    }

                    return;
                } catch (RetryCallException var4) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.pause(var4.delaySeconds);
                    ++var1;
                } catch (Exception var5) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.LOGGER.error("Couldn't reset world");
                    this.error(var5.toString());
                    return;
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class RestoreTask extends LongRunningTask {
        private final Backup backup;
        private final long worldId;
        private final RealmsConfigureWorldScreen lastScreen;

        public RestoreTask(Backup param0, long param1, RealmsConfigureWorldScreen param2) {
            this.backup = param0;
            this.worldId = param1;
            this.lastScreen = param2;
        }

        @Override
        public void run() {
            this.setTitle(RealmsScreen.getLocalizedString("mco.backup.restoring"));
            RealmsClient var0 = RealmsClient.createRealmsClient();
            int var1 = 0;

            while(var1 < 25) {
                try {
                    if (this.aborted()) {
                        return;
                    }

                    var0.restoreWorld(this.worldId, this.backup.backupId);
                    RealmsTasks.pause(1);
                    if (this.aborted()) {
                        return;
                    }

                    Realms.setScreen(this.lastScreen.getNewScreen());
                    return;
                } catch (RetryCallException var4) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.pause(var4.delaySeconds);
                    ++var1;
                } catch (RealmsServiceException var5) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.LOGGER.error("Couldn't restore backup", (Throwable)var5);
                    Realms.setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
                    return;
                } catch (Exception var6) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.LOGGER.error("Couldn't restore backup", (Throwable)var6);
                    this.error(var6.getLocalizedMessage());
                    return;
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SwitchMinigameTask extends LongRunningTask {
        private final long worldId;
        private final WorldTemplate worldTemplate;
        private final RealmsConfigureWorldScreen lastScreen;

        public SwitchMinigameTask(long param0, WorldTemplate param1, RealmsConfigureWorldScreen param2) {
            this.worldId = param0;
            this.worldTemplate = param1;
            this.lastScreen = param2;
        }

        @Override
        public void run() {
            RealmsClient var0 = RealmsClient.createRealmsClient();
            String var1 = RealmsScreen.getLocalizedString("mco.minigame.world.starting.screen.title");
            this.setTitle(var1);

            for(int var2 = 0; var2 < 25; ++var2) {
                try {
                    if (this.aborted()) {
                        return;
                    }

                    if (var0.putIntoMinigameMode(this.worldId, this.worldTemplate.id)) {
                        Realms.setScreen(this.lastScreen);
                        break;
                    }
                } catch (RetryCallException var5) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.pause(var5.delaySeconds);
                } catch (Exception var6) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.LOGGER.error("Couldn't start mini game!");
                    this.error(var6.toString());
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SwitchSlotTask extends LongRunningTask {
        private final long worldId;
        private final int slot;
        private final RealmsConfirmResultListener listener;
        private final int confirmId;

        public SwitchSlotTask(long param0, int param1, RealmsConfirmResultListener param2, int param3) {
            this.worldId = param0;
            this.slot = param1;
            this.listener = param2;
            this.confirmId = param3;
        }

        @Override
        public void run() {
            RealmsClient var0 = RealmsClient.createRealmsClient();
            String var1 = RealmsScreen.getLocalizedString("mco.minigame.world.slot.screen.title");
            this.setTitle(var1);

            for(int var2 = 0; var2 < 25; ++var2) {
                try {
                    if (this.aborted()) {
                        return;
                    }

                    if (var0.switchSlot(this.worldId, this.slot)) {
                        this.listener.confirmResult(true, this.confirmId);
                        break;
                    }
                } catch (RetryCallException var5) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.pause(var5.delaySeconds);
                } catch (Exception var6) {
                    if (this.aborted()) {
                        return;
                    }

                    RealmsTasks.LOGGER.error("Couldn't switch world!");
                    this.error(var6.toString());
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrialCreationTask extends LongRunningTask {
        private final String name;
        private final String motd;
        private final RealmsMainScreen lastScreen;

        public TrialCreationTask(String param0, String param1, RealmsMainScreen param2) {
            this.name = param0;
            this.motd = param1;
            this.lastScreen = param2;
        }

        @Override
        public void run() {
            String var0 = RealmsScreen.getLocalizedString("mco.create.world.wait");
            this.setTitle(var0);
            RealmsClient var1 = RealmsClient.createRealmsClient();

            try {
                RealmsServer var2 = var1.createTrial(this.name, this.motd);
                if (var2 != null) {
                    this.lastScreen.setCreatedTrial(true);
                    this.lastScreen.closePopup();
                    RealmsResetWorldScreen var3 = new RealmsResetWorldScreen(
                        this.lastScreen,
                        var2,
                        this.lastScreen.newScreen(),
                        RealmsScreen.getLocalizedString("mco.selectServer.create"),
                        RealmsScreen.getLocalizedString("mco.create.world.subtitle"),
                        10526880,
                        RealmsScreen.getLocalizedString("mco.create.world.skip")
                    );
                    var3.setResetTitle(RealmsScreen.getLocalizedString("mco.create.world.reset.title"));
                    Realms.setScreen(var3);
                } else {
                    this.error(RealmsScreen.getLocalizedString("mco.trial.unavailable"));
                }
            } catch (RealmsServiceException var5) {
                RealmsTasks.LOGGER.error("Couldn't create trial");
                this.error(var5.toString());
            } catch (UnsupportedEncodingException var6) {
                RealmsTasks.LOGGER.error("Couldn't create trial");
                this.error(var6.getLocalizedMessage());
            } catch (IOException var71) {
                RealmsTasks.LOGGER.error("Could not parse response creating trial");
                this.error(var71.getLocalizedMessage());
            } catch (Exception var8) {
                RealmsTasks.LOGGER.error("Could not create trial");
                this.error(var8.getLocalizedMessage());
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WorldCreationTask extends LongRunningTask {
        private final String name;
        private final String motd;
        private final long worldId;
        private final RealmsScreen lastScreen;

        public WorldCreationTask(long param0, String param1, String param2, RealmsScreen param3) {
            this.worldId = param0;
            this.name = param1;
            this.motd = param2;
            this.lastScreen = param3;
        }

        @Override
        public void run() {
            String var0 = RealmsScreen.getLocalizedString("mco.create.world.wait");
            this.setTitle(var0);
            RealmsClient var1 = RealmsClient.createRealmsClient();

            try {
                var1.initializeWorld(this.worldId, this.name, this.motd);
                Realms.setScreen(this.lastScreen);
            } catch (RealmsServiceException var4) {
                RealmsTasks.LOGGER.error("Couldn't create world");
                this.error(var4.toString());
            } catch (UnsupportedEncodingException var5) {
                RealmsTasks.LOGGER.error("Couldn't create world");
                this.error(var5.getLocalizedMessage());
            } catch (IOException var6) {
                RealmsTasks.LOGGER.error("Could not parse response creating world");
                this.error(var6.getLocalizedMessage());
            } catch (Exception var7) {
                RealmsTasks.LOGGER.error("Could not create world");
                this.error(var7.getLocalizedMessage());
            }

        }
    }
}
