package net.minecraft.server.packs.repository;

import java.util.function.Consumer;

public interface RepositorySource {
    <T extends Pack> void loadPacks(Consumer<T> var1, Pack.PackConstructor<T> var2);
}
