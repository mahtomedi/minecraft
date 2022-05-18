package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ProcessorMailbox<Runnable> IO_MAILBOX = ProcessorMailbox.create(Util.backgroundExecutor(), "server-list-io");
    private static final int MAX_HIDDEN_SERVERS = 16;
    private final Minecraft minecraft;
    private final List<ServerData> serverList = Lists.newArrayList();
    private final List<ServerData> hiddenServerList = Lists.newArrayList();

    public ServerList(Minecraft param0) {
        this.minecraft = param0;
        this.load();
    }

    public void load() {
        try {
            this.serverList.clear();
            this.hiddenServerList.clear();
            CompoundTag var0 = NbtIo.read(new File(this.minecraft.gameDirectory, "servers.dat"));
            if (var0 == null) {
                return;
            }

            ListTag var1 = var0.getList("servers", 10);

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                CompoundTag var3 = var1.getCompound(var2);
                ServerData var4 = ServerData.read(var3);
                if (var3.getBoolean("hidden")) {
                    this.hiddenServerList.add(var4);
                } else {
                    this.serverList.add(var4);
                }
            }
        } catch (Exception var6) {
            LOGGER.error("Couldn't load server list", (Throwable)var6);
        }

    }

    public void save() {
        try {
            ListTag var0 = new ListTag();

            for(ServerData var1 : this.serverList) {
                CompoundTag var2 = var1.write();
                var2.putBoolean("hidden", false);
                var0.add(var2);
            }

            for(ServerData var3 : this.hiddenServerList) {
                CompoundTag var4 = var3.write();
                var4.putBoolean("hidden", true);
                var0.add(var4);
            }

            CompoundTag var5 = new CompoundTag();
            var5.put("servers", var0);
            File var6 = File.createTempFile("servers", ".dat", this.minecraft.gameDirectory);
            NbtIo.write(var5, var6);
            File var7 = new File(this.minecraft.gameDirectory, "servers.dat_old");
            File var8 = new File(this.minecraft.gameDirectory, "servers.dat");
            Util.safeReplaceFile(var8, var6, var7);
        } catch (Exception var61) {
            LOGGER.error("Couldn't save server list", (Throwable)var61);
        }

    }

    public ServerData get(int param0) {
        return this.serverList.get(param0);
    }

    @Nullable
    public ServerData get(String param0) {
        for(ServerData var0 : this.serverList) {
            if (var0.ip.equals(param0)) {
                return var0;
            }
        }

        for(ServerData var1 : this.hiddenServerList) {
            if (var1.ip.equals(param0)) {
                return var1;
            }
        }

        return null;
    }

    @Nullable
    public ServerData unhide(String param0) {
        for(int var0 = 0; var0 < this.hiddenServerList.size(); ++var0) {
            ServerData var1 = this.hiddenServerList.get(var0);
            if (var1.ip.equals(param0)) {
                this.hiddenServerList.remove(var0);
                this.serverList.add(var1);
                return var1;
            }
        }

        return null;
    }

    public void remove(ServerData param0) {
        if (!this.serverList.remove(param0)) {
            this.hiddenServerList.remove(param0);
        }

    }

    public void add(ServerData param0, boolean param1) {
        if (param1) {
            this.hiddenServerList.add(0, param0);

            while(this.hiddenServerList.size() > 16) {
                this.hiddenServerList.remove(this.hiddenServerList.size() - 1);
            }
        } else {
            this.serverList.add(param0);
        }

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

    private static boolean set(ServerData param0, List<ServerData> param1) {
        for(int var0 = 0; var0 < param1.size(); ++var0) {
            ServerData var1 = param1.get(var0);
            if (var1.name.equals(param0.name) && var1.ip.equals(param0.ip)) {
                param1.set(var0, param0);
                return true;
            }
        }

        return false;
    }

    public static void saveSingleServer(ServerData param0) {
        IO_MAILBOX.tell(() -> {
            ServerList var0x = new ServerList(Minecraft.getInstance());
            var0x.load();
            if (!set(param0, var0x.serverList)) {
                set(param0, var0x.hiddenServerList);
            }

            var0x.save();
        });
    }
}
