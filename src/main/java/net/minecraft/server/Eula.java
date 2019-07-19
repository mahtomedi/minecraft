package net.minecraft.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Eula {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Path file;
    private final boolean agreed;

    public Eula(Path param0) {
        this.file = param0;
        this.agreed = SharedConstants.IS_RUNNING_IN_IDE || this.readFile();
    }

    private boolean readFile() {
        try (InputStream var0 = Files.newInputStream(this.file)) {
            Properties var1 = new Properties();
            var1.load(var0);
            return Boolean.parseBoolean(var1.getProperty("eula", "false"));
        } catch (Exception var16) {
            LOGGER.warn("Failed to load {}", this.file);
            this.saveDefaults();
            return false;
        }
    }

    public boolean hasAgreedToEULA() {
        return this.agreed;
    }

    private void saveDefaults() {
        if (!SharedConstants.IS_RUNNING_IN_IDE) {
            try (OutputStream var0 = Files.newOutputStream(this.file)) {
                Properties var1 = new Properties();
                var1.setProperty("eula", "false");
                var1.store(
                    var0,
                    "By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula)."
                );
            } catch (Exception var14) {
                LOGGER.warn("Failed to save {}", this.file, var14);
            }

        }
    }
}
