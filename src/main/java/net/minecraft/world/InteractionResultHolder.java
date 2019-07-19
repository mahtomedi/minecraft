package net.minecraft.world;

public class InteractionResultHolder<T> {
    private final InteractionResult result;
    private final T object;

    public InteractionResultHolder(InteractionResult param0, T param1) {
        this.result = param0;
        this.object = param1;
    }

    public InteractionResult getResult() {
        return this.result;
    }

    public T getObject() {
        return this.object;
    }
}
