package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.brigadier.CommandDispatcher;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.storage.LevelStorageSource;

public class CommandsReport implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public CommandsReport(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(HashCache param0) throws IOException {
        YggdrasilAuthenticationService var0 = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
        MinecraftSessionService var1 = var0.createMinecraftSessionService();
        GameProfileRepository var2 = var0.createProfileRepository();
        File var3 = new File(this.generator.getOutputFolder().toFile(), "tmp");
        GameProfileCache var4 = new GameProfileCache(var2, new File(var3, MinecraftServer.USERID_CACHE_FILE.getName()));
        DedicatedServerSettings var5 = new DedicatedServerSettings(Paths.get("server.properties"));
        LevelStorageSource.LevelStorageAccess var6 = LevelStorageSource.createDefault(var3.toPath()).createAccess("world");
        MinecraftServer var7 = new DedicatedServer(var6, var5, DataFixers.getDataFixer(), var1, var2, var4, LoggerChunkProgressListener::new);
        Path var8 = this.generator.getOutputFolder().resolve("reports/commands.json");
        CommandDispatcher<CommandSourceStack> var9 = var7.getCommands().getDispatcher();
        DataProvider.save(GSON, param0, ArgumentTypes.serializeNodeToJson(var9, var9.getRoot()), var8);
    }

    @Override
    public String getName() {
        return "Command Syntax";
    }
}
