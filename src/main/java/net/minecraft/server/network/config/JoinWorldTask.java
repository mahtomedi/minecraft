package net.minecraft.server.network.config;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.server.network.ConfigurationTask;

public class JoinWorldTask implements ConfigurationTask {
    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type("join_world");

    @Override
    public void start(Consumer<Packet<?>> param0) {
        param0.accept(new ClientboundFinishConfigurationPacket());
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}
