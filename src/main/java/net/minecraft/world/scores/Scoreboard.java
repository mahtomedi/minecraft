package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class Scoreboard {
    public static final String HIDDEN_SCORE_PREFIX = "#";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, Objective> objectivesByName = Maps.newHashMap();
    private final Map<ObjectiveCriteria, List<Objective>> objectivesByCriteria = Maps.newHashMap();
    private final Map<String, PlayerScores> playerScores = Maps.newHashMap();
    private final Map<DisplaySlot, Objective> displayObjectives = new EnumMap<>(DisplaySlot.class);
    private final Map<String, PlayerTeam> teamsByName = Maps.newHashMap();
    private final Map<String, PlayerTeam> teamsByPlayer = Maps.newHashMap();

    @Nullable
    public Objective getObjective(@Nullable String param0) {
        return this.objectivesByName.get(param0);
    }

    public Objective addObjective(
        String param0, ObjectiveCriteria param1, Component param2, ObjectiveCriteria.RenderType param3, boolean param4, @Nullable NumberFormat param5
    ) {
        if (this.objectivesByName.containsKey(param0)) {
            throw new IllegalArgumentException("An objective with the name '" + param0 + "' already exists!");
        } else {
            Objective var0 = new Objective(this, param0, param1, param2, param3, param4, param5);
            this.objectivesByCriteria.computeIfAbsent(param1, param0x -> Lists.newArrayList()).add(var0);
            this.objectivesByName.put(param0, var0);
            this.onObjectiveAdded(var0);
            return var0;
        }
    }

    public final void forAllObjectives(ObjectiveCriteria param0, ScoreHolder param1, Consumer<ScoreAccess> param2) {
        this.objectivesByCriteria
            .getOrDefault(param0, Collections.emptyList())
            .forEach(param2x -> param2.accept(this.getOrCreatePlayerScore(param1, param2x, true)));
    }

    private PlayerScores getOrCreatePlayerInfo(String param0) {
        return this.playerScores.computeIfAbsent(param0, param0x -> new PlayerScores());
    }

    public ScoreAccess getOrCreatePlayerScore(ScoreHolder param0, Objective param1) {
        return this.getOrCreatePlayerScore(param0, param1, false);
    }

    public ScoreAccess getOrCreatePlayerScore(final ScoreHolder param0, final Objective param1, boolean param2) {
        final boolean var0 = param2 || !param1.getCriteria().isReadOnly();
        PlayerScores var1 = this.getOrCreatePlayerInfo(param0.getScoreboardName());
        final MutableBoolean var2 = new MutableBoolean();
        final Score var3 = var1.getOrCreate(param1, param1x -> var2.setTrue());
        return new ScoreAccess() {
            @Override
            public int get() {
                return var3.value();
            }

            @Override
            public void set(int param0x) {
                if (!var0) {
                    throw new IllegalStateException("Cannot modify read-only score");
                } else {
                    boolean var0 = var2.isTrue();
                    if (param1.displayAutoUpdate()) {
                        Component var1 = param0.getDisplayName();
                        if (var1 != null && !var1.equals(var3.display())) {
                            var3.display(var1);
                            var0 = true;
                        }
                    }

                    if (param0 != var3.value()) {
                        var3.value(param0);
                        var0 = true;
                    }

                    if (var0) {
                        this.sendScoreToPlayers();
                    }

                }
            }

            @Nullable
            @Override
            public Component display() {
                return var3.display();
            }

            @Override
            public void display(@Nullable Component param0x) {
                if (var2.isTrue() || !Objects.equals(param0, var3.display())) {
                    var3.display(param0);
                    this.sendScoreToPlayers();
                }

            }

            @Override
            public void numberFormatOverride(@Nullable NumberFormat param0x) {
                var3.numberFormat(param0);
                this.sendScoreToPlayers();
            }

            @Override
            public boolean locked() {
                return var3.isLocked();
            }

            @Override
            public void unlock() {
                this.setLocked(false);
            }

            @Override
            public void lock() {
                this.setLocked(true);
            }

            private void setLocked(boolean param0x) {
                var3.setLocked(param0);
                if (var2.isTrue()) {
                    this.sendScoreToPlayers();
                }

                Scoreboard.this.onScoreLockChanged(param0, param1);
            }

            private void sendScoreToPlayers() {
                Scoreboard.this.onScoreChanged(param0, param1, var3);
                var2.setFalse();
            }
        };
    }

    @Nullable
    public ReadOnlyScoreInfo getPlayerScoreInfo(ScoreHolder param0, Objective param1) {
        PlayerScores var0 = this.playerScores.get(param0.getScoreboardName());
        return var0 != null ? var0.get(param1) : null;
    }

    public Collection<PlayerScoreEntry> listPlayerScores(Objective param0) {
        List<PlayerScoreEntry> var0 = new ArrayList<>();
        this.playerScores.forEach((param2, param3) -> {
            Score var0x = param3.get(param0);
            if (var0x != null) {
                var0.add(new PlayerScoreEntry(param2, var0x.value(), var0x.display(), var0x.numberFormat()));
            }

        });
        return var0;
    }

    public Collection<Objective> getObjectives() {
        return this.objectivesByName.values();
    }

    public Collection<String> getObjectiveNames() {
        return this.objectivesByName.keySet();
    }

    public Collection<ScoreHolder> getTrackedPlayers() {
        return this.playerScores.keySet().stream().map(ScoreHolder::forNameOnly).toList();
    }

    public void resetAllPlayerScores(ScoreHolder param0) {
        PlayerScores var0 = this.playerScores.remove(param0.getScoreboardName());
        if (var0 != null) {
            this.onPlayerRemoved(param0);
        }

    }

    public void resetSinglePlayerScore(ScoreHolder param0, Objective param1) {
        PlayerScores var0 = this.playerScores.get(param0.getScoreboardName());
        if (var0 != null) {
            boolean var1 = var0.remove(param1);
            if (!var0.hasScores()) {
                PlayerScores var2 = this.playerScores.remove(param0.getScoreboardName());
                if (var2 != null) {
                    this.onPlayerRemoved(param0);
                }
            } else if (var1) {
                this.onPlayerScoreRemoved(param0, param1);
            }
        }

    }

    public Object2IntMap<Objective> listPlayerScores(ScoreHolder param0) {
        PlayerScores var0 = this.playerScores.get(param0.getScoreboardName());
        return var0 != null ? var0.listScores() : Object2IntMaps.emptyMap();
    }

    public void removeObjective(Objective param0) {
        this.objectivesByName.remove(param0.getName());

        for(DisplaySlot var0 : DisplaySlot.values()) {
            if (this.getDisplayObjective(var0) == param0) {
                this.setDisplayObjective(var0, null);
            }
        }

        List<Objective> var1 = this.objectivesByCriteria.get(param0.getCriteria());
        if (var1 != null) {
            var1.remove(param0);
        }

        for(PlayerScores var2 : this.playerScores.values()) {
            var2.remove(param0);
        }

        this.onObjectiveRemoved(param0);
    }

    public void setDisplayObjective(DisplaySlot param0, @Nullable Objective param1) {
        this.displayObjectives.put(param0, param1);
    }

    @Nullable
    public Objective getDisplayObjective(DisplaySlot param0) {
        return this.displayObjectives.get(param0);
    }

    @Nullable
    public PlayerTeam getPlayerTeam(String param0) {
        return this.teamsByName.get(param0);
    }

    public PlayerTeam addPlayerTeam(String param0) {
        PlayerTeam var0 = this.getPlayerTeam(param0);
        if (var0 != null) {
            LOGGER.warn("Requested creation of existing team '{}'", param0);
            return var0;
        } else {
            var0 = new PlayerTeam(this, param0);
            this.teamsByName.put(param0, var0);
            this.onTeamAdded(var0);
            return var0;
        }
    }

    public void removePlayerTeam(PlayerTeam param0) {
        this.teamsByName.remove(param0.getName());

        for(String var0 : param0.getPlayers()) {
            this.teamsByPlayer.remove(var0);
        }

        this.onTeamRemoved(param0);
    }

    public boolean addPlayerToTeam(String param0, PlayerTeam param1) {
        if (this.getPlayersTeam(param0) != null) {
            this.removePlayerFromTeam(param0);
        }

        this.teamsByPlayer.put(param0, param1);
        return param1.getPlayers().add(param0);
    }

    public boolean removePlayerFromTeam(String param0) {
        PlayerTeam var0 = this.getPlayersTeam(param0);
        if (var0 != null) {
            this.removePlayerFromTeam(param0, var0);
            return true;
        } else {
            return false;
        }
    }

    public void removePlayerFromTeam(String param0, PlayerTeam param1) {
        if (this.getPlayersTeam(param0) != param1) {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + param1.getName() + "'.");
        } else {
            this.teamsByPlayer.remove(param0);
            param1.getPlayers().remove(param0);
        }
    }

    public Collection<String> getTeamNames() {
        return this.teamsByName.keySet();
    }

    public Collection<PlayerTeam> getPlayerTeams() {
        return this.teamsByName.values();
    }

    @Nullable
    public PlayerTeam getPlayersTeam(String param0) {
        return this.teamsByPlayer.get(param0);
    }

    public void onObjectiveAdded(Objective param0) {
    }

    public void onObjectiveChanged(Objective param0) {
    }

    public void onObjectiveRemoved(Objective param0) {
    }

    protected void onScoreChanged(ScoreHolder param0, Objective param1, Score param2) {
    }

    protected void onScoreLockChanged(ScoreHolder param0, Objective param1) {
    }

    public void onPlayerRemoved(ScoreHolder param0) {
    }

    public void onPlayerScoreRemoved(ScoreHolder param0, Objective param1) {
    }

    public void onTeamAdded(PlayerTeam param0) {
    }

    public void onTeamChanged(PlayerTeam param0) {
    }

    public void onTeamRemoved(PlayerTeam param0) {
    }

    public void entityRemoved(Entity param0) {
        if (!(param0 instanceof Player) && !param0.isAlive()) {
            this.resetAllPlayerScores(param0);
            this.removePlayerFromTeam(param0.getScoreboardName());
        }
    }

    protected ListTag savePlayerScores() {
        ListTag var0 = new ListTag();
        this.playerScores.forEach((param1, param2) -> param2.listRawScores().forEach((param2x, param3) -> {
                CompoundTag var0x = param3.write();
                var0x.putString("Name", param1);
                var0x.putString("Objective", param2x.getName());
                var0.add(var0x);
            }));
        return var0;
    }

    protected void loadPlayerScores(ListTag param0) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            CompoundTag var1 = param0.getCompound(var0);
            Score var2 = Score.read(var1);
            String var3 = var1.getString("Name");
            String var4 = var1.getString("Objective");
            Objective var5 = this.getObjective(var4);
            if (var5 == null) {
                LOGGER.error("Unknown objective {} for name {}, ignoring", var4, var3);
            } else {
                this.getOrCreatePlayerInfo(var3).setScore(var5, var2);
            }
        }

    }
}
