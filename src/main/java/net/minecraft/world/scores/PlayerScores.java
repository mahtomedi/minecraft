package net.minecraft.world.scores;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;

class PlayerScores {
    private final Map<Objective, Score> scores = new HashMap<>();

    @Nullable
    public Score get(Objective param0) {
        return this.scores.get(param0);
    }

    public Score getOrCreate(Objective param0, Consumer<Score> param1) {
        return this.scores.computeIfAbsent(param0, param1x -> {
            Score var0 = new Score();
            param1.accept(var0);
            return var0;
        });
    }

    public boolean remove(Objective param0) {
        return this.scores.get(param0) != null;
    }

    public boolean hasScores() {
        return !this.scores.isEmpty();
    }

    public Object2IntMap<Objective> listScores() {
        Object2IntMap<Objective> var0 = new Object2IntOpenHashMap<>();
        this.scores.forEach((param1, param2) -> var0.put(param1, param2.value()));
        return var0;
    }

    void setScore(Objective param0, Score param1) {
        this.scores.put(param0, param1);
    }

    Map<Objective, Score> listRawScores() {
        return Collections.unmodifiableMap(this.scores);
    }
}
