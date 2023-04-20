package net.minecraft.client.quickplay;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class QuickPlay {
    public static final Component ERROR_TITLE = Component.translatable("quickplay.error.title");
    private static final Component INVALID_IDENTIFIER = Component.translatable("quickplay.error.invalid_identifier");
    private static final Component REALM_CONNECT = Component.translatable("quickplay.error.realm_connect");
    private static final Component REALM_PERMISSION = Component.translatable("quickplay.error.realm_permission");
    private static final Component TO_TITLE = Component.translatable("gui.toTitle");
    private static final Component TO_WORLD_LIST = Component.translatable("gui.toWorld");
    private static final Component TO_REALMS_LIST = Component.translatable("gui.toRealms");

    public static void connect(Minecraft param0, GameConfig.QuickPlayData param1, ReloadInstance param2, RealmsClient param3) {
        String var0 = param1.singleplayer();
        String var1 = param1.multiplayer();
        String var2 = param1.realms();
        param2.done().thenRunAsync(() -> {
            if (!StringUtils.isBlank(var0)) {
                joinSingleplayerWorld(param0, var0);
            } else if (!StringUtils.isBlank(var1)) {
                joinMultiplayerWorld(param0, var1);
            } else if (!StringUtils.isBlank(var2)) {
                joinRealmsWorld(param0, param3, var2);
            }

        }, param0);
    }

    private static void joinSingleplayerWorld(Minecraft param0, String param1) {
        if (!param0.getLevelSource().levelExists(param1)) {
            Screen var0 = new SelectWorldScreen(new TitleScreen());
            param0.setScreen(new DisconnectedScreen(var0, ERROR_TITLE, INVALID_IDENTIFIER, TO_WORLD_LIST));
        } else {
            param0.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
            param0.createWorldOpenFlows().loadLevel(new TitleScreen(), param1);
        }
    }

    private static void joinMultiplayerWorld(Minecraft param0, String param1) {
        ServerList var0 = new ServerList(param0);
        var0.load();
        ServerData var1 = var0.get(param1);
        if (var1 == null) {
            var1 = new ServerData(I18n.get("selectServer.defaultName"), param1, false);
            var0.add(var1, true);
            var0.save();
        }

        ServerAddress var2 = ServerAddress.parseString(param1);
        ConnectScreen.startConnecting(new JoinMultiplayerScreen(new TitleScreen()), param0, var2, var1, true);
    }

    private static void joinRealmsWorld(Minecraft param0, RealmsClient param1, String param2) {
        long var0;
        RealmsServerList var1;
        try {
            var0 = Long.parseLong(param2);
            var1 = param1.listWorlds();
        } catch (NumberFormatException var9) {
            Screen var3 = new RealmsMainScreen(new TitleScreen());
            param0.setScreen(new DisconnectedScreen(var3, ERROR_TITLE, INVALID_IDENTIFIER, TO_REALMS_LIST));
            return;
        } catch (RealmsServiceException var101) {
            Screen var5 = new TitleScreen();
            param0.setScreen(new DisconnectedScreen(var5, ERROR_TITLE, REALM_CONNECT, TO_TITLE));
            return;
        }

        RealmsServer var8 = var1.servers.stream().filter(param1x -> param1x.id == var0).findFirst().orElse(null);
        if (var8 == null) {
            Screen var9 = new RealmsMainScreen(new TitleScreen());
            param0.setScreen(new DisconnectedScreen(var9, ERROR_TITLE, REALM_PERMISSION, TO_REALMS_LIST));
        } else {
            TitleScreen var10 = new TitleScreen();
            GetServerDetailsTask var11 = new GetServerDetailsTask(new RealmsMainScreen(var10), var10, var8, new ReentrantLock());
            param0.setScreen(new RealmsLongRunningMcoTaskScreen(var10, var11));
        }
    }
}
