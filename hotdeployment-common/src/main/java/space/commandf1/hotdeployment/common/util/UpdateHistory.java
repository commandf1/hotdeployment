package space.commandf1.hotdeployment.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class UpdateHistory {
    private static final UpdateHistory INSTANCE = new UpdateHistory();

    public static UpdateHistory getInstance() {
        return INSTANCE;
    }

    @Getter
    @AllArgsConstructor
    public static class Record {
        private final long timestamp;
        private final String pluginName;
        private final int updatedClassCount;
        private final int updatedResourceCount;
        private final String note;

        public String format() {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
            return fmt.format(Instant.ofEpochMilli(timestamp)) + " | plugin=" + pluginName +
                    ", classes=" + updatedClassCount + ", resources=" + updatedResourceCount +
                    (note != null && !note.isEmpty() ? ", note=" + note : "");
        }
    }

    private final Deque<Record> records = new ArrayDeque<>();
    private int capacity = 100;

    public synchronized void setCapacity(int capacity) {
        if (capacity <= 0) return;
        this.capacity = capacity;
        while (records.size() > capacity) {
            records.removeFirst();
        }
    }

    public synchronized void add(String pluginName, int classCount, int resourceCount, String note) {
        records.addLast(new Record(System.currentTimeMillis(), pluginName, classCount, resourceCount, note));
        while (records.size() > capacity) {
            records.removeFirst();
        }
    }

    public synchronized List<Record> listRecent(int max) {
        int n = Math.max(0, Math.min(max, records.size()));
        List<Record> list = new ArrayList<>(n);
        int i = 0;
        for (Record r : records) {
            if (i++ >= records.size() - n) list.add(r);
        }
        return list;
    }

    public synchronized int size() {
        return records.size();
    }
}


