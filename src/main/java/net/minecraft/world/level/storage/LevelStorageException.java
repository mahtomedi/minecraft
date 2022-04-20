package net.minecraft.world.level.storage;

import net.minecraft.network.chat.Component;

public class LevelStorageException extends RuntimeException {
    private final Component messageComponent;

    public LevelStorageException(Component param0) {
        super(param0.getString());
        this.messageComponent = param0;
    }

    public Component getMessageComponent() {
        return this.messageComponent;
    }
}
