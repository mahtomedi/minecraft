package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InteractionResultHolder<T> {
    private final InteractionResult result;
    private final T object;
    private final boolean swingOnSuccess;

    public InteractionResultHolder(InteractionResult param0, T param1, boolean param2) {
        this.result = param0;
        this.object = param1;
        this.swingOnSuccess = param2;
    }

    public InteractionResult getResult() {
        return this.result;
    }

    public T getObject() {
        return this.object;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldSwingOnSuccess() {
        return this.swingOnSuccess;
    }

    public static <T> InteractionResultHolder<T> success(T param0) {
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, param0, true);
    }

    public static <T> InteractionResultHolder<T> successNoSwing(T param0) {
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, param0, false);
    }

    public static <T> InteractionResultHolder<T> pass(@Nullable T param0) {
        return new InteractionResultHolder<>(InteractionResult.PASS, param0, false);
    }

    public static <T> InteractionResultHolder<T> fail(@Nullable T param0) {
        return new InteractionResultHolder<>(InteractionResult.FAIL, param0, false);
    }
}
