package net.minecraft.gametest.framework;

import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogTestReporter implements TestReporter {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onTestFailed(GameTestInfo param0) {
        if (param0.isRequired()) {
            LOGGER.error("{} failed! {}", param0.getTestName(), Util.describeError(param0.getError()));
        } else {
            LOGGER.warn("(optional) {} failed. {}", param0.getTestName(), Util.describeError(param0.getError()));
        }

    }
}
