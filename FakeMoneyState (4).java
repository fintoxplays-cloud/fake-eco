package com.fakemoney.scoreboard.client;

/**
 * Holds the mod's entire runtime state: whether a fake value is currently
 * active, and what it should display as. Nothing here is ever sent over the
 * network - it is purely local, in-memory state used to decide what gets
 * drawn to the screen.
 */
public final class FakeMoneyState {

    private static final FakeMoneyState INSTANCE = new FakeMoneyState();

    private volatile boolean enabled = false;
    private volatile String displayValue = "";

    private FakeMoneyState() {
    }

    public static FakeMoneyState getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    /** Activates the override with an already-formatted display string, e.g. "$10B". */
    public void set(String formattedValue) {
        this.displayValue = formattedValue;
        this.enabled = true;
    }

    /** Turns the override off; the real server-provided value will show again. */
    public void clear() {
        this.enabled = false;
        this.displayValue = "";
    }
}
