package com.mobile.madfya.data;

/**
 * Seeds the database with the demo content shown in the Figma prototype so the
 * three screens look populated on first launch.
 */
final class Seed {

    private static final long MINUTE = 60L * 1000L;
    private static final long HOUR = 60L * MINUTE;
    private static final long DAY = 24L * HOUR;

    private Seed() {
    }

    static void populate(AppDatabase db) {
        long now = System.currentTimeMillis();

        // ----- Users (Admin page) -----
        UserDao users = db.userDao();
        users.insert(new User("Jane_Smith", "Admin", true, now));
        users.insert(new User("Mike Ross", "Maintenance", true, now));
        users.insert(new User("David_Lee", "Resident", false, now));
        users.insert(new User("Sarah Jenkins", "Maintenance", true, now));
        users.insert(new User("Something", "Resident", true, now));

        // ----- Alerts / activity feed (Alerts page) -----
        AlertDao alerts = db.alertDao();
        alerts.insert(new Alert("Critical: Pressure drop in Sector 4", null,
                "System", "critical", now - 15 * MINUTE, false));
        alerts.insert(new Alert("New Water Quality Report available", null,
                "Community", "report", now - 2 * HOUR, false));
        alerts.insert(new Alert("Maintenance request #402 scheduled for tomorrow", null,
                "Personal", "maintenance", now - 4 * HOUR, false));
        alerts.insert(new Alert("Flow rate anomaly detected", "Minor fluctuations in supply line 7",
                "System", "flow", now - DAY, false));
        alerts.insert(new Alert("Scheduled Maintenance: Central Hub", null,
                "System", "announce", now - 2 * DAY, false));

        // ----- Community notices (Community page) -----
        CommunityNoticeDao notices = db.noticeDao();
        notices.insert(new CommunityNotice(
                "System Maintenance Scheduled",
                "Tomorrow from 10 AM to 2 PM. Water may be temporarily unavailable.",
                "Alert", "Admin", true, now - 30 * MINUTE,
                null, 0, 0, 0, true, true));

        long waterReportId = notices.insert(new CommunityNotice(
                "Water Quality Report - June",
                "The water turbidity and pH levels are well within safe limits. "
                        + "System is running at 98% efficiency.",
                "General", "Admin", true, now - 2 * HOUR,
                "Kampung A", 1.2, 24, 5, false, false));

        long meetingId = notices.insert(new CommunityNotice(
                "Community Meeting",
                "Join us at the station this Saturday to discuss upcoming water supply "
                        + "improvements and address community concerns.",
                "Event", "Village Head", false, now - 5 * HOUR,
                "Community Center", 0.5, 12, 3, false, false));

        // ----- Comments (kept in sync with the counts above) -----
        CommentDao comments = db.commentDao();
        comments.insert(new Comment((int) waterReportId, "Mike Ross", "Great to see the efficiency this high.", now - 100 * MINUTE));
        comments.insert(new Comment((int) waterReportId, "Sarah Jenkins", "Thanks for the transparency.", now - 90 * MINUTE));
        comments.insert(new Comment((int) waterReportId, "David Lee", "How often is this measured?", now - 80 * MINUTE));
        comments.insert(new Comment((int) waterReportId, "Admin", "Readings are taken every 6 hours.", now - 70 * MINUTE));
        comments.insert(new Comment((int) waterReportId, "Jane Smith", "Noted, thank you.", now - 60 * MINUTE));

        comments.insert(new Comment((int) meetingId, "Mike Ross", "I'll be there.", now - 4 * HOUR));
        comments.insert(new Comment((int) meetingId, "Sarah Jenkins", "Can we join online?", now - 3 * HOUR));
        comments.insert(new Comment((int) meetingId, "Village Head", "A link will be shared on Friday.", now - 2 * HOUR));
    }
}
