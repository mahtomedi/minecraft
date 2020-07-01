package net.minecraft.server.packs.repository;

import java.util.function.Consumer;
import net.minecraft.server.packs.VanillaPackResources;

public class ServerPacksSource implements RepositorySource {
    private final VanillaPackResources vanillaPack = new VanillaPackResources("minecraft");

    @Override
    public void loadPacks(Consumer<Pack> param0, Pack.PackConstructor param1) {
        Pack var0 = Pack.create("vanilla", false, () -> this.vanillaPack, param1, Pack.Position.BOTTOM, PackSource.BUILT_IN);
        if (var0 != null) {
            param0.accept(var0);
        }

    }
}
