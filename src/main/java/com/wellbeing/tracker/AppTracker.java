package com.wellbeing.tracker;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.wellbeing.database.DBManager;
import com.wellbeing.model.AppUsage;
import com.wellbeing.util.AppNameUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppTracker {
    private static final int PROCESS_QUERY_INFORMATION = 0x0400;
    private static final int PROCESS_VM_READ = 0x0010;

    private ScheduledExecutorService executorService;
    private volatile String currentAppName = "";
    private volatile LocalDateTime currentAppStartTime;

    public void startTracking() {
        executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "AppTracker-Thread");
            thread.setDaemon(true);
            return thread;
        });

        currentAppStartTime = LocalDateTime.now();
        executorService.scheduleAtFixedRate(this::checkActiveWindow, 0, 1, TimeUnit.SECONDS);
    }

    public void stopTracking() {
        if (executorService != null) {
            executorService.shutdown();
        }
        if (!currentAppName.isEmpty() && currentAppStartTime != null) {
            saveCurrentAppUsage();
        }
    }

    public synchronized String getCurrentAppName() {
        return currentAppName == null || currentAppName.isBlank() ? "Monitoring idle" : currentAppName;
    }

    public synchronized long getCurrentSessionDurationSeconds() {
        if (currentAppStartTime == null || currentAppName == null || currentAppName.isBlank()) {
            return 0;
        }
        return Math.max(0, ChronoUnit.SECONDS.between(currentAppStartTime, LocalDateTime.now()));
    }

    public synchronized LocalDateTime getCurrentAppStartTime() {
        return currentAppStartTime;
    }

    private void checkActiveWindow() {
        try {
            String activeAppName = getActiveProcessName();
            if (activeAppName == null || activeAppName.isEmpty()) {
                activeAppName = "Unknown";
            }

            if (!activeAppName.equals(currentAppName)) {
                if (!currentAppName.isEmpty()) {
                    saveCurrentAppUsage();
                }
                synchronized (this) {
                    currentAppName = activeAppName;
                    currentAppStartTime = LocalDateTime.now();
                }
            }
        } catch (Exception e) {
            System.err.println("Error tracking window: " + e.getMessage());
        }
    }

    private synchronized void saveCurrentAppUsage() {
        LocalDateTime now = LocalDateTime.now();
        long duration = ChronoUnit.SECONDS.between(currentAppStartTime, now);
        if (duration > 0) {
            AppUsage usage = new AppUsage(currentAppName, currentAppStartTime, now, duration);
            DBManager.saveAppUsage(usage);
        }
    }

    private String getActiveProcessName() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        if (hwnd == null) {
            return null;
        }

        IntByReference pid = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);

        HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(
                PROCESS_QUERY_INFORMATION | PROCESS_VM_READ,
                false,
                pid.getValue()
        );

        if (processHandle == null) {
            return getWindowText(hwnd);
        }

        String fullPath = null;
        try {
            fullPath = Kernel32Util.QueryFullProcessImageName(processHandle, 0);
        } catch (Exception ignored) {
        }
        Kernel32.INSTANCE.CloseHandle(processHandle);

        if (fullPath == null || fullPath.isEmpty()) {
            return getWindowText(hwnd);
        }

        String name = new File(fullPath).getName();
        if (name == null || name.isEmpty()) {
            return getWindowText(hwnd);
        }
        return AppNameUtils.normalizeAppName(name);
    }

    private String getWindowText(HWND hwnd) {
        char[] windowText = new char[512];
        User32.INSTANCE.GetWindowText(hwnd, windowText, 512);
        return Native.toString(windowText);
    }
}
