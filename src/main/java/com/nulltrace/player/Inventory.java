package com.nulltrace.player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Inventory {
    private final Set<String> items;

    public Inventory() {
        this.items = new LinkedHashSet<>();
    }

    public boolean addItem(String item) {
        if (item == null || item.isBlank()) {
            return false;
        }
        return items.add(item.trim());
    }

    public boolean hasItem(String item) {
        return items.contains(item);
    }

    public void restoreItems(Collection<String> restoredItems) {
        items.clear();
        for (String item : restoredItems) {
            addItem(item);
        }
    }

    public void clear() {
        items.clear();
    }

    public Set<String> getItems() {
        return Collections.unmodifiableSet(items);
    }
}
