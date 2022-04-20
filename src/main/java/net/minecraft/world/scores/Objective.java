package net.minecraft.world.scores;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Objective {
    private final Scoreboard scoreboard;
    private final String name;
    private final ObjectiveCriteria criteria;
    private Component displayName;
    private Component formattedDisplayName;
    private ObjectiveCriteria.RenderType renderType;

    public Objective(Scoreboard param0, String param1, ObjectiveCriteria param2, Component param3, ObjectiveCriteria.RenderType param4) {
        this.scoreboard = param0;
        this.name = param1;
        this.criteria = param2;
        this.displayName = param3;
        this.formattedDisplayName = this.createFormattedDisplayName();
        this.renderType = param4;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public String getName() {
        return this.name;
    }

    public ObjectiveCriteria getCriteria() {
        return this.criteria;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    private Component createFormattedDisplayName() {
        return ComponentUtils.wrapInSquareBrackets(
            this.displayName.copy().withStyle(param0 -> param0.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(this.name))))
        );
    }

    public Component getFormattedDisplayName() {
        return this.formattedDisplayName;
    }

    public void setDisplayName(Component param0) {
        this.displayName = param0;
        this.formattedDisplayName = this.createFormattedDisplayName();
        this.scoreboard.onObjectiveChanged(this);
    }

    public ObjectiveCriteria.RenderType getRenderType() {
        return this.renderType;
    }

    public void setRenderType(ObjectiveCriteria.RenderType param0) {
        this.renderType = param0;
        this.scoreboard.onObjectiveChanged(this);
    }
}
