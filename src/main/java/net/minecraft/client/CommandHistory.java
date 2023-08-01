package net.minecraft.client;

import com.google.common.base.Charsets;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import net.minecraft.util.ArrayListDeque;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class CommandHistory {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_PERSISTED_COMMAND_HISTORY = 50;
    private static final String PERSISTED_COMMANDS_FILE_NAME = "command_history.txt";
    private final Path commandsPath;
    private final ArrayListDeque<String> lastCommands = new ArrayListDeque<>(50);

    public CommandHistory(Path param0) {
        this.commandsPath = param0.resolve("command_history.txt");
        if (Files.exists(this.commandsPath)) {
            try (BufferedReader var0 = Files.newBufferedReader(this.commandsPath, Charsets.UTF_8)) {
                this.lastCommands.addAll(var0.lines().toList());
            } catch (Exception var7) {
                LOGGER.error("Failed to read {}, command history will be missing", "command_history.txt", var7);
            }
        }

    }

    public void addCommand(String param0) {
        if (!param0.equals(this.lastCommands.peekLast())) {
            if (this.lastCommands.size() >= 50) {
                this.lastCommands.removeFirst();
            }

            this.lastCommands.addLast(param0);
            this.save();
        }

    }

    private void save() {
        try (BufferedWriter var0 = Files.newBufferedWriter(this.commandsPath, Charsets.UTF_8)) {
            for(String var1 : this.lastCommands) {
                var0.write(var1);
                var0.newLine();
            }
        } catch (IOException var6) {
            LOGGER.error("Failed to write {}, command history will be missing", "command_history.txt", var6);
        }

    }

    public Collection<String> history() {
        return this.lastCommands;
    }
}
