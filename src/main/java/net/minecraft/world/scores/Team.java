package net.minecraft.world.scores;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class Team {
    public boolean isAlliedTo(@Nullable Team param0) {
        if (param0 == null) {
            return false;
        } else {
            return this == param0;
        }
    }

    public abstract String getName();

    public abstract MutableComponent getFormattedName(Component var1);

    public abstract boolean canSeeFriendlyInvisibles();

    public abstract boolean isAllowFriendlyFire();

    public abstract Team.Visibility getNameTagVisibility();

    public abstract ChatFormatting getColor();

    public abstract Collection<String> getPlayers();

    public abstract Team.Visibility getDeathMessageVisibility();

    public abstract Team.CollisionRule getCollisionRule();

    public static enum CollisionRule {
        ALWAYS("always", 0),
        NEVER("never", 1),
        PUSH_OTHER_TEAMS("pushOtherTeams", 2),
        PUSH_OWN_TEAM("pushOwnTeam", 3);

        private static final Map<String, Team.CollisionRule> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(param0 -> param0.name, param0 -> param0));
        public final String name;
        public final int id;

        @Nullable
        public static Team.CollisionRule byName(String param0) {
            return BY_NAME.get(param0);
        }

        private CollisionRule(String param0, int param1) {
            this.name = param0;
            this.id = param1;
        }

        public Component getDisplayName() {
            return Component.translatable("team.collision." + this.name);
        }
    }

    public static enum Visibility {
        ALWAYS("always", 0),
        NEVER("never", 1),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

        private static final Map<String, Team.Visibility> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(param0 -> param0.name, param0 -> param0));
        public final String name;
        public final int id;

        public static String[] getAllNames() {
            return BY_NAME.keySet().toArray(new String[0]);
        }

        @Nullable
        public static Team.Visibility byName(String param0) {
            return BY_NAME.get(param0);
        }

        private Visibility(String param0, int param1) {
            this.name = param0;
            this.id = param1;
        }

        public Component getDisplayName() {
            return Component.translatable("team.visibility." + this.name);
        }
    }
}
