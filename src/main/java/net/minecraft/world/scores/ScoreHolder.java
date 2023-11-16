package net.minecraft.world.scores;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public interface ScoreHolder {
    String WILDCARD_NAME = "*";
    ScoreHolder WILDCARD = new ScoreHolder() {
        @Override
        public String getScoreboardName() {
            return "*";
        }
    };

    String getScoreboardName();

    @Nullable
    default Component getDisplayName() {
        return null;
    }

    default Component getFeedbackDisplayName() {
        Component var0 = this.getDisplayName();
        return var0 != null
            ? var0.copy().withStyle(param0 -> param0.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(this.getScoreboardName()))))
            : Component.literal(this.getScoreboardName());
    }

    static ScoreHolder forNameOnly(final String param0) {
        if (param0.equals("*")) {
            return WILDCARD;
        } else {
            final Component var0 = Component.literal(param0);
            return new ScoreHolder() {
                @Override
                public String getScoreboardName() {
                    return param0;
                }

                @Override
                public Component getFeedbackDisplayName() {
                    return var0;
                }
            };
        }
    }

    static ScoreHolder fromGameProfile(GameProfile param0) {
        final String var0 = param0.getName();
        return new ScoreHolder() {
            @Override
            public String getScoreboardName() {
                return var0;
            }
        };
    }
}
