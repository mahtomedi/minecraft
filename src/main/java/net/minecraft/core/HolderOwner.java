package net.minecraft.core;

public interface HolderOwner<T> {
    default boolean canSerializeIn(HolderOwner<T> param0) {
        return param0 == this;
    }
}
