package net.minecraft.world.scores;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ScoreboardSaveData extends SavedData {
    public static final String FILE_ID = "scoreboard";
    private final Scoreboard scoreboard;

    public ScoreboardSaveData(Scoreboard param0) {
        this.scoreboard = param0;
    }

    public ScoreboardSaveData load(CompoundTag param0) {
        this.loadObjectives(param0.getList("Objectives", 10));
        this.scoreboard.loadPlayerScores(param0.getList("PlayerScores", 10));
        if (param0.contains("DisplaySlots", 10)) {
            this.loadDisplaySlots(param0.getCompound("DisplaySlots"));
        }

        if (param0.contains("Teams", 9)) {
            this.loadTeams(param0.getList("Teams", 10));
        }

        return this;
    }

    private void loadTeams(ListTag param0) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            CompoundTag var1 = param0.getCompound(var0);
            String var2 = var1.getString("Name");
            if (var2.length() > 16) {
                var2 = var2.substring(0, 16);
            }

            PlayerTeam var3 = this.scoreboard.addPlayerTeam(var2);
            Component var4 = Component.Serializer.fromJson(var1.getString("DisplayName"));
            if (var4 != null) {
                var3.setDisplayName(var4);
            }

            if (var1.contains("TeamColor", 8)) {
                var3.setColor(ChatFormatting.getByName(var1.getString("TeamColor")));
            }

            if (var1.contains("AllowFriendlyFire", 99)) {
                var3.setAllowFriendlyFire(var1.getBoolean("AllowFriendlyFire"));
            }

            if (var1.contains("SeeFriendlyInvisibles", 99)) {
                var3.setSeeFriendlyInvisibles(var1.getBoolean("SeeFriendlyInvisibles"));
            }

            if (var1.contains("MemberNamePrefix", 8)) {
                Component var5 = Component.Serializer.fromJson(var1.getString("MemberNamePrefix"));
                if (var5 != null) {
                    var3.setPlayerPrefix(var5);
                }
            }

            if (var1.contains("MemberNameSuffix", 8)) {
                Component var6 = Component.Serializer.fromJson(var1.getString("MemberNameSuffix"));
                if (var6 != null) {
                    var3.setPlayerSuffix(var6);
                }
            }

            if (var1.contains("NameTagVisibility", 8)) {
                Team.Visibility var7 = Team.Visibility.byName(var1.getString("NameTagVisibility"));
                if (var7 != null) {
                    var3.setNameTagVisibility(var7);
                }
            }

            if (var1.contains("DeathMessageVisibility", 8)) {
                Team.Visibility var8 = Team.Visibility.byName(var1.getString("DeathMessageVisibility"));
                if (var8 != null) {
                    var3.setDeathMessageVisibility(var8);
                }
            }

            if (var1.contains("CollisionRule", 8)) {
                Team.CollisionRule var9 = Team.CollisionRule.byName(var1.getString("CollisionRule"));
                if (var9 != null) {
                    var3.setCollisionRule(var9);
                }
            }

            this.loadTeamPlayers(var3, var1.getList("Players", 8));
        }

    }

    private void loadTeamPlayers(PlayerTeam param0, ListTag param1) {
        for(int var0 = 0; var0 < param1.size(); ++var0) {
            this.scoreboard.addPlayerToTeam(param1.getString(var0), param0);
        }

    }

    private void loadDisplaySlots(CompoundTag param0) {
        for(int var0 = 0; var0 < 19; ++var0) {
            if (param0.contains("slot_" + var0, 8)) {
                String var1 = param0.getString("slot_" + var0);
                Objective var2 = this.scoreboard.getObjective(var1);
                this.scoreboard.setDisplayObjective(var0, var2);
            }
        }

    }

    private void loadObjectives(ListTag param0) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            CompoundTag var1 = param0.getCompound(var0);
            ObjectiveCriteria.byName(var1.getString("CriteriaName")).ifPresent(param1 -> {
                String var0x = var1.getString("Name");
                if (var0x.length() > 16) {
                    var0x = var0x.substring(0, 16);
                }

                Component var1x = Component.Serializer.fromJson(var1.getString("DisplayName"));
                ObjectiveCriteria.RenderType var2x = ObjectiveCriteria.RenderType.byId(var1.getString("RenderType"));
                this.scoreboard.addObjective(var0x, param1, var1x, var2x);
            });
        }

    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        param0.put("Objectives", this.saveObjectives());
        param0.put("PlayerScores", this.scoreboard.savePlayerScores());
        param0.put("Teams", this.saveTeams());
        this.saveDisplaySlots(param0);
        return param0;
    }

    private ListTag saveTeams() {
        ListTag var0 = new ListTag();

        for(PlayerTeam var2 : this.scoreboard.getPlayerTeams()) {
            CompoundTag var3 = new CompoundTag();
            var3.putString("Name", var2.getName());
            var3.putString("DisplayName", Component.Serializer.toJson(var2.getDisplayName()));
            if (var2.getColor().getId() >= 0) {
                var3.putString("TeamColor", var2.getColor().getName());
            }

            var3.putBoolean("AllowFriendlyFire", var2.isAllowFriendlyFire());
            var3.putBoolean("SeeFriendlyInvisibles", var2.canSeeFriendlyInvisibles());
            var3.putString("MemberNamePrefix", Component.Serializer.toJson(var2.getPlayerPrefix()));
            var3.putString("MemberNameSuffix", Component.Serializer.toJson(var2.getPlayerSuffix()));
            var3.putString("NameTagVisibility", var2.getNameTagVisibility().name);
            var3.putString("DeathMessageVisibility", var2.getDeathMessageVisibility().name);
            var3.putString("CollisionRule", var2.getCollisionRule().name);
            ListTag var4 = new ListTag();

            for(String var5 : var2.getPlayers()) {
                var4.add(StringTag.valueOf(var5));
            }

            var3.put("Players", var4);
            var0.add(var3);
        }

        return var0;
    }

    private void saveDisplaySlots(CompoundTag param0) {
        CompoundTag var0 = new CompoundTag();
        boolean var1 = false;

        for(int var2 = 0; var2 < 19; ++var2) {
            Objective var3 = this.scoreboard.getDisplayObjective(var2);
            if (var3 != null) {
                var0.putString("slot_" + var2, var3.getName());
                var1 = true;
            }
        }

        if (var1) {
            param0.put("DisplaySlots", var0);
        }

    }

    private ListTag saveObjectives() {
        ListTag var0 = new ListTag();

        for(Objective var2 : this.scoreboard.getObjectives()) {
            if (var2.getCriteria() != null) {
                CompoundTag var3 = new CompoundTag();
                var3.putString("Name", var2.getName());
                var3.putString("CriteriaName", var2.getCriteria().getName());
                var3.putString("DisplayName", Component.Serializer.toJson(var2.getDisplayName()));
                var3.putString("RenderType", var2.getRenderType().getId());
                var0.add(var3);
            }
        }

        return var0;
    }
}
