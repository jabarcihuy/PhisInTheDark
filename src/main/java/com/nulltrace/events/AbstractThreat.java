package com.nulltrace.events;

public abstract class AbstractThreat implements Threat {
    private final String name;

    protected AbstractThreat(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
