package net.minecraft.server.rcon;

import java.util.UUID;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class RconConsoleSource implements CommandSource {
    private static final String RCON = "Rcon";
    private static final Component RCON_COMPONENT = new TextComponent("Rcon");
    private final StringBuffer buffer = new StringBuffer();
    private final MinecraftServer server;

    public RconConsoleSource(MinecraftServer param0) {
        this.server = param0;
    }

    public void prepareForCommand() {
        this.buffer.setLength(0);
    }

    public String getCommandResponse() {
        return this.buffer.toString();
    }

    public CommandSourceStack createCommandSourceStack() {
        ServerLevel var0 = this.server.overworld();
        return new CommandSourceStack(this, Vec3.atLowerCornerOf(var0.getSharedSpawnPos()), Vec2.ZERO, var0, 4, "Rcon", RCON_COMPONENT, this.server, null);
    }

    @Override
    public void sendMessage(Component param0, UUID param1) {
        this.buffer.append(param0.getString());
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return this.server.shouldRconBroadcast();
    }
}
