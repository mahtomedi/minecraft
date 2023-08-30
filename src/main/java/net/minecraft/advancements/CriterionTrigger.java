package net.minecraft.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.server.PlayerAdvancements;

public interface CriterionTrigger<T extends CriterionTriggerInstance> {
    void addPlayerListener(PlayerAdvancements var1, CriterionTrigger.Listener<T> var2);

    void removePlayerListener(PlayerAdvancements var1, CriterionTrigger.Listener<T> var2);

    void removePlayerListeners(PlayerAdvancements var1);

    T createInstance(JsonObject var1, DeserializationContext var2);

    default Criterion<T> createCriterion(T param0) {
        return new Criterion<>(this, param0);
    }

    public static record Listener<T extends CriterionTriggerInstance>(T trigger, AdvancementHolder advancement, String criterion) {
        public void run(PlayerAdvancements param0) {
            param0.award(this.advancement, this.criterion);
        }
    }
}
