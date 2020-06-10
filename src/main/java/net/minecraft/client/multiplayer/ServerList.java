package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerList {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final List<ServerData> serverList = Lists.newArrayList();

    public ServerList(Minecraft param0) {
        this.minecraft = param0;
        this.load();
    }

    public void load() {
        try {
            this.serverList.clear();
            CompoundTag var0 = NbtIo.read(new File(this.minecraft.gameDirectory, "servers.dat"));
            if (var0 == null) {
                return;
            }

            ListTag var1 = var0.getList("servers", 10);

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                this.serverList.add(ServerData.read(var1.getCompound(var2)));
            }
        } catch (Exception var4) {
            LOGGER.error("Couldn't load server list", (Throwable)var4);
        }

    }

    public void save() {
        try {
            ListTag var0 = new ListTag();

            for(ServerData var1 : this.serverList) {
                var0.add(var1.write());
            }

            CompoundTag var2 = new CompoundTag();
            var2.put("servers", var0);
            File var3 = File.createTempFile("servers", ".dat", this.minecraft.gameDirectory);
            NbtIo.write(var2, var3);
            File var4 = new File(this.minecraft.gameDirectory, "servers.dat_old");
            File var5 = new File(this.minecraft.gameDirectory, "servers.dat");
            Util.safeReplaceFile(var5, var3, var4);
        } catch (Exception var61) {
            LOGGER.error("Couldn't save server list", (Throwable)var61);
        }

    }

    public ServerData get(int param0) {
        return this.serverList.get(param0);
    }

    public void remove(ServerData param0) {
        this.serverList.remove(param0);
    }

    public void add(ServerData param0) {
        this.serverList.add(param0);
    }

    public int size() {
        return this.serverList.size();
    }

    public void swap(int param0, int param1) {
        ServerData var0 = this.get(param0);
        this.serverList.set(param0, this.get(param1));
        this.serverList.set(param1, var0);
        this.save();
    }

    public void replace(int param0, ServerData param1) {
        this.serverList.set(param0, param1);
    }

    public static void saveSingleServer(ServerData param0) {
        ServerList var0 = new ServerList(Minecraft.getInstance());
        var0.load();

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            ServerData var2 = var0.get(var1);
            if (var2.name.equals(param0.name) && var2.ip.equals(param0.ip)) {
                var0.replace(var1, param0);
                break;
            }
        }

        var0.save();
    }
}
