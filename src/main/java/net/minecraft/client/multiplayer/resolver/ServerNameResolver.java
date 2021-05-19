package net.minecraft.client.multiplayer.resolver;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerNameResolver {
    public static final ServerNameResolver DEFAULT = new ServerNameResolver(
        ServerAddressResolver.SYSTEM, ServerRedirectHandler.createDnsSrvRedirectHandler(), AddressCheck.createFromService()
    );
    private final ServerAddressResolver resolver;
    private final ServerRedirectHandler redirectHandler;
    private final AddressCheck addressCheck;

    @VisibleForTesting
    ServerNameResolver(ServerAddressResolver param0, ServerRedirectHandler param1, AddressCheck param2) {
        this.resolver = param0;
        this.redirectHandler = param1;
        this.addressCheck = param2;
    }

    public Optional<ResolvedServerAddress> resolveAddress(ServerAddress param0) {
        Optional<ResolvedServerAddress> var0 = this.resolver.resolve(param0);
        if ((!var0.isPresent() || this.addressCheck.isAllowed(var0.get())) && this.addressCheck.isAllowed(param0)) {
            Optional<ServerAddress> var1 = this.redirectHandler.lookupRedirect(param0);
            if (var1.isPresent()) {
                var0 = this.resolver.resolve(var1.get()).filter(this.addressCheck::isAllowed);
            }

            return var0;
        } else {
            return Optional.empty();
        }
    }
}
