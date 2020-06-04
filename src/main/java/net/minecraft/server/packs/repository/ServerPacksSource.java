package net.minecraft.server.packs.repository;

import java.util.function.Consumer;
import net.minecraft.server.packs.VanillaPackResources;

public class ServerPacksSource implements RepositorySource {
    private final VanillaPackResources vanillaPack = new VanillaPackResources("minecraft");

    @Override
    public <T extends Pack> void loadPacks(Consumer<T> param0, Pack.PackConstructor<T> param1) {
        T var0 = Pack.create("vanilla", false, () -> this.vanillaPack, param1, Pack.Position.BOTTOM, PackSource.BUILT_IN);
        if (var0 != null) {
            param0.accept(var0);
        }

    }
}
