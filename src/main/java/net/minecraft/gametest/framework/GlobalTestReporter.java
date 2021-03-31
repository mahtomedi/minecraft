package net.minecraft.gametest.framework;

public class GlobalTestReporter {
    private static TestReporter DELEGATE = new LogTestReporter();

    public static void replaceWith(TestReporter param0) {
        DELEGATE = param0;
    }

    public static void onTestFailed(GameTestInfo param0) {
        DELEGATE.onTestFailed(param0);
    }

    public static void onTestSuccess(GameTestInfo param0) {
        DELEGATE.onTestSuccess(param0);
    }

    public static void finish() {
        DELEGATE.finish();
    }
}
