package net.minecraft.world.level.saveddata;

public class SaveDataDirtyRunnable implements Runnable {
    private final SavedData savedData;

    public SaveDataDirtyRunnable(SavedData param0) {
        this.savedData = param0;
    }

    @Override
    public void run() {
        this.savedData.setDirty();
    }
}
