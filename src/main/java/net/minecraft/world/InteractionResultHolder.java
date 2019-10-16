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

    public static <T> InteractionResultHolder<T> success(T param0) {
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, param0);
    }

    public static <T> InteractionResultHolder<T> consume(T param0) {
        return new InteractionResultHolder<>(InteractionResult.CONSUME, param0);
    }

    public static <T> InteractionResultHolder<T> pass(T param0) {
        return new InteractionResultHolder<>(InteractionResult.PASS, param0);
    }

    public static <T> InteractionResultHolder<T> fail(T param0) {
        return new InteractionResultHolder<>(InteractionResult.FAIL, param0);
    }
}
