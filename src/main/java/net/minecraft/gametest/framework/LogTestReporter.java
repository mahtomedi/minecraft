package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import org.slf4j.Logger;

public class LogTestReporter implements TestReporter {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onTestFailed(GameTestInfo param0) {
        String var0 = param0.getStructureBlockPos().toShortString();
        if (param0.isRequired()) {
            LOGGER.error("{} failed at {}! {}", param0.getTestName(), var0, Util.describeError(param0.getError()));
        } else {
            LOGGER.warn("(optional) {} failed at {}. {}", param0.getTestName(), var0, Util.describeError(param0.getError()));
        }

    }

    @Override
    public void onTestSuccess(GameTestInfo param0) {
    }
}
