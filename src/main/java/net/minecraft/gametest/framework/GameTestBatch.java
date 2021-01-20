package net.minecraft.gametest.framework;

import java.util.Collection;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;

public class GameTestBatch {
    private final String name;
    private final Collection<TestFunction> testFunctions;
    @Nullable
    private final Consumer<ServerLevel> beforeBatchFunction;
    @Nullable
    private final Consumer<ServerLevel> afterBatchFunction;

    public GameTestBatch(String param0, Collection<TestFunction> param1, @Nullable Consumer<ServerLevel> param2, @Nullable Consumer<ServerLevel> param3) {
        if (param1.isEmpty()) {
            throw new IllegalArgumentException("A GameTestBatch must include at least one TestFunction!");
        } else {
            this.name = param0;
            this.testFunctions = param1;
            this.beforeBatchFunction = param2;
            this.afterBatchFunction = param3;
        }
    }

    public String getName() {
        return this.name;
    }

    public Collection<TestFunction> getTestFunctions() {
        return this.testFunctions;
    }

    public void runBeforeBatchFunction(ServerLevel param0) {
        if (this.beforeBatchFunction != null) {
            this.beforeBatchFunction.accept(param0);
        }

    }

    public void runAfterBatchFunction(ServerLevel param0) {
        if (this.afterBatchFunction != null) {
            this.afterBatchFunction.accept(param0);
        }

    }
}
