package net.minecraft.server.dedicated;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DedicatedPlayerList extends PlayerList {
    private static final Logger LOGGER = LogManager.getLogger();

    public DedicatedPlayerList(DedicatedServer param0, PlayerDataStorage param1) {
        super(param0, param1, param0.getProperties().maxPlayers);
        DedicatedServerProperties var0 = param0.getProperties();
        this.setViewDistance(var0.viewDistance);
        super.setUsingWhiteList(var0.whiteList.get());
        if (!param0.isSingleplayer()) {
            this.getBans().setEnabled(true);
            this.getIpBans().setEnabled(true);
        }

        this.loadUserBanList();
        this.saveUserBanList();
        this.loadIpBanList();
        this.saveIpBanList();
        this.loadOps();
        this.loadWhiteList();
        this.saveOps();
        if (!this.getWhiteList().getFile().exists()) {
            this.saveWhiteList();
        }

    }

    @Override
    public void setUsingWhiteList(boolean param0) {
        super.setUsingWhiteList(param0);
        this.getServer().storeUsingWhiteList(param0);
    }

    @Override
    public void op(GameProfile param0) {
        super.op(param0);
        this.saveOps();
    }

    @Override
    public void deop(GameProfile param0) {
        super.deop(param0);
        this.saveOps();
    }

    @Override
    public void reloadWhiteList() {
        this.loadWhiteList();
    }

    private void saveIpBanList() {
        try {
            this.getIpBans().save();
        } catch (IOException var2) {
            LOGGER.warn("Failed to save ip banlist: ", (Throwable)var2);
        }

    }

    private void saveUserBanList() {
        try {
            this.getBans().save();
        } catch (IOException var2) {
            LOGGER.warn("Failed to save user banlist: ", (Throwable)var2);
        }

    }

    private void loadIpBanList() {
        try {
            this.getIpBans().load();
        } catch (IOException var2) {
            LOGGER.warn("Failed to load ip banlist: ", (Throwable)var2);
        }

    }

    private void loadUserBanList() {
        try {
            this.getBans().load();
        } catch (IOException var2) {
            LOGGER.warn("Failed to load user banlist: ", (Throwable)var2);
        }

    }

    private void loadOps() {
        try {
            this.getOps().load();
        } catch (Exception var2) {
            LOGGER.warn("Failed to load operators list: ", (Throwable)var2);
        }

    }

    private void saveOps() {
        try {
            this.getOps().save();
        } catch (Exception var2) {
            LOGGER.warn("Failed to save operators list: ", (Throwable)var2);
        }

    }

    private void loadWhiteList() {
        try {
            this.getWhiteList().load();
        } catch (Exception var2) {
            LOGGER.warn("Failed to load white-list: ", (Throwable)var2);
        }

    }

    private void saveWhiteList() {
        try {
            this.getWhiteList().save();
        } catch (Exception var2) {
            LOGGER.warn("Failed to save white-list: ", (Throwable)var2);
        }

    }

    @Override
    public boolean isWhiteListed(GameProfile param0) {
        return !this.isUsingWhitelist() || this.isOp(param0) || this.getWhiteList().isWhiteListed(param0);
    }

    public DedicatedServer getServer() {
        return (DedicatedServer)super.getServer();
    }

    @Override
    public boolean canBypassPlayerLimit(GameProfile param0) {
        return this.getOps().canBypassPlayerLimit(param0);
    }
}
