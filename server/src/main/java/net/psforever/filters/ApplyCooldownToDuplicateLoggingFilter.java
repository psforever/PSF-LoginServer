// Copyright (c) 2021 PSForever
package net.psforever.filters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Disrupts a variety of logging messages that would otherwise repeat within a certain frame of time.
 * Until there is a significant break in time between the logging of the duplicated messages,
 * those messages are denied logging.
 * Only exact matches via hash are denied.
 * Be aware of the pitfalls of default `String` hash code.
 */
public class ApplyCooldownToDuplicateLoggingFilter extends Filter<ILoggingEvent> {
    private long cooldown;
    private ConcurrentHashMap<String, Long> messageMap;
    private long cleaning = 900000L; //default: 15min
    private ScheduledExecutorService housecleaning;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String msg = event.getMessage();
        long currTime = System.currentTimeMillis();
        Long previousTime = messageMap.put(msg, currTime);
        if (previousTime != null && previousTime + cooldown > currTime) {
            return FilterReply.DENY;
        } else {
            return FilterReply.NEUTRAL;
        }
    }

    public void setCooldown(Long duration) {
        this.cooldown = duration;
    }

    public void setCleaning(Long duration) {
        this.cleaning = duration;
    }

    @Override
    public void start() {
        if (this.cooldown != 0L) {
            messageMap = new ConcurrentHashMap<>(1000);
            housecleaning = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                //being "concurrent" should be enough
                //the worst that can happen is two of the same message back-to-back in the log once in a while
                if (!messageMap.isEmpty()) {
                    long currTime = System.currentTimeMillis();
                    Iterator<String> oldLogMessages = messageMap.entrySet().stream()
                            .filter( entry -> entry.getValue() + cooldown < currTime )
                            .map( Map.Entry::getKey )
                            .iterator();
                    oldLogMessages.forEachRemaining(key -> messageMap.remove(key));
                }
            };
            housecleaning.scheduleWithFixedDelay(task, cleaning, cleaning, TimeUnit.MILLISECONDS);
            super.start();
        }
    }

    @Override
    public void stop() {
        housecleaning.shutdownNow();
        messageMap.clear();
        messageMap = null;
        super.stop();
    }
}
