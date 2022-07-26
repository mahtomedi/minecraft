package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import org.slf4j.Logger;

public class LogTestReporter implements TestReporter {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onTestFailed(GameTestInfo param0) {
        if (param0.isRequired()) {
            LOGGER.error("{} failed! {}", param0.getTestName(), Util.describeError(param0.getError()));
        } else {
            LOGGER.warn("(optional) {} failed. {}", param0.getTestName(), Util.describeError(param0.getError()));
        }

    }

    @Override
    public void onTestSuccess(GameTestInfo param0) {
    }
}
