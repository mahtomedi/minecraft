package net.minecraft.world.scores;

import java.util.Comparator;
import javax.annotation.Nullable;

public class Score {
    public static final Comparator<Score> SCORE_COMPARATOR = (param0, param1) -> {
        if (param0.getScore() > param1.getScore()) {
            return 1;
        } else {
            return param0.getScore() < param1.getScore() ? -1 : param1.getOwner().compareToIgnoreCase(param0.getOwner());
        }
    };
    private final Scoreboard scoreboard;
    @Nullable
    private final Objective objective;
    private final String owner;
    private int count;
    private boolean locked;
    private boolean forceUpdate;

    public Score(Scoreboard param0, Objective param1, String param2) {
        this.scoreboard = param0;
        this.objective = param1;
        this.owner = param2;
        this.locked = true;
        this.forceUpdate = true;
    }

    public void add(int param0) {
        if (this.objective.getCriteria().isReadOnly()) {
            throw new IllegalStateException("Cannot modify read-only score");
        } else {
            this.setScore(this.getScore() + param0);
        }
    }

    public void increment() {
        this.add(1);
    }

    public int getScore() {
        return this.count;
    }

    public void reset() {
        this.setScore(0);
    }

    public void setScore(int param0) {
        int var0 = this.count;
        this.count = param0;
        if (var0 != param0 || this.forceUpdate) {
            this.forceUpdate = false;
            this.getScoreboard().onScoreChanged(this);
        }

    }

    @Nullable
    public Objective getObjective() {
        return this.objective;
    }

    public String getOwner() {
        return this.owner;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean param0) {
        this.locked = param0;
    }
}
