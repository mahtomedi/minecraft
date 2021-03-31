package net.minecraft.gametest.framework;

import java.util.function.Consumer;
import net.minecraft.world.level.block.Rotation;

public class TestFunction {
    private final String batchName;
    private final String testName;
    private final String structureName;
    private final boolean required;
    private final int maxAttempts;
    private final int requiredSuccesses;
    private final Consumer<GameTestHelper> function;
    private final int maxTicks;
    private final long setupTicks;
    private final Rotation rotation;

    public TestFunction(String param0, String param1, String param2, int param3, long param4, boolean param5, Consumer<GameTestHelper> param6) {
        this(param0, param1, param2, Rotation.NONE, param3, param4, param5, 1, 1, param6);
    }

    public TestFunction(String param0, String param1, String param2, Rotation param3, int param4, long param5, boolean param6, Consumer<GameTestHelper> param7) {
        this(param0, param1, param2, param3, param4, param5, param6, 1, 1, param7);
    }

    public TestFunction(
        String param0,
        String param1,
        String param2,
        Rotation param3,
        int param4,
        long param5,
        boolean param6,
        int param7,
        int param8,
        Consumer<GameTestHelper> param9
    ) {
        this.batchName = param0;
        this.testName = param1;
        this.structureName = param2;
        this.rotation = param3;
        this.maxTicks = param4;
        this.required = param6;
        this.requiredSuccesses = param7;
        this.maxAttempts = param8;
        this.function = param9;
        this.setupTicks = param5;
    }

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

    public boolean isFlaky() {
        return this.maxAttempts > 1;
    }

    public int getMaxAttempts() {
        return this.maxAttempts;
    }

    public int getRequiredSuccesses() {
        return this.requiredSuccesses;
    }
}
