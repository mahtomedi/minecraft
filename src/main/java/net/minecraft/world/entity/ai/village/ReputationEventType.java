package net.minecraft.world.entity.ai.village;

public interface ReputationEventType {
    ReputationEventType ZOMBIE_VILLAGER_CURED = register("zombie_villager_cured");
    ReputationEventType GOLEM_KILLED = register("golem_killed");
    ReputationEventType VILLAGER_HURT = register("villager_hurt");
    ReputationEventType VILLAGER_KILLED = register("villager_killed");
    ReputationEventType TRADE = register("trade");

    static ReputationEventType register(final String param0) {
        return new ReputationEventType() {
            @Override
            public String toString() {
                return param0;
            }
        };
    }
}
