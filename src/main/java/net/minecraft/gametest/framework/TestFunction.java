package net.minecraft.gametest.framework;

import java.util.function.Consumer;
import net.minecraft.world.level.block.Rotation;

public class TestFunction {
    private final String batchName;
    private final String testName;
    private final String structureName;
    private final boolean required;
    private final Consumer<GameTestHelper> function;
    private final int maxTicks;
    private final long setupTicks;
    private final Rotation rotation;

    public void run(GameTestHelper param0) {
        this.function.accept(param0);
    }

    public String getTestName() {
        return this.testName;
    }

    public String getStructureName() {
        return this.structureName;
    }

    @Override
    public String toString() {
        return this.testName;
    }

    public int getMaxTicks() {
        return this.maxTicks;
    }

    public boolean isRequired() {
        return this.required;
    }

    public String getBatchName() {
        return this.batchName;
    }

    public long getSetupTicks() {
        return this.setupTicks;
    }

    public Rotation getRotation() {
        return this.rotation;
    }
}
