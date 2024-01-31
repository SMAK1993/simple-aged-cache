package io.collective

import java.time.Clock

class SimpleAgedKache {
    inner class ExpirableEntry(var key: Any?, var value: Any?, retentionInMillis: Int) {
        var next: ExpirableEntry? = null
        var prev: ExpirableEntry? = null
        var retentionInMillis: Int = clock!!.millis().toInt() + retentionInMillis
    }

    private var clock: Clock? = null
    private var head: ExpirableEntry? = null
    private var tail: ExpirableEntry? = null
    private var size = 0

    constructor(clock: Clock?) {
        head = null
        tail = null
        size = 0
        this.clock = clock
    }

    constructor() {
        head = null
        tail = null
        size = 0
        clock = Clock.systemDefaultZone()
    }

    // Search for entry with given key in cache.
    // Return entry if found, otherwise return null.
    private fun search(key: Any): ExpirableEntry? {
        var curr = head
        while (curr != null) {
            if (curr.key === key) {
                return curr
            }
            curr = curr.next
        }
        return null
    }

    //  Flushes the cache of expired entries
    private fun flush() {
        var curr = head
        while (curr != null) {
            if (clock!!.millis().toInt() > curr.retentionInMillis) {
                // If entry is expired, adjust pointers of prev/next entries
                if (curr === head) {
                    head = head?.next
                } else {
                    curr.prev?.next = curr.next
                }
                if (curr !== tail) {
                    curr.next?.prev = curr.prev
                }
                size--
            }
            curr = curr.next
        }
        if (size == 0) {
            head = null
            tail = null
        }
    }

    //  Inserts a new key-value-retention entry in the cache or
    //  updates an existing entry with new values
    fun put(key: Any?, value: Any?, retentionInMillis: Int) {
        // Search for existing cache entry with specified key
        val existingEntry = search(key!!)
        if (existingEntry != null) {
            // Update existing entry
            existingEntry.value = value
            existingEntry.retentionInMillis = clock!!.millis().toInt() + retentionInMillis
        } else {
            // Existing entry not found, so create a new one
            val entry =  ExpirableEntry(key, value, retentionInMillis)
            if (head == null) {
                // If the cache was empty, set new head
                head = entry
            } else {
                // Add new entry to the end of the cache/linked list
                tail!!.next = entry
                entry.prev = tail
            }
            tail = entry
            size++
        }
    }

    fun isEmpty(): Boolean {
        return (size() == 0)
    }

    fun size(): Int {
        // Flush expired entries before returning the size
        flush()
        return size
    }

    fun get(key: Any?): Any? {
        // Flush expired entries before returning value
        flush()
        val existingEntry = search(key!!)
        return existingEntry?.value
    }
}