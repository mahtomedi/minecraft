package net.minecraft.gametest.framework;

class ExhaustedAttemptsException extends Throwable {
    public ExhaustedAttemptsException(int param0, int param1, GameTestInfo param2) {
        super(
            "Not enough successes: "
                + param1
                + " out of "
                + param0
                + " attempts. Required successes: "
                + param2.requiredSuccesses()
                + ". max attempts: "
                + param2.maxAttempts()
                + ".",
            param2.getError()
        );
    }
}
