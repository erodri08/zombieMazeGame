import java.nio.file.*;
import java.util.*;

/**
 * JSON-based leaderboard stored in leaderboard.json 
 * Tracks top 10 entries sorted by fastest time.
 * @author Ethan Rodrigues
 */
public class Leaderboard {

    public static class Entry implements Comparable<Entry> {
        public String name;
        public long   timeMs;
        public String timeStr;

        public Entry(String name, long timeMs) {
            this.name    = name;
            this.timeMs  = timeMs;
            this.timeStr = formatTime(timeMs);
        }

        @Override
        public int compareTo(Entry o) {
            return Long.compare(this.timeMs, o.timeMs);
        }

        public static String formatTime(long ms) {
            long s  = ms / 1000;
            long m  = s / 60;
            s = s % 60;
            long cs = (ms % 1000) / 10;
            return String.format("%d:%02d.%02d", m, s, cs);
        }
    }

    private static final String FILE = "leaderboard.json";
    private static final int    MAX  = 10;

    private List<Entry> entries = new ArrayList<>();

    public Leaderboard() {
        load();
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /** Returns the rank (1-based) of the given time, or -1 if not on board. */
    public int getRank(long timeMs) {
        List<Entry> tmp = new ArrayList<>(entries);
        tmp.add(new Entry("?", timeMs));
        Collections.sort(tmp);
        for (int i = 0; i < tmp.size(); i++) {
            if (tmp.get(i).timeMs == timeMs) return i + 1;
        }
        return -1;
    }

    /** Add an entry and re-sort. Returns the rank. */
    public int addEntry(String name, long timeMs) {
        entries.add(new Entry(name.trim().isEmpty() ? "???" : name.trim(), timeMs));
        Collections.sort(entries);
        if (entries.size() > MAX) entries = entries.subList(0, MAX);
        save();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).timeMs == timeMs) return i + 1;
        }
        return -1;
    }

    /** True if this time would make it onto the leaderboard. */
    public boolean isTopScore(long timeMs) {
        if (entries.size() < MAX) return true;
        return timeMs < entries.get(entries.size() - 1).timeMs;
    }

    private void load() {
        entries = new ArrayList<>();
        try {
            String json = new String(Files.readAllBytes(Paths.get(FILE)));
            json = json.trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]"))   json = json.substring(0, json.length() - 1);
            String[] objs = json.split("\\},\\s*\\{");
            for (String obj : objs) {
                obj = obj.replace("{", "").replace("}", "").trim();
                if (obj.isEmpty()) continue;
                String name = extractStr(obj, "name");
                long   ms   = extractLong(obj, "timeMs");
                if (name != null && ms > 0) entries.add(new Entry(name, ms));
            }
            Collections.sort(entries);
        } catch (Exception ignored) {
            // File not found or malformed — start fresh
        }
    }

    private void save() {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            String safe = e.name.replace("\"", "\\\"");
            sb.append("  {\"name\":\"").append(safe)
              .append("\",\"timeMs\":").append(e.timeMs).append("}");
            if (i < entries.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        try {
            Files.write(Paths.get(FILE), sb.toString().getBytes());
        } catch (Exception ignored) {}
    }

    private static String extractStr(String obj, String key) {
        String search = "\"" + key + "\"";
        int idx = obj.indexOf(search);
        if (idx < 0) return null;
        int q1 = obj.indexOf('"', idx + search.length() + 1);
        int q2 = obj.indexOf('"', q1 + 1);
        if (q1 < 0 || q2 < 0) return null;
        return obj.substring(q1 + 1, q2);
    }

    private static long extractLong(String obj, String key) {
        String search = "\"" + key + "\":";
        int idx = obj.indexOf(search);
        if (idx < 0) return -1;
        int start = idx + search.length();
        int end = start;
        while (end < obj.length() && (Character.isDigit(obj.charAt(end)) || obj.charAt(end) == '-')) end++;
        try { return Long.parseLong(obj.substring(start, end).trim()); }
        catch (Exception e) { return -1; }
    }
}
