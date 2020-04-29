package net.minecraft.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;

public interface CriterionTrigger<T extends CriterionTriggerInstance> {
    ResourceLocation getId();

    void addPlayerListener(PlayerAdvancements var1, CriterionTrigger.Listener<T> var2);

    void removePlayerListener(PlayerAdvancements var1, CriterionTrigger.Listener<T> var2);

    void removePlayerListeners(PlayerAdvancements var1);

    T createInstance(JsonObject var1, DeserializationContext var2);

    public static class Listener<T extends CriterionTriggerInstance> {
        private final T trigger;
        private final Advancement advancement;
        private final String criterion;

        public Listener(T param0, Advancement param1, String param2) {
            this.trigger = param0;
            this.advancement = param1;
            this.criterion = param2;
        }

        public T getTriggerInstance() {
            return this.trigger;
        }

        public void run(PlayerAdvancements param0) {
            param0.award(this.advancement, this.criterion);
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                CriterionTrigger.Listener<?> var0 = (CriterionTrigger.Listener)param0;
                if (!this.trigger.equals(var0.trigger)) {
                    return false;
                } else {
                    return !this.advancement.equals(var0.advancement) ? false : this.criterion.equals(var0.criterion);
                }
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int var0 = this.trigger.hashCode();
            var0 = 31 * var0 + this.advancement.hashCode();
            return 31 * var0 + this.criterion.hashCode();
        }
    }
}
