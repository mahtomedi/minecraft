package net.minecraft.world.scores;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerTeam extends Team {
    private final Scoreboard scoreboard;
    private final String name;
    private final Set<String> players = Sets.newHashSet();
    private Component displayName;
    private Component playerPrefix = new TextComponent("");
    private Component playerSuffix = new TextComponent("");
    private boolean allowFriendlyFire = true;
    private boolean seeFriendlyInvisibles = true;
    private Team.Visibility nameTagVisibility = Team.Visibility.ALWAYS;
    private Team.Visibility deathMessageVisibility = Team.Visibility.ALWAYS;
    private ChatFormatting color = ChatFormatting.RESET;
    private Team.CollisionRule collisionRule = Team.CollisionRule.ALWAYS;

    public PlayerTeam(Scoreboard param0, String param1) {
        this.scoreboard = param0;
        this.name = param1;
        this.displayName = new TextComponent(param1);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public Component getFormattedDisplayName() {
        Component var0 = ComponentUtils.wrapInSquareBrackets(
            this.displayName
                .deepCopy()
                .withStyle(param0 -> param0.setInsertion(this.name).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.name))))
        );
        ChatFormatting var1 = this.getColor();
        if (var1 != ChatFormatting.RESET) {
            var0.withStyle(var1);
        }

        return var0;
    }

    public void setDisplayName(Component param0) {
        if (param0 == null) {
            throw new IllegalArgumentException("Name cannot be null");
        } else {
            this.displayName = param0;
            this.scoreboard.onTeamChanged(this);
        }
    }

    public void setPlayerPrefix(@Nullable Component param0) {
        this.playerPrefix = (Component)(param0 == null ? new TextComponent("") : param0.deepCopy());
        this.scoreboard.onTeamChanged(this);
    }

    public Component getPlayerPrefix() {
        return this.playerPrefix;
    }

    public void setPlayerSuffix(@Nullable Component param0) {
        this.playerSuffix = (Component)(param0 == null ? new TextComponent("") : param0.deepCopy());
        this.scoreboard.onTeamChanged(this);
    }

    public Component getPlayerSuffix() {
        return this.playerSuffix;
    }

    @Override
    public Collection<String> getPlayers() {
        return this.players;
    }

    @Override
    public Component getFormattedName(Component param0) {
        Component var0 = new TextComponent("").append(this.playerPrefix).append(param0).append(this.playerSuffix);
        ChatFormatting var1 = this.getColor();
        if (var1 != ChatFormatting.RESET) {
            var0.withStyle(var1);
        }

        return var0;
    }

    public static Component formatNameForTeam(@Nullable Team param0, Component param1) {
        return param0 == null ? param1.deepCopy() : param0.getFormattedName(param1);
    }

    @Override
    public boolean isAllowFriendlyFire() {
        return this.allowFriendlyFire;
    }

    public void setAllowFriendlyFire(boolean param0) {
        this.allowFriendlyFire = param0;
        this.scoreboard.onTeamChanged(this);
    }

    @Override
    public boolean canSeeFriendlyInvisibles() {
        return this.seeFriendlyInvisibles;
    }

    public void setSeeFriendlyInvisibles(boolean param0) {
        this.seeFriendlyInvisibles = param0;
        this.scoreboard.onTeamChanged(this);
    }

    @Override
    public Team.Visibility getNameTagVisibility() {
        return this.nameTagVisibility;
    }

    @Override
    public Team.Visibility getDeathMessageVisibility() {
        return this.deathMessageVisibility;
    }

    public void setNameTagVisibility(Team.Visibility param0) {
        this.nameTagVisibility = param0;
        this.scoreboard.onTeamChanged(this);
    }

    public void setDeathMessageVisibility(Team.Visibility param0) {
        this.deathMessageVisibility = param0;
        this.scoreboard.onTeamChanged(this);
    }

    @Override
    public Team.CollisionRule getCollisionRule() {
        return this.collisionRule;
    }

    public void setCollisionRule(Team.CollisionRule param0) {
        this.collisionRule = param0;
        this.scoreboard.onTeamChanged(this);
    }

    public int packOptions() {
        int var0 = 0;
        if (this.isAllowFriendlyFire()) {
            var0 |= 1;
        }

        if (this.canSeeFriendlyInvisibles()) {
            var0 |= 2;
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public void unpackOptions(int param0) {
        this.setAllowFriendlyFire((param0 & 1) > 0);
        this.setSeeFriendlyInvisibles((param0 & 2) > 0);
    }

    public void setColor(ChatFormatting param0) {
        this.color = param0;
        this.scoreboard.onTeamChanged(this);
    }

    @Override
    public ChatFormatting getColor() {
        return this.color;
    }
}
