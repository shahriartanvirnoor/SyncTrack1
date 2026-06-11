// DateUtils.java
package com.synctrack.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * DateUtils - Comprehensive date and time utility class for SyncTrack
 * Provides methods for date manipulation, formatting, calculations, and comparisons
 */
public class DateUtils {
    
    // ==================== CONSTANTS ====================
    
    // Formatters
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    public static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    public static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    public static final DateTimeFormatter ISO_WEEK_FORMATTER = DateTimeFormatter.ofPattern("'W'ww");
    
    // Duration constants (in seconds)
    public static final long SECONDS_PER_MINUTE = 60;
    public static final long SECONDS_PER_HOUR = 3600;
    public static final long SECONDS_PER_DAY = 86400;
    public static final long SECONDS_PER_WEEK = 604800;
    public static final long SECONDS_PER_MONTH = 2592000; // Approximate (30 days)
    
    // Time zones
    public static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    
    // ==================== FORMATTING METHODS ====================
    
    /**
     * Format LocalDate to display string (e.g., "Jan 15, 2024")
     */
    public static String formatDisplayDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DISPLAY_DATE_FORMATTER);
    }
    
    /**
     * Format LocalDateTime to display string (e.g., "Jan 15, 2024 14:30")
     */
    public static String formatDisplayDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DISPLAY_DATE_TIME_FORMATTER);
    }
    
    /**
     * Format LocalTime to display string (e.g., "02:30 PM")
     */
    public static String formatDisplayTime(LocalTime time) {
        if (time == null) return "N/A";
        return time.format(DISPLAY_TIME_FORMATTER);
    }
    
    /**
     * Format duration in seconds to human-readable string (e.g., "2h 30m")
     */
    public static String formatDuration(long seconds) {
        if (seconds < 0) return "0s";
        
        long days = seconds / SECONDS_PER_DAY;
        long hours = (seconds % SECONDS_PER_DAY) / SECONDS_PER_HOUR;
        long minutes = (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        long secs = seconds % SECONDS_PER_MINUTE;
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (secs > 0 || sb.length() == 0) sb.append(secs).append("s");
        
        return sb.toString().trim();
    }
    
    /**
     * Format duration in seconds to compact string (e.g., "2h 30m")
     */
    public static String formatDurationCompact(long seconds) {
        if (seconds < 0) return "0m";
        
        long hours = seconds / SECONDS_PER_HOUR;
        long minutes = (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm", minutes);
        } else {
            return "<1m";
        }
    }
    
    /**
     * Format duration for timer display (HH:MM:SS)
     */
    public static String formatTimerDisplay(long seconds) {
        long hours = seconds / SECONDS_PER_HOUR;
        long minutes = (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        long secs = seconds % SECONDS_PER_MINUTE;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
    
    /**
     * Format date for filename (YYYYMMDD_HHMMSS)
     */
    public static String formatForFilename(LocalDateTime dateTime) {
        return dateTime.format(FILE_NAME_FORMATTER);
    }
    
    /**
     * Get relative time string (e.g., "2 hours ago", "just now")
     */
    public static String getRelativeTimeString(LocalDateTime dateTime) {
        if (dateTime == null) return "unknown";
        
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);
        
        long seconds = duration.getSeconds();
        
        if (seconds < 10) return "just now";
        if (seconds < 60) return seconds + " seconds ago";
        
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        
        long hours = minutes / 60;
        if (hours < 24) return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        
        long days = hours / 24;
        if (days < 7) return days + " day" + (days == 1 ? "" : "s") + " ago";
        
        long weeks = days / 7;
        if (weeks < 4) return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
        
        long months = days / 30;
        if (months < 12) return months + " month" + (months == 1 ? "" : "s") + " ago";
        
        long years = days / 365;
        return years + " year" + (years == 1 ? "" : "s") + " ago";
    }
    
    // ==================== DATE CALCULATIONS ====================
    
    /**
     * Get start of day (00:00:00)
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }
    
    /**
     * Get end of day (23:59:59)
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }
    
    /**
     * Get start of week (Monday at 00:00:00)
     */
    public static LocalDateTime getStartOfWeek(LocalDate date) {
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        return monday.atStartOfDay();
    }
    
    /**
     * Get end of week (Sunday at 23:59:59)
     */
    public static LocalDateTime getEndOfWeek(LocalDate date) {
        LocalDate sunday = date.with(DayOfWeek.SUNDAY);
        return sunday.atTime(LocalTime.MAX);
    }
    
    /**
     * Get start of month (1st at 00:00:00)
     */
    public static LocalDateTime getStartOfMonth(LocalDate date) {
        LocalDate firstDay = date.withDayOfMonth(1);
        return firstDay.atStartOfDay();
    }
    
    /**
     * Get end of month (last day at 23:59:59)
     */
    public static LocalDateTime getEndOfMonth(LocalDate date) {
        LocalDate lastDay = date.withDayOfMonth(date.lengthOfMonth());
        return lastDay.atTime(LocalTime.MAX);
    }
    
    /**
     * Get start of year (Jan 1 at 00:00:00)
     */
    public static LocalDateTime getStartOfYear(LocalDate date) {
        LocalDate firstDay = date.withDayOfYear(1);
        return firstDay.atStartOfDay();
    }
    
    /**
     * Get end of year (Dec 31 at 23:59:59)
     */
    public static LocalDateTime getEndOfYear(LocalDate date) {
        LocalDate lastDay = date.withDayOfYear(date.lengthOfYear());
        return lastDay.atTime(LocalTime.MAX);
    }
    
    /**
     * Get date range for a period
     */
    public static DateRange getDateRange(PeriodType period, LocalDate referenceDate) {
        LocalDateTime start;
        LocalDateTime end = getEndOfDay(referenceDate);
        
        switch (period) {
            case DAY:
                start = getStartOfDay(referenceDate);
                break;
            case WEEK:
                start = getStartOfWeek(referenceDate);
                break;
            case MONTH:
                start = getStartOfMonth(referenceDate);
                break;
            case YEAR:
                start = getStartOfYear(referenceDate);
                break;
            default:
                start = getStartOfDay(referenceDate);
        }
        
        return new DateRange(start, end);
    }
    
    // ==================== DATE COMPARISONS ====================
    
    /**
     * Check if a date is today
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }
    
    /**
     * Check if a date is yesterday
     */
    public static boolean isYesterday(LocalDate date) {
        return date != null && date.equals(LocalDate.now().minusDays(1));
    }
    
    /**
     * Check if a date is tomorrow
     */
    public static boolean isTomorrow(LocalDate date) {
        return date != null && date.equals(LocalDate.now().plusDays(1));
    }
    
    /**
     * Check if a date is in the past
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }
    
    /**
     * Check if a date is in the future
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }
    
    /**
     * Check if a date is within the current week
     */
    public static boolean isCurrentWeek(LocalDate date) {
        if (date == null) return false;
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = now.with(DayOfWeek.SUNDAY);
        return !date.isBefore(weekStart) && !date.isAfter(weekEnd);
    }
    
    /**
     * Check if a date is within the current month
     */
    public static boolean isCurrentMonth(LocalDate date) {
        if (date == null) return false;
        LocalDate now = LocalDate.now();
        return date.getYear() == now.getYear() && date.getMonth() == now.getMonth();
    }
    
    /**
     * Get the difference between two dates in days
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }
    
    /**
     * Get the difference between two dates in weeks
     */
    public static long weeksBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.WEEKS.between(start, end);
    }
    
    /**
     * Get the difference between two dates in months
     */
    public static long monthsBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.MONTHS.between(start, end);
    }
    
    /**
     * Get the difference between two dates in years
     */
    public static long yearsBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.YEARS.between(start, end);
    }
    
    // ==================== STREAK CALCULATIONS ====================
    
    /**
     * Calculate current streak of consecutive days with activity
     * @param activityDates Set of dates where user was active
     * @return Current streak length
     */
    public static int calculateCurrentStreak(Set<LocalDate> activityDates) {
        if (activityDates == null || activityDates.isEmpty()) return 0;
        
        LocalDate today = LocalDate.now();
        int streak = 0;
        
        for (int i = 0; i < 365; i++) { // Limit to 1 year
            LocalDate checkDate = today.minusDays(i);
            if (activityDates.contains(checkDate)) {
                streak++;
            } else {
                break;
            }
        }
        
        return streak;
    }
    
    /**
     * Calculate longest streak of consecutive days with activity
     */
    public static int calculateLongestStreak(Set<LocalDate> activityDates) {
        if (activityDates == null || activityDates.isEmpty()) return 0;
        
        List<LocalDate> sortedDates = new ArrayList<>(activityDates);
        Collections.sort(sortedDates);
        
        int longestStreak = 0;
        int currentStreak = 1;
        
        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate prev = sortedDates.get(i - 1);
            LocalDate curr = sortedDates.get(i);
            
            if (ChronoUnit.DAYS.between(prev, curr) == 1) {
                currentStreak++;
            } else {
                longestStreak = Math.max(longestStreak, currentStreak);
                currentStreak = 1;
            }
        }
        
        return Math.max(longestStreak, currentStreak);
    }
    
    /**
     * Check if streak would be broken if no activity today
     */
    public static boolean wouldBreakStreak(Set<LocalDate> activityDates) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        return activityDates.contains(yesterday) && !activityDates.contains(today);
    }
    
    // ==================== DATE GENERATION ====================
    
    /**
     * Generate list of dates between start and end (inclusive)
     */
    public static List<LocalDate> generateDateRange(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;
        
        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        
        return dates;
    }
    
    /**
     * Generate list of weeks between start and end
     */
    public static List<YearWeek> generateWeekRange(YearWeek start, YearWeek end) {
        List<YearWeek> weeks = new ArrayList<>();
        YearWeek current = start;
        
        while (!current.isAfter(end)) {
            weeks.add(current);
            current = current.plusWeeks(1);
        }
        
        return weeks;
    }
    
    /**
     * Generate list of months between start and end
     */
    public static List<YearMonth> generateMonthRange(YearMonth start, YearMonth end) {
        List<YearMonth> months = new ArrayList<>();
        YearMonth current = start;
        
        while (!current.isAfter(end)) {
            months.add(current);
            current = current.plusMonths(1);
        }
        
        return months;
    }
    
    /**
     * Get all dates in the current week
     */
    public static List<LocalDate> getCurrentWeekDates() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        
        List<LocalDate> weekDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDates.add(monday.plusDays(i));
        }
        
        return weekDates;
    }
    
    /**
     * Get all dates in a specific month
     */
    public static List<LocalDate> getMonthDates(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        List<LocalDate> dates = new ArrayList<>();
        
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            dates.add(LocalDate.of(year, month, day));
        }
        
        return dates;
    }
    
    // ==================== PARSING METHODS ====================
    
    /**
     * Parse date string with multiple format attempts
     */
    public static Optional<LocalDate> parseDate(String dateString) {
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DISPLAY_DATE_FORMATTER,
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return Optional.of(LocalDate.parse(dateString, formatter));
            } catch (Exception ignored) {}
        }
        
        return Optional.empty();
    }
    
    /**
     * Parse duration string (e.g., "2h30m", "1d", "90m") to seconds
     */
    public static long parseDurationString(String durationString) {
        if (durationString == null || durationString.trim().isEmpty()) return 0;
        
        String normalized = durationString.toLowerCase().trim();
        long totalSeconds = 0;
        
        // Parse days
        if (normalized.contains("d")) {
            String daysPart = normalized.substring(0, normalized.indexOf("d"));
            try {
                totalSeconds += Long.parseLong(daysPart) * SECONDS_PER_DAY;
            } catch (NumberFormatException ignored) {}
            normalized = normalized.substring(normalized.indexOf("d") + 1);
        }
        
        // Parse hours
        if (normalized.contains("h")) {
            String hoursPart = normalized.substring(0, normalized.indexOf("h"));
            try {
                totalSeconds += Long.parseLong(hoursPart) * SECONDS_PER_HOUR;
            } catch (NumberFormatException ignored) {}
            normalized = normalized.substring(normalized.indexOf("h") + 1);
        }
        
        // Parse minutes
        if (normalized.contains("m")) {
            String minutesPart = normalized.substring(0, normalized.indexOf("m"));
            try {
                totalSeconds += Long.parseLong(minutesPart) * SECONDS_PER_MINUTE;
            } catch (NumberFormatException ignored) {}
        }
        
        return totalSeconds;
    }
    
    // ==================== TIME ZONE UTILITIES ====================
    
    /**
     * Convert LocalDateTime to UTC
     */
    public static LocalDateTime toUtc(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(DEFAULT_ZONE);
        return zoned.withZoneSameInstant(UTC_ZONE).toLocalDateTime();
    }
    
    /**
     * Convert UTC to LocalDateTime
     */
    public static LocalDateTime fromUtc(LocalDateTime utcDateTime) {
        ZonedDateTime zoned = utcDateTime.atZone(UTC_ZONE);
        return zoned.withZoneSameInstant(DEFAULT_ZONE).toLocalDateTime();
    }
    
    // ==================== BUSINESS HOURS UTILITIES ====================
    
    /**
     * Check if a time is within business hours (9 AM - 5 PM)
     */
    public static boolean isBusinessHour(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        DayOfWeek day = dateTime.getDayOfWeek();
        
        boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        boolean isBusinessTime = !time.isBefore(LocalTime.of(9, 0)) && !time.isAfter(LocalTime.of(17, 0));
        
        return isWeekday && isBusinessTime;
    }
    
    /**
     * Get next business day
     */
    public static LocalDate getNextBusinessDay(LocalDate date) {
        LocalDate nextDay = date.plusDays(1);
        while (nextDay.getDayOfWeek() == DayOfWeek.SATURDAY || nextDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }
    
    // ==================== PRODUCTIVITY TIME UTILITIES ====================
    
    /**
     * Calculate productive hours based on task completion times
     */
    public static long calculateProductiveHours(List<LocalDateTime> startTimes, List<LocalDateTime> endTimes) {
        if (startTimes == null || endTimes == null || startTimes.size() != endTimes.size()) {
            return 0;
        }
        
        long totalSeconds = 0;
        for (int i = 0; i < startTimes.size(); i++) {
            totalSeconds += Duration.between(startTimes.get(i), endTimes.get(i)).getSeconds();
        }
        
        return totalSeconds;
    }
    
    /**
     * Get the best time of day for user productivity based on historical data
     */
    public static String getBestProductivityTime(Map<Integer, Integer> hourProductivityMap) {
        if (hourProductivityMap == null || hourProductivityMap.isEmpty()) {
            return "Not enough data";
        }
        
        Map.Entry<Integer, Integer> bestHour = hourProductivityMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        
        if (bestHour == null) return "Not enough data";
        
        int hour = bestHour.getKey();
        String period = hour < 12 ? "AM" : "PM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        
        return String.format("%d:00 %s", displayHour, period);
    }
    
    // ==================== HOLIDAY & SPECIAL DATE UTILITIES ====================
    
    /**
     * Check if a date is a weekend
     */
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
    
    /**
     * Check if a date is a major holiday (simplified)
     */
    public static boolean isHoliday(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        
        // New Year's Day
        if (month == 1 && day == 1) return true;
        // Christmas
        if (month == 12 && day == 25) return true;
        // Independence Day (US)
        if (month == 7 && day == 4) return true;
        
        return false;
    }
    
    // ==================== AGE CALCULATIONS ====================
    
    /**
     * Calculate age from birthdate
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    /**
     * Get next birthday
     */
    public static LocalDate getNextBirthday(LocalDate birthDate) {
        if (birthDate == null) return null;
        
        LocalDate today = LocalDate.now();
        LocalDate nextBirthday = birthDate.withYear(today.getYear());
        
        if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            nextBirthday = nextBirthday.plusYears(1);
        }
        
        return nextBirthday;
    }
    
    // ==================== HELPER CLASSES ====================
    
    /**
     * Represents a period type for date ranges
     */
    public enum PeriodType {
        DAY, WEEK, MONTH, YEAR, CUSTOM
    }
    
    /**
     * Represents a date range with start and end
     */
    public static class DateRange {
        private final LocalDateTime start;
        private final LocalDateTime end;
        
        public DateRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
        
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }
        
        public boolean contains(LocalDateTime dateTime) {
            return !dateTime.isBefore(start) && !dateTime.isAfter(end);
        }
        
        public boolean contains(LocalDate date) {
            return contains(date.atStartOfDay());
        }
        
        public long getDurationSeconds() {
            return Duration.between(start, end).getSeconds();
        }
        
        public String getFormattedDuration() {
            return formatDuration(getDurationSeconds());
        }
    }
    
    /**
     * Represents a year-week for week-based operations
     */
    public static class YearWeek implements Comparable<YearWeek> {
        private final int year;
        private final int week;
        
        public YearWeek(int year, int week) {
            this.year = year;
            this.week = week;
        }
        
        public static YearWeek now() {
            LocalDate now = LocalDate.now();
            return new YearWeek(now.getYear(), now.get(WeekFields.ISO.weekOfWeekBasedYear()));
        }
        
        public static YearWeek of(LocalDate date) {
            return new YearWeek(date.getYear(), date.get(WeekFields.ISO.weekOfWeekBasedYear()));
        }
        
        public YearWeek plusWeeks(int weeks) {
            LocalDate date = getStartDate().plusWeeks(weeks);
            return new YearWeek(date.getYear(), date.get(WeekFields.ISO.weekOfWeekBasedYear()));
        }
        
        public LocalDate getStartDate() {
            return LocalDate.of(year, 1, 1)
                .with(WeekFields.ISO.weekOfWeekBasedYear(), week)
                .with(DayOfWeek.MONDAY);
        }
        
        public LocalDate getEndDate() {
            return getStartDate().plusDays(6);
        }
        
        public boolean isAfter(YearWeek other) {
            return compareTo(other) > 0;
        }
        
        public boolean isBefore(YearWeek other) {
            return compareTo(other) < 0;
        }
        
        @Override
        public int compareTo(YearWeek other) {
            int yearCompare = Integer.compare(this.year, other.year);
            if (yearCompare != 0) return yearCompare;
            return Integer.compare(this.week, other.week);
        }
        
        @Override
        public String toString() {
            return String.format("%d-W%02d", year, week);
        }
    }
    
    /**
     * Represents a time slot for scheduling
     */
    public static class TimeSlot {
        private final LocalDateTime start;
        private final LocalDateTime end;
        
        public TimeSlot(LocalDateTime start, long durationMinutes) {
            this.start = start;
            this.end = start.plusMinutes(durationMinutes);
        }
        
        public TimeSlot(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
        
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }
        
        public long getDurationMinutes() {
            return Duration.between(start, end).toMinutes();
        }
        
        public boolean overlaps(TimeSlot other) {
            return !this.end.isBefore(other.start) && !this.start.isAfter(other.end);
        }
        
        public boolean contains(LocalDateTime dateTime) {
            return !dateTime.isBefore(start) && !dateTime.isAfter(end);
        }
    }
    
    // ==================== STATIC INITIALIZATION ====================
    
    static {
        // Set default time zone if needed
        TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_ZONE));
    }
}

// Note: Add these imports at the top of your file:
// import java.time.*;
// import java.time.format.DateTimeFormatter;
// import java.time.temporal.*;
// import java.util.*;
// import java.util.concurrent.TimeUnit;
