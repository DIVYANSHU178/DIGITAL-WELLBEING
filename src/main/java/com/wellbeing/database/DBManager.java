package com.wellbeing.database;

import com.wellbeing.model.AppStats;
import com.wellbeing.model.AppUsage;
import com.wellbeing.model.DailyUsage;
import com.wellbeing.util.AppNameUtils;
import com.wellbeing.util.TimeUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DBManager {
    private static final String DB_URL = "jdbc:sqlite:wellbeing.db";

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String createAppUsage = "CREATE TABLE IF NOT EXISTS AppUsage (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "app_name TEXT NOT NULL," +
                    "start_time TEXT NOT NULL," +
                    "end_time TEXT NOT NULL," +
                    "duration_seconds INTEGER NOT NULL" +
                    ")";
            stmt.execute(createAppUsage);

            String createDailyStats = "CREATE TABLE IF NOT EXISTS DailyStats (" +
                    "date TEXT PRIMARY KEY," +
                    "total_screen_time INTEGER DEFAULT 0" +
                    ")";
            stmt.execute(createDailyStats);

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static long getTodayTotalScreenTime() {
        String query = "SELECT total_screen_time FROM DailyStats WHERE date = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, LocalDate.now().toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total_screen_time");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get today's screen time: " + e.getMessage());
        }
        return 0;
    }

    public static List<AppStats> getTodayAppBreakdown() {
        String startOfToday = LocalDate.now().atStartOfDay().format(TimeUtils.DB_FORMATTER);
        String query = "SELECT app_name, SUM(duration_seconds) AS total, MAX(end_time) AS last_used " +
                "FROM AppUsage WHERE start_time >= ? GROUP BY app_name ORDER BY total DESC";

        List<AppStats> stats = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, startOfToday);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stats.add(new AppStats(
                            rs.getString("app_name"),
                            rs.getLong("total"),
                            parseDateTime(rs.getString("last_used"))
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get today app breakdown: " + e.getMessage());
        }
        return stats;
    }

    public static List<AppStats> getTodayAppDetails() {
        return getTodayAppBreakdown();
    }

    public static List<AppStats> getWeeklyAppBreakdown() {
        String startOfLastWeek = LocalDate.now().minusDays(7).atStartOfDay().format(TimeUtils.DB_FORMATTER);
        String query = "SELECT app_name, SUM(duration_seconds) AS total FROM AppUsage " +
                "WHERE start_time >= ? GROUP BY app_name ORDER BY total DESC";

        List<AppStats> stats = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, startOfLastWeek);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stats.add(new AppStats(rs.getString("app_name"), rs.getLong("total")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get weekly app breakdown: " + e.getMessage());
        }
        return stats;
    }

    public static List<DailyUsage> getDailyUsageLast7Days() {
        LocalDate startDate = LocalDate.now().minusDays(6);
        String query = "SELECT substr(start_time, 1, 10) AS usage_date, SUM(duration_seconds) AS total " +
                "FROM AppUsage WHERE start_time >= ? GROUP BY usage_date ORDER BY usage_date ASC";

        List<DailyUsage> results = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            results.add(new DailyUsage(
                    date,
                    date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    0
            ));
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, startDate.atStartOfDay().format(TimeUtils.DB_FORMATTER));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = LocalDate.parse(rs.getString("usage_date"));
                    long total = rs.getLong("total");
                    for (DailyUsage usage : results) {
                        if (usage.getDate().equals(date)) {
                            usage.setDurationSeconds(total);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get daily usage: " + e.getMessage());
        }
        return results;
    }

    public static void saveAppUsage(AppUsage usage) {
        String insertSql = "INSERT INTO AppUsage(app_name, start_time, end_time, duration_seconds) VALUES(?,?,?,?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            pstmt.setString(1, AppNameUtils.normalizeAppName(usage.getAppName()));
            pstmt.setString(2, usage.getStartTime().format(TimeUtils.DB_FORMATTER));
            pstmt.setString(3, usage.getEndTime().format(TimeUtils.DB_FORMATTER));
            pstmt.setLong(4, usage.getDurationSeconds());
            pstmt.executeUpdate();

            updateDailyStats(usage.getEndTime().toLocalDate(), usage.getDurationSeconds());
        } catch (SQLException e) {
            System.err.println("Failed to save app usage: " + e.getMessage());
        }
    }

    private static void updateDailyStats(LocalDate date, long additionalDuration) {
        String upsertSql = "INSERT INTO DailyStats (date, total_screen_time) VALUES (?, ?) " +
                "ON CONFLICT(date) DO UPDATE SET total_screen_time = total_screen_time + ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(upsertSql)) {
            pstmt.setString(1, date.toString());
            pstmt.setLong(2, additionalDuration);
            pstmt.setLong(3, additionalDuration);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update daily stats: " + e.getMessage());
        }
    }

    private static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, TimeUtils.DB_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
