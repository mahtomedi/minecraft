package net.minecraft.client;

import com.mojang.bridge.game.GameSession;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Session implements GameSession {
    private final int players;
    private final boolean isRemoteServer;
    private final String difficulty;
    private final String gameMode;
    private final UUID id;

    public Session(MultiPlayerLevel param0, LocalPlayer param1, ClientPacketListener param2) {
        this.players = param2.getOnlinePlayers().size();
        this.isRemoteServer = !param2.getConnection().isMemoryConnection();
        this.difficulty = param0.getDifficulty().getKey();
        PlayerInfo var0 = param2.getPlayerInfo(param1.getUUID());
        if (var0 != null) {
            this.gameMode = var0.getGameMode().getName();
        } else {
            this.gameMode = "unknown";
        }

        this.id = param2.getId();
    }

    @Override
    public int getPlayerCount() {
        return this.players;
    }

    @Override
    public boolean isRemoteServer() {
        return this.isRemoteServer;
    }

    @Override
    public String getDifficulty() {
        return this.difficulty;
    }

    @Override
    public String getGameMode() {
        return this.gameMode;
    }

    @Override
    public UUID getSessionId() {
        return this.id;
    }
}
