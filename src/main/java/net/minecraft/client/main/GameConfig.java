package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.platform.DisplayData;
import java.io.File;
import java.net.Proxy;
import javax.annotation.Nullable;
import net.minecraft.client.User;
import net.minecraft.client.resources.AssetIndex;
import net.minecraft.client.resources.DirectAssetIndex;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameConfig {
    public final GameConfig.UserData user;
    public final DisplayData display;
    public final GameConfig.FolderData location;
    public final GameConfig.GameData game;
    public final GameConfig.ServerData server;

    public GameConfig(GameConfig.UserData param0, DisplayData param1, GameConfig.FolderData param2, GameConfig.GameData param3, GameConfig.ServerData param4) {
        this.user = param0;
        this.display = param1;
        this.location = param2;
        this.game = param3;
        this.server = param4;
    }

    @OnlyIn(Dist.CLIENT)
    public static class FolderData {
        public final File gameDirectory;
        public final File resourcePackDirectory;
        public final File assetDirectory;
        @Nullable
        public final String assetIndex;

        public FolderData(File param0, File param1, File param2, @Nullable String param3) {
            this.gameDirectory = param0;
            this.resourcePackDirectory = param1;
            this.assetDirectory = param2;
            this.assetIndex = param3;
        }

        public AssetIndex getAssetIndex() {
            return (AssetIndex)(this.assetIndex == null ? new DirectAssetIndex(this.assetDirectory) : new AssetIndex(this.assetDirectory, this.assetIndex));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class GameData {
        public final boolean demo;
        public final String launchVersion;
        public final String versionType;
        public final boolean disableMultiplayer;
        public final boolean disableChat;

        public GameData(boolean param0, String param1, String param2, boolean param3, boolean param4) {
            this.demo = param0;
            this.launchVersion = param1;
            this.versionType = param2;
            this.disableMultiplayer = param3;
            this.disableChat = param4;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ServerData {
        @Nullable
        public final String hostname;
        public final int port;

        public ServerData(@Nullable String param0, int param1) {
            this.hostname = param0;
            this.port = param1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class UserData {
        public final User user;
        public final PropertyMap userProperties;
        public final PropertyMap profileProperties;
        public final Proxy proxy;

        public UserData(User param0, PropertyMap param1, PropertyMap param2, Proxy param3) {
            this.user = param0;
            this.userProperties = param1;
            this.profileProperties = param2;
            this.proxy = param3;
        }
    }
}
