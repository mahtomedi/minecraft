package net.minecraft.world.entity;

public enum MobSpawnType {
    NATURAL,
    CHUNK_GENERATION,
    SPAWNER,
    STRUCTURE,
    BREEDING,
    MOB_SUMMONED,
    JOCKEY,
    EVENT,
    CONVERSION,
    REINFORCEMENT,
    TRIGGERED,
    BUCKET,
    SPAWN_EGG,
    COMMAND,
    DISPENSER,
    PATROL,
    TRIAL_SPAWNER;

    public static boolean isSpawner(MobSpawnType param0) {
        return param0 == SPAWNER || param0 == TRIAL_SPAWNER;
    }

    public static boolean ignoresLightRequirements(MobSpawnType param0) {
        return param0 == TRIAL_SPAWNER;
    }
}
