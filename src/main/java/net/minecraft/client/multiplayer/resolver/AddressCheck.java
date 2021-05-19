package net.minecraft.client.multiplayer.resolver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mojang.blocklist.BlockListSupplier;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface AddressCheck {
    boolean isAllowed(ResolvedServerAddress var1);

    boolean isAllowed(ServerAddress var1);

    static AddressCheck createFromService() {
        final ImmutableList<Predicate<String>> var0 = Streams.stream(ServiceLoader.load(BlockListSupplier.class))
            .map(BlockListSupplier::createBlockList)
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList());
        return new AddressCheck() {
            @Override
            public boolean isAllowed(ResolvedServerAddress param0) {
                String var0 = param0.getHostName();
                String var1 = param0.getHostIp();
                return var0.stream().noneMatch(param2 -> param2.test(var0) || param2.test(var1));
            }

            @Override
            public boolean isAllowed(ServerAddress param0) {
                String var0 = param0.getHost();
                return var0.stream().noneMatch(param1 -> param1.test(var0));
            }
        };
    }
}
