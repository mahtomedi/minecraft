package net.minecraft.world.level.storage.loot;

public class SerializerType<T> {
    private final Serializer<? extends T> serializer;

    public SerializerType(Serializer<? extends T> param0) {
        this.serializer = param0;
    }

    public Serializer<? extends T> getSerializer() {
        return this.serializer;
    }
}
