package io.collective;

import java.time.Clock;

//  Implementing a simple aged cache with a doubly linked list with head and tail pointers.
//  There is a good balance between complexity/speed of implementation and performance.
//  A HashMap is more performant but more complex to implement.

//  The SimpleAgedCache class is essentially the Doubly Linked List.
public class SimpleAgedCache {
    //  The ExpirableEntry class is essentially a Node in the List.
    class ExpirableEntry {
        ExpirableEntry next;
        ExpirableEntry prev;
        Object key;
        Object value;
        int retentionInMillis;

        public ExpirableEntry(Object key, Object value, int retentionInMillis) {
            this.next = null;
            this.prev = null;
            this.key = key;
            this.value = value;
            this.retentionInMillis = (int) clock.millis() + retentionInMillis;
        }
    }

    private final Clock clock;
    private ExpirableEntry head;
    private ExpirableEntry tail;
    private int size;

    public SimpleAgedCache(Clock clock) {
        head = null;
        tail = null;
        size = 0;
        this.clock = clock;
    }

    // Constructor when no clock is specified defaults to system clock.
    public SimpleAgedCache() {
        head = null;
        tail = null;
        size = 0;
        clock = Clock.systemDefaultZone();
    }

    // Search for entry with given key in cache.
    // Return entry if found, otherwise return null.
    private ExpirableEntry search(Object key) {
        ExpirableEntry curr = head;
        while (curr != null) {
            if (curr.key == key) {
                return curr;
            }
            curr = curr.next;
        }
        return null;
    }

    //  Flushes the cache of expired entries
    private void flush() {
        ExpirableEntry curr = head;
        while (curr != null) {
            if ((int) clock.millis() > curr.retentionInMillis) {
                // If entry is expired, adjust pointers of prev/next entries
                if (curr == head) {
                    head = head.next;
                }
                else {
                    curr.prev.next = curr.next;
                }
                if (curr != tail) {
                    curr.next.prev = curr.prev;
                }
                size--;
            }
            curr = curr.next;
        }
        if (size == 0) {
            head = null;
            tail = null;
        }
    }

    //  Inserts a new key-value-retention entry in the cache or
    //  updates an existing entry with new values
    public void put(Object key, Object value, int retentionInMillis) {
        // Search for existing cache entry with specified key
        ExpirableEntry existingEntry = search(key);
        if (existingEntry != null) {
            // Update existing entry
            existingEntry.value = value;
            existingEntry.retentionInMillis = (int) clock.millis() + retentionInMillis;
        }
        else {
            // Existing entry not found, so create a new one
            ExpirableEntry entry = new ExpirableEntry(key, value, retentionInMillis);
            if (head == null) {
                // If the cache was empty, set new head
                head = entry;
            }
            else {
                // Add new entry to the end of the cache/linked list
                tail.next = entry;
                entry.prev = tail;
            }
            tail = entry;
            size++;
        }
    }

    public boolean isEmpty() {
        return (size() == 0);
    }

    public int size() {
        // Flush expired entries before returning the size
        flush();
        return size;
    }

    public Object get(Object key) {
        // Flush expired entries before returning value
        flush();
        ExpirableEntry existingEntry = search(key);
        if (existingEntry != null) {
            return existingEntry.value;
        }
        else return null;
    }
}