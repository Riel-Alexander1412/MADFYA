package com.mobile.madfya;

/**
 * Small UI helpers shared by the Admin, Alerts and Community screens:
 * relative time formatting, avatar initials/colours and alert icon/colour mapping.
 */
public final class Ui {

    private Ui() {
    }

    /** Turns a timestamp into a short relative label such as "15m ago" or "2h ago". */
    public static String timeAgo(long millis) {
        long diff = System.currentTimeMillis() - millis;
        if (diff < 0) {
            diff = 0;
        }
        long minutes = diff / 60000L;
        if (minutes < 1) {
            return "just now";
        }
        if (minutes < 60) {
            return minutes + "m ago";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "h ago";
        }
        long days = hours / 24;
        if (days < 7) {
            return days + "d ago";
        }
        return (days / 7) + "w ago";
    }

    /** Up to two uppercase initials from a name, e.g. "Jane Smith" -> "JS". */
    public static String initials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(parts[0].charAt(0)));
        if (parts.length > 1) {
            sb.append(Character.toUpperCase(parts[parts.length - 1].charAt(0)));
        }
        return sb.toString();
    }

    public static int avatarColorRes(boolean active, String id) {
        if (!active) {
            return R.color.avatar_gray;
        }
        return  R.color.avatar_blue;
    }
    public static int avatarColorRes(boolean active, int id) {
        if (!active) {
            return R.color.avatar_gray;
        }
        return  R.color.avatar_blue;
    }

    public static int alertIcon(String type) {
        if (type == null) {
            return R.drawable.ic_notifications;
        }
        switch (type) {
            case "critical":
                return R.drawable.ic_warning;
            case "report":
                return R.drawable.ic_description;
            case "maintenance":
                return R.drawable.ic_build;
            case "flow":
                return R.drawable.ic_water_drop;
            case "announce":
                return R.drawable.ic_campaign;
            case "community":
                return R.drawable.ic_groups;
            case "personal":
                return R.drawable.ic_person;
            default:
                return R.drawable.ic_notifications;
        }
    }

    public static int alertColorRes(String type) {
        if (type == null) {
            return R.color.brand_blue;
        }
        switch (type) {
            case "critical":
                return R.color.alert_critical;
            case "maintenance":
                return R.color.alert_maintenance;
            case "flow":
                return R.color.alert_flow;
            case "personal":
                return R.color.alert_personal;
            case "report":
            case "announce":
            case "community":
            default:
                return R.color.brand_blue;
        }
    }
}
