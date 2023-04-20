package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.platform.DisplayData;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.client.User;
import net.minecraft.client.resources.IndexedAssetSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class GameConfig {
    public final GameConfig.UserData user;
    public final DisplayData display;
    public final GameConfig.FolderData location;
    public final GameConfig.GameData game;
    public final GameConfig.QuickPlayData quickPlay;

    public GameConfig(GameConfig.UserData param0, DisplayData param1, GameConfig.FolderData param2, GameConfig.GameData param3, GameConfig.QuickPlayData param4) {
        this.user = param0;
        this.display = param1;
        this.location = param2;
        this.game = param3;
        this.quickPlay = param4;
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

        public Path getExternalAssetSource() {
            return this.assetIndex == null ? this.assetDirectory.toPath() : IndexedAssetSource.createIndexFs(this.assetDirectory.toPath(), this.assetIndex);
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
    public static record QuickPlayData(@Nullable String path, @Nullable String singleplayer, @Nullable String multiplayer, @Nullable String realms) {
        public boolean isEnabled() {
            return !StringUtils.isBlank(this.singleplayer) || !StringUtils.isBlank(this.multiplayer) || !StringUtils.isBlank(this.realms);
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
