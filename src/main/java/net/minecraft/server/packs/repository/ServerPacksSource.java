package net.minecraft.server.packs.repository;

import java.util.Map;
import net.minecraft.server.packs.VanillaPack;

public class ServerPacksSource implements RepositorySource {
    private final VanillaPack vanillaPack = new VanillaPack("minecraft");

    @Override
    public <T extends UnopenedPack> void loadPacks(Map<String, T> param0, UnopenedPack.UnopenedPackConstructor<T> param1) {
        T var0 = UnopenedPack.create("vanilla", false, () -> this.vanillaPack, param1, UnopenedPack.Position.BOTTOM);
        if (var0 != null) {
            param0.put("vanilla", var0);
        }

    }
}
