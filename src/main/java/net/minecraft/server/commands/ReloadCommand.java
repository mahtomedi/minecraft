package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class ReloadCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void reloadPacks(Collection<String> param0, CommandSourceStack param1) {
        param1.getServer().reloadResources(param0).exceptionally(param1x -> {
            LOGGER.warn("Failed to execute reload", param1x);
            param1.sendFailure(Component.translatable("commands.reload.failure"));
            return null;
        });
    }

    private static Collection<String> discoverNewPacks(PackRepository param0, WorldData param1, Collection<String> param2) {
        param0.reload();
        Collection<String> var0 = Lists.newArrayList(param2);
        Collection<String> var1 = param1.getDataPackConfig().getDisabled();

        for(String var2 : param0.getAvailableIds()) {
            if (!var1.contains(var2) && !var0.contains(var2)) {
                var0.add(var2);
            }
        }

        return var0;
    }

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(Commands.literal("reload").requires(param0x -> param0x.hasPermission(2)).executes(param0x -> {
            CommandSourceStack var0x = param0x.getSource();
            MinecraftServer var1 = var0x.getServer();
            PackRepository var2 = var1.getPackRepository();
            WorldData var3 = var1.getWorldData();
            Collection<String> var4 = var2.getSelectedIds();
            Collection<String> var5 = discoverNewPacks(var2, var3, var4);
            var0x.sendSuccess(Component.translatable("commands.reload.success"), true);
            reloadPacks(var5, var0x);
            return 0;
        }));
    }
}
