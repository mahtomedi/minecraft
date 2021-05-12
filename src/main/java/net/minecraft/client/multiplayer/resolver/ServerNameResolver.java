package net.minecraft.client.multiplayer.resolver;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mojang.blocklist.BlockListSupplier;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerNameResolver {
    public static final ServerNameResolver DEFAULT = new ServerNameResolver(
        ServerAddressResolver.SYSTEM, ServerRedirectHandler.createDnsSrvRedirectHandler(), createBlockCheckFromService()
    );
    private final ServerAddressResolver resolver;
    private final ServerRedirectHandler redirectHandler;
    private final Predicate<ResolvedServerAddress> allowCheck;

    @VisibleForTesting
    ServerNameResolver(ServerAddressResolver param0, ServerRedirectHandler param1, Predicate<ResolvedServerAddress> param2) {
        this.resolver = param0;
        this.redirectHandler = param1;
        this.allowCheck = param2.negate();
    }

    private static Predicate<ResolvedServerAddress> createBlockCheckFromService() {
        ImmutableList<Predicate<String>> var0 = Streams.stream(ServiceLoader.load(BlockListSupplier.class))
            .map(BlockListSupplier::createBlockList)
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList());
        return param1 -> var0.stream().anyMatch(param1x -> param1x.test(param1.getHostName()) || param1x.test(param1.getHostIp()));
    }

    public Optional<ResolvedServerAddress> resolveAddress(ServerAddress param0) {
        Optional<ResolvedServerAddress> var0 = this.resolveAndFilter(param0);
        if (!var0.isPresent()) {
            return Optional.empty();
        } else {
            Optional<ServerAddress> var1 = this.redirectHandler.lookupRedirect(param0);
            if (var1.isPresent()) {
                var0 = this.resolveAndFilter(var1.get());
            }

            return var0;
        }
    }

    private Optional<ResolvedServerAddress> resolveAndFilter(ServerAddress param0) {
        return this.resolver.resolve(param0).filter(this.allowCheck);
    }
}
