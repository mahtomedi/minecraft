package net.minecraft.world.scores;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class PlayerTeam extends Team {
    private static final int BIT_FRIENDLY_FIRE = 0;
    private static final int BIT_SEE_INVISIBLES = 1;
    private final Scoreboard scoreboard;
    private final String name;
    private final Set<String> players = Sets.newHashSet();
    private Component displayName;
    private Component playerPrefix = CommonComponents.EMPTY;
    private Component playerSuffix = CommonComponents.EMPTY;
    private boolean allowFriendlyFire = true;
    private boolean seeFriendlyInvisibles = true;
    private Team.Visibility nameTagVisibility = Team.Visibility.ALWAYS;
    private Team.Visibility deathMessageVisibility = Team.Visibility.ALWAYS;
    private ChatFormatting color = ChatFormatting.RESET;
    private Team.CollisionRule collisionRule = Team.CollisionRule.ALWAYS;
    private final Style displayNameStyle;

    public PlayerTeam(Scoreboard param0, String param1) {
        this.scoreboard = param0;
        this.name = param1;
        this.displayName = Component.literal(param1);
        this.displayNameStyle = Style.EMPTY.withInsertion(param1).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(param1)));
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public MutableComponent getFormattedDisplayName() {
        MutableComponent var0 = ComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle(this.displayNameStyle));
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
        this.playerPrefix = param0 == null ? CommonComponents.EMPTY : param0;
        this.scoreboard.onTeamChanged(this);
    }

    public Component getPlayerPrefix() {
        return this.playerPrefix;
    }

    public void setPlayerSuffix(@Nullable Component param0) {
        this.playerSuffix = param0 == null ? CommonComponents.EMPTY : param0;
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
    public MutableComponent getFormattedName(Component param0) {
        MutableComponent var0 = Component.empty().append(this.playerPrefix).append(param0).append(this.playerSuffix);
        ChatFormatting var1 = this.getColor();
        if (var1 != ChatFormatting.RESET) {
            var0.withStyle(var1);
        }

        return var0;
    }

    public static MutableComponent formatNameForTeam(@Nullable Team param0, Component param1) {
        return param0 == null ? param1.copy() : param0.getFormattedName(param1);
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
