package net.minecraft.network.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.PacketListener;

public interface BundlerInfo {
    int BUNDLE_SIZE_LIMIT = 4096;
    BundlerInfo EMPTY = new BundlerInfo() {
        @Override
        public void unbundlePacket(Packet<?> param0, Consumer<Packet<?>> param1) {
            param1.accept(param0);
        }

        @Nullable
        @Override
        public BundlerInfo.Bundler startPacketBundling(Packet<?> param0) {
            return null;
        }
    };

    static <T extends PacketListener, P extends BundlePacket<T>> BundlerInfo createForPacket(
        final Class<P> param0, final Function<Iterable<Packet<T>>, P> param1, final BundleDelimiterPacket<T> param2
    ) {
        return new BundlerInfo() {
            @Override
            public void unbundlePacket(Packet<?> param0x, Consumer<Packet<?>> param1x) {
                if (param0.getClass() == param0) {
                    P var0 = (P)param0;
                    param1.accept(param2);
                    var0.subPackets().forEach(param1);
                    param1.accept(param2);
                } else {
                    param1.accept(param0);
                }

            }

            @Nullable
            @Override
            public BundlerInfo.Bundler startPacketBundling(Packet<?> param0x) {
                return param0 == param2 ? new BundlerInfo.Bundler() {
                    private final List<Packet<T>> bundlePackets = new ArrayList<>();

                    @Nullable
                    @Override
                    public Packet<?> addPacket(Packet<?> param0x) {
                        if (param0 == param2) {
                            return param1.apply(this.bundlePackets);
                        } else if (this.bundlePackets.size() >= 4096) {
                            throw new IllegalStateException("Too many packets in a bundle");
                        } else {
                            this.bundlePackets.add(param0);
                            return null;
                        }
                    }
                } : null;
            }
        };
    }

    void unbundlePacket(Packet<?> var1, Consumer<Packet<?>> var2);

    @Nullable
    BundlerInfo.Bundler startPacketBundling(Packet<?> var1);

    public interface Bundler {
        @Nullable
        Packet<?> addPacket(Packet<?> var1);
    }

    public interface Provider {
        BundlerInfo bundlerInfo();
    }
}
