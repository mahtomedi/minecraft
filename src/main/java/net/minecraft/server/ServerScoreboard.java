package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardSaveData;

public class ServerScoreboard extends Scoreboard {
    private final MinecraftServer server;
    private final Set<Objective> trackedObjectives = Sets.newHashSet();
    private final List<Runnable> dirtyListeners = Lists.newArrayList();

    public ServerScoreboard(MinecraftServer param0) {
        this.server = param0;
    }

    @Override
    public void onScoreChanged(Score param0) {
        super.onScoreChanged(param0);
        if (this.trackedObjectives.contains(param0.getObjective())) {
            this.server
                .getPlayerList()
                .broadcastAll(
                    new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, param0.getObjective().getName(), param0.getOwner(), param0.getScore())
                );
        }

        this.setDirty();
    }

    @Override
    public void onPlayerRemoved(String param0) {
        super.onPlayerRemoved(param0);
        this.server.getPlayerList().broadcastAll(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, null, param0, 0));
        this.setDirty();
    }

    @Override
    public void onPlayerScoreRemoved(String param0, Objective param1) {
        super.onPlayerScoreRemoved(param0, param1);
        if (this.trackedObjectives.contains(param1)) {
            this.server.getPlayerList().broadcastAll(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, param1.getName(), param0, 0));
        }

        this.setDirty();
    }

    @Override
    public void setDisplayObjective(int param0, @Nullable Objective param1) {
        Objective var0 = this.getDisplayObjective(param0);
        super.setDisplayObjective(param0, param1);
        if (var0 != param1 && var0 != null) {
            if (this.getObjectiveDisplaySlotCount(var0) > 0) {
                this.server.getPlayerList().broadcastAll(new ClientboundSetDisplayObjectivePacket(param0, param1));
            } else {
                this.stopTrackingObjective(var0);
            }
        }

        if (param1 != null) {
            if (this.trackedObjectives.contains(param1)) {
                this.server.getPlayerList().broadcastAll(new ClientboundSetDisplayObjectivePacket(param0, param1));
            } else {
                this.startTrackingObjective(param1);
            }
        }

        this.setDirty();
    }

    @Override
    public boolean addPlayerToTeam(String param0, PlayerTeam param1) {
        if (super.addPlayerToTeam(param0, param1)) {
            this.server
                .getPlayerList()
                .broadcastAll(ClientboundSetPlayerTeamPacket.createPlayerPacket(param1, param0, ClientboundSetPlayerTeamPacket.Action.ADD));
            this.setDirty();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void removePlayerFromTeam(String param0, PlayerTeam param1) {
        super.removePlayerFromTeam(param0, param1);
        this.server
            .getPlayerList()
            .broadcastAll(ClientboundSetPlayerTeamPacket.createPlayerPacket(param1, param0, ClientboundSetPlayerTeamPacket.Action.REMOVE));
        this.setDirty();
    }

    @Override
    public void onObjectiveAdded(Objective param0) {
        super.onObjectiveAdded(param0);
        this.setDirty();
    }

    @Override
    public void onObjectiveChanged(Objective param0) {
        super.onObjectiveChanged(param0);
        if (this.trackedObjectives.contains(param0)) {
            this.server.getPlayerList().broadcastAll(new ClientboundSetObjectivePacket(param0, 2));
        }

        this.setDirty();
    }

    @Override
    public void onObjectiveRemoved(Objective param0) {
        super.onObjectiveRemoved(param0);
        if (this.trackedObjectives.contains(param0)) {
            this.stopTrackingObjective(param0);
        }

        this.setDirty();
    }

    @Override
    public void onTeamAdded(PlayerTeam param0) {
        super.onTeamAdded(param0);
        this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(param0, true));
        this.setDirty();
    }

    @Override
    public void onTeamChanged(PlayerTeam param0) {
        super.onTeamChanged(param0);
        this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(param0, false));
        this.setDirty();
    }

    @Override
    public void onTeamRemoved(PlayerTeam param0) {
        super.onTeamRemoved(param0);
        this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createRemovePacket(param0));
        this.setDirty();
    }

    public void addDirtyListener(Runnable param0) {
        this.dirtyListeners.add(param0);
    }

    protected void setDirty() {
        for(Runnable var0 : this.dirtyListeners) {
            var0.run();
        }

    }

    public List<Packet<?>> getStartTrackingPackets(Objective param0) {
        List<Packet<?>> var0 = Lists.newArrayList();
        var0.add(new ClientboundSetObjectivePacket(param0, 0));

        for(int var1 = 0; var1 < 19; ++var1) {
            if (this.getDisplayObjective(var1) == param0) {
                var0.add(new ClientboundSetDisplayObjectivePacket(var1, param0));
            }
        }

        for(Score var2 : this.getPlayerScores(param0)) {
            var0.add(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, var2.getObjective().getName(), var2.getOwner(), var2.getScore()));
        }

        return var0;
    }

    public void startTrackingObjective(Objective param0) {
        List<Packet<?>> var0 = this.getStartTrackingPackets(param0);

        for(ServerPlayer var1 : this.server.getPlayerList().getPlayers()) {
            for(Packet<?> var2 : var0) {
                var1.connection.send(var2);
            }
        }

        this.trackedObjectives.add(param0);
    }

    public List<Packet<?>> getStopTrackingPackets(Objective param0) {
        List<Packet<?>> var0 = Lists.newArrayList();
        var0.add(new ClientboundSetObjectivePacket(param0, 1));

        for(int var1 = 0; var1 < 19; ++var1) {
            if (this.getDisplayObjective(var1) == param0) {
                var0.add(new ClientboundSetDisplayObjectivePacket(var1, param0));
            }
        }

        return var0;
    }

    public void stopTrackingObjective(Objective param0) {
        List<Packet<?>> var0 = this.getStopTrackingPackets(param0);

        for(ServerPlayer var1 : this.server.getPlayerList().getPlayers()) {
            for(Packet<?> var2 : var0) {
                var1.connection.send(var2);
            }
        }

        this.trackedObjectives.remove(param0);
    }

    public int getObjectiveDisplaySlotCount(Objective param0) {
        int var0 = 0;

        for(int var1 = 0; var1 < 19; ++var1) {
            if (this.getDisplayObjective(var1) == param0) {
                ++var0;
            }
        }

        return var0;
    }

    public ScoreboardSaveData createData() {
        ScoreboardSaveData var0 = new ScoreboardSaveData(this);
        this.addDirtyListener(var0::setDirty);
        return var0;
    }

    public ScoreboardSaveData createData(CompoundTag param0) {
        return this.createData().load(param0);
    }

    public static enum Method {
        CHANGE,
        REMOVE;
    }
}
