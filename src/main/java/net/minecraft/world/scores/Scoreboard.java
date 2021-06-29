package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Scoreboard {
    public static final int DISPLAY_SLOT_LIST = 0;
    public static final int DISPLAY_SLOT_SIDEBAR = 1;
    public static final int DISPLAY_SLOT_BELOW_NAME = 2;
    public static final int DISPLAY_SLOT_TEAMS_SIDEBAR_START = 3;
    public static final int DISPLAY_SLOT_TEAMS_SIDEBAR_END = 18;
    public static final int DISPLAY_SLOTS = 19;
    public static final int MAX_NAME_LENGTH = 40;
    private final Map<String, Objective> objectivesByName = Maps.newHashMap();
    private final Map<ObjectiveCriteria, List<Objective>> objectivesByCriteria = Maps.newHashMap();
    private final Map<String, Map<Objective, Score>> playerScores = Maps.newHashMap();
    private final Objective[] displayObjectives = new Objective[19];
    private final Map<String, PlayerTeam> teamsByName = Maps.newHashMap();
    private final Map<String, PlayerTeam> teamsByPlayer = Maps.newHashMap();
    private static String[] displaySlotNames;

    public boolean hasObjective(String param0) {
        return this.objectivesByName.containsKey(param0);
    }

    public Objective getOrCreateObjective(String param0) {
        return this.objectivesByName.get(param0);
    }

    @Nullable
    public Objective getObjective(@Nullable String param0) {
        return this.objectivesByName.get(param0);
    }

    public Objective addObjective(String param0, ObjectiveCriteria param1, Component param2, ObjectiveCriteria.RenderType param3) {
        if (param0.length() > 16) {
            throw new IllegalArgumentException("The objective name '" + param0 + "' is too long!");
        } else if (this.objectivesByName.containsKey(param0)) {
            throw new IllegalArgumentException("An objective with the name '" + param0 + "' already exists!");
        } else {
            Objective var0 = new Objective(this, param0, param1, param2, param3);
            this.objectivesByCriteria.computeIfAbsent(param1, param0x -> Lists.newArrayList()).add(var0);
            this.objectivesByName.put(param0, var0);
            this.onObjectiveAdded(var0);
            return var0;
        }
    }

    public final void forAllObjectives(ObjectiveCriteria param0, String param1, Consumer<Score> param2) {
        this.objectivesByCriteria.getOrDefault(param0, Collections.emptyList()).forEach(param2x -> param2.accept(this.getOrCreatePlayerScore(param1, param2x)));
    }

    public boolean hasPlayerScore(String param0, Objective param1) {
        Map<Objective, Score> var0 = this.playerScores.get(param0);
        if (var0 == null) {
            return false;
        } else {
            Score var1 = var0.get(param1);
            return var1 != null;
        }
    }

    public Score getOrCreatePlayerScore(String param0, Objective param1) {
        if (param0.length() > 40) {
            throw new IllegalArgumentException("The player name '" + param0 + "' is too long!");
        } else {
            Map<Objective, Score> var0 = this.playerScores.computeIfAbsent(param0, param0x -> Maps.newHashMap());
            return var0.computeIfAbsent(param1, param1x -> {
                Score var0x = new Score(this, param1x, param0);
                var0x.setScore(0);
                return var0x;
            });
        }
    }

    public Collection<Score> getPlayerScores(Objective param0) {
        List<Score> var0 = Lists.newArrayList();

        for(Map<Objective, Score> var1 : this.playerScores.values()) {
            Score var2 = var1.get(param0);
            if (var2 != null) {
                var0.add(var2);
            }
        }

        var0.sort(Score.SCORE_COMPARATOR);
        return var0;
    }

    public Collection<Objective> getObjectives() {
        return this.objectivesByName.values();
    }

    public Collection<String> getObjectiveNames() {
        return this.objectivesByName.keySet();
    }

    public Collection<String> getTrackedPlayers() {
        return Lists.newArrayList(this.playerScores.keySet());
    }

    public void resetPlayerScore(String param0, @Nullable Objective param1) {
        if (param1 == null) {
            Map<Objective, Score> var0 = this.playerScores.remove(param0);
            if (var0 != null) {
                this.onPlayerRemoved(param0);
            }
        } else {
            Map<Objective, Score> var1 = this.playerScores.get(param0);
            if (var1 != null) {
                Score var2 = var1.remove(param1);
                if (var1.size() < 1) {
                    Map<Objective, Score> var3 = this.playerScores.remove(param0);
                    if (var3 != null) {
                        this.onPlayerRemoved(param0);
                    }
                } else if (var2 != null) {
                    this.onPlayerScoreRemoved(param0, param1);
                }
            }
        }

    }

    public Map<Objective, Score> getPlayerScores(String param0) {
        Map<Objective, Score> var0 = this.playerScores.get(param0);
        if (var0 == null) {
            var0 = Maps.newHashMap();
        }

        return var0;
    }

    public void removeObjective(Objective param0) {
        this.objectivesByName.remove(param0.getName());

        for(int var0 = 0; var0 < 19; ++var0) {
            if (this.getDisplayObjective(var0) == param0) {
                this.setDisplayObjective(var0, null);
            }
        }

        List<Objective> var1 = this.objectivesByCriteria.get(param0.getCriteria());
        if (var1 != null) {
            var1.remove(param0);
        }

        for(Map<Objective, Score> var2 : this.playerScores.values()) {
            var2.remove(param0);
        }

        this.onObjectiveRemoved(param0);
    }

    public void setDisplayObjective(int param0, @Nullable Objective param1) {
        this.displayObjectives[param0] = param1;
    }

    @Nullable
    public Objective getDisplayObjective(int param0) {
        return this.displayObjectives[param0];
    }

    @Nullable
    public PlayerTeam getPlayerTeam(String param0) {
        return this.teamsByName.get(param0);
    }

    public PlayerTeam addPlayerTeam(String param0) {
        if (param0.length() > 16) {
            throw new IllegalArgumentException("The team name '" + param0 + "' is too long!");
        } else {
            PlayerTeam var0 = this.getPlayerTeam(param0);
            if (var0 != null) {
                throw new IllegalArgumentException("A team with the name '" + param0 + "' already exists!");
            } else {
                var0 = new PlayerTeam(this, param0);
                this.teamsByName.put(param0, var0);
                this.onTeamAdded(var0);
                return var0;
            }
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
        if (param0.length() > 40) {
            throw new IllegalArgumentException("The player name '" + param0 + "' is too long!");
        } else {
            if (this.getPlayersTeam(param0) != null) {
                this.removePlayerFromTeam(param0);
            }

            this.teamsByPlayer.put(param0, param1);
            return param1.getPlayers().add(param0);
        }
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

    public void onScoreChanged(Score param0) {
    }

    public void onPlayerRemoved(String param0) {
    }

    public void onPlayerScoreRemoved(String param0, Objective param1) {
    }

    public void onTeamAdded(PlayerTeam param0) {
    }

    public void onTeamChanged(PlayerTeam param0) {
    }

    public void onTeamRemoved(PlayerTeam param0) {
    }

    public static String getDisplaySlotName(int param0) {
        switch(param0) {
            case 0:
                return "list";
            case 1:
                return "sidebar";
            case 2:
                return "belowName";
            default:
                if (param0 >= 3 && param0 <= 18) {
                    ChatFormatting var0 = ChatFormatting.getById(param0 - 3);
                    if (var0 != null && var0 != ChatFormatting.RESET) {
                        return "sidebar.team." + var0.getName();
                    }
                }

                return null;
        }
    }

    public static int getDisplaySlotByName(String param0) {
        if ("list".equalsIgnoreCase(param0)) {
            return 0;
        } else if ("sidebar".equalsIgnoreCase(param0)) {
            return 1;
        } else if ("belowName".equalsIgnoreCase(param0)) {
            return 2;
        } else {
            if (param0.startsWith("sidebar.team.")) {
                String var0 = param0.substring("sidebar.team.".length());
                ChatFormatting var1 = ChatFormatting.getByName(var0);
                if (var1 != null && var1.getId() >= 0) {
                    return var1.getId() + 3;
                }
            }

            return -1;
        }
    }

    public static String[] getDisplaySlotNames() {
        if (displaySlotNames == null) {
            displaySlotNames = new String[19];

            for(int var0 = 0; var0 < 19; ++var0) {
                displaySlotNames[var0] = getDisplaySlotName(var0);
            }
        }

        return displaySlotNames;
    }

    public void entityRemoved(Entity param0) {
        if (param0 != null && !(param0 instanceof Player) && !param0.isAlive()) {
            String var0 = param0.getStringUUID();
            this.resetPlayerScore(var0, null);
            this.removePlayerFromTeam(var0);
        }
    }

    protected ListTag savePlayerScores() {
        ListTag var0 = new ListTag();
        this.playerScores
            .values()
            .stream()
            .map(Map::values)
            .forEach(param1 -> param1.stream().filter(param0x -> param0x.getObjective() != null).forEach(param1x -> {
                    CompoundTag var0x = new CompoundTag();
                    var0x.putString("Name", param1x.getOwner());
                    var0x.putString("Objective", param1x.getObjective().getName());
                    var0x.putInt("Score", param1x.getScore());
                    var0x.putBoolean("Locked", param1x.isLocked());
                    var0.add(var0x);
                }));
        return var0;
    }

    protected void loadPlayerScores(ListTag param0) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            CompoundTag var1 = param0.getCompound(var0);
            Objective var2 = this.getOrCreateObjective(var1.getString("Objective"));
            String var3 = var1.getString("Name");
            if (var3.length() > 40) {
                var3 = var3.substring(0, 40);
            }

            Score var4 = this.getOrCreatePlayerScore(var3, var2);
            var4.setScore(var1.getInt("Score"));
            if (var1.contains("Locked")) {
                var4.setLocked(var1.getBoolean("Locked"));
            }
        }

    }
}
