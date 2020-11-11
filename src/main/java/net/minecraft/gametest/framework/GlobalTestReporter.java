package net.minecraft.gametest.framework;

public class GlobalTestReporter {
    private static TestReporter DELEGATE = new LogTestReporter();

    public static void onTestFailed(GameTestInfo param0) {
        DELEGATE.onTestFailed(param0);
    }
}