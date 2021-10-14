package com.rainchat.sellbox.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class ServerLog {

    public static String prefix;
    public static Plugin plugin;

    public ServerLog(Plugin pluginS) {
        plugin = pluginS;
        prefix = "[" + pluginS.getName() + "] ";
    }

    public static void log(Level level, String text) {
        plugin.getLogger().log(level, prefix + text);
    }

    public static void info(String text) {
        Bukkit.getLogger().info(prefix + " " + text);
    }

    public static void warning(String text) {
        Bukkit.getLogger().warning(prefix + " " + text);
    }

    public static void error(String text) {
        Bukkit.getLogger().severe(prefix + " " + text);
    }

    public static void exception(StackTraceElement[] stackTraceElement, String text) {
        info("(!) " + prefix + " has being encountered an error, pasting below for support (!)");
        for (StackTraceElement traceElement : stackTraceElement) {
            error(traceElement.toString());
        }
        info("Message: " + text);
        info(prefix + " version: " + plugin.getDescription().getVersion());
        info("Please report this error to me on spigot");
        info("(!) " + prefix + " (!)");
    }
}
