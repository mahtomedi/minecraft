package net.minecraft.commands;

@FunctionalInterface
public interface CommandResultCallback {
    CommandResultCallback EMPTY = new CommandResultCallback() {
        @Override
        public void onResult(boolean param0, int param1) {
        }

        @Override
        public String toString() {
            return "<empty>";
        }
    };

    void onResult(boolean var1, int var2);

    default void onSuccess(int param0) {
        this.onResult(true, param0);
    }

    default void onFailure() {
        this.onResult(false, 0);
    }

    static CommandResultCallback chain(CommandResultCallback param0, CommandResultCallback param1) {
        if (param0 == EMPTY) {
            return param1;
        } else {
            return param1 == EMPTY ? param0 : (param2, param3) -> {
                param0.onResult(param2, param3);
                param1.onResult(param2, param3);
            };
        }
    }
}
