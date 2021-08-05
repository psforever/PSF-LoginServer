package net.psforever.filters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Disrupts a variety of messages that, when formatted, match against the configured regular expression.
 * A replacement for the `EvaluatorFilter`.
 */
public class MsgRegexFilter extends Filter<ILoggingEvent> {
    private String regex;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (isStarted() && event.getFormattedMessage().matches(regex)) {
            return FilterReply.DENY;
        } else {
            return FilterReply.NEUTRAL;
        }
    }

    @Override
    public void start() {
        if (this.regex != null) {
            super.start();
        }
    }
}
