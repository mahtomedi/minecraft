package net.minecraft.server.gui;

import java.util.Vector;
import javax.swing.JList;
import net.minecraft.server.MinecraftServer;

public class PlayerListComponent extends JList<String> {
    private final MinecraftServer server;
    private int tickCount;

    public PlayerListComponent(MinecraftServer param0) {
        this.server = param0;
        param0.addTickable(this::tick);
    }

    public void tick() {
        if (this.tickCount++ % 20 == 0) {
            Vector<String> var0 = new Vector<>();

            for(int var1 = 0; var1 < this.server.getPlayerList().getPlayers().size(); ++var1) {
                var0.add(this.server.getPlayerList().getPlayers().get(var1).getGameProfile().getName());
            }

            this.setListData(var0);
        }

    }
}
