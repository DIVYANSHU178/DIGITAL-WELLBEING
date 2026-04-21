package com.wellbeing.util;

public final class AppNameUtils {
    private AppNameUtils() {
        // utility class
    }

    public static String normalizeAppName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return "Unknown";
        }

        String name = rawName.trim();
        if (name.toLowerCase().endsWith(".exe")) {
            name = name.substring(0, name.length() - 4);
        }

        String lower = name.toLowerCase();
        return switch (lower) {
            case "explorer" -> "Windows Explorer";
            case "taskmgr", "task manager" -> "Task Manager";
            case "code" -> "Visual Studio Code";
            case "chrome" -> "Google Chrome";
            case "msedge" -> "Microsoft Edge";
            case "teams" -> "Microsoft Teams";
            case "notepad" -> "Notepad";
            case "cmd", "cmd.exe" -> "Command Prompt";
            case "powershell", "pwsh" -> "PowerShell";
            default -> capitalizeAppName(name);
        };
    }

    private static String capitalizeAppName(String name) {
        String[] parts = name.split("[\\s\\-_]+");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase());
            }
            if (i < parts.length - 1) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }
}
