// Copyright (c) 2021 PSForever
package net.psforever.filters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Disrupts a variety of logging messages that would otherwise repeat within a certain frame of time.
 * Until there is a significant break in time between the logging of the duplicated messages,
 * those messages are denied logging.
 * Only exact matches via hash are denied.
 * Be aware of the pitfalls of default `String` hash code.
 */
public class ApplyCooldownToDuplicateLoggingFilter extends Filter<ILoggingEvent> {
    private long cooldown;
    private long cleaning = 900000L; //default: 15min
    private ConcurrentHashMap<String, Long> messageMap;
    private long housecleaningTime = System.currentTimeMillis() + cleaning;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String msg = event.getMessage();
        long currTime = System.currentTimeMillis();
        Long previousTime = messageMap.put(msg, currTime);
        if (previousTime != null && previousTime + cooldown > currTime) {
            return FilterReply.DENY;
        } else {
            if (currTime > housecleaningTime) {
                runCleaning();
                housecleaningTime = currTime + cleaning;
            }
            return FilterReply.NEUTRAL;
        }
    }

    public void setCooldown(Long duration) {
        cooldown = duration;
    }

    public void setCleaning(Long duration) {
        cleaning = duration;
        housecleaningTime = System.currentTimeMillis() + cleaning;
    }

    private void runCleaning() {
        if (!messageMap.isEmpty()) {
            long currTime = System.currentTimeMillis();
            Iterator<String> oldLogMessages = messageMap.entrySet().stream()
                    .filter( entry -> entry.getValue() + cooldown < currTime )
                    .map( Map.Entry::getKey )
                    .iterator();
            oldLogMessages.forEachRemaining(key -> messageMap.remove(key));
        }
    }

    @Override
    public void stop() {
        messageMap.clear();
        messageMap = null;
        super.stop();
    }
}
