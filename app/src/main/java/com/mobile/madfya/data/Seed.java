package com.mobile.madfya.data;
import java.util.concurrent.TimeUnit;

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

        SensorsDao sensors = db.sensorsDao();
        long timenow        = System.currentTimeMillis();
        long oneWeekAgo = now - TimeUnit.DAYS.toMillis(7);
        long oneMonthAgo= now - TimeUnit.DAYS.toMillis(30);
        long twoMonthsAgo = now - TimeUnit.DAYS.toMillis(60);

        // ── Sensor 1 — Fully operational, recently maintained ────────────────
        Sensors s1 = new Sensors(timenow, oneWeekAgo, 1, 3.7812, 103.3261);
        s1.ph_level       = "7.2";
        s1.turbidity      = "1.8";
        s1.temperature    = "28.5";
        s1.water_flow_rate= "12.4";
        s1.status         = "OPERATIONAL";
        s1.HP             = 96;

        // ── Sensor 2 — Operational but due for maintenance soon ──────────────
        Sensors s2 = new Sensors(now, oneMonthAgo, 1, 3.7854, 103.3312);
        s2.ph_level       = "7.0";
        s2.turbidity      = "2.1";
        s2.temperature    = "29.1";
        s2.water_flow_rate= "11.8";
        s2.status         = "OPERATIONAL";
        s2.HP             = 78;

        // ── Sensor 3 — Warning state, high turbidity ─────────────────────────
        Sensors s3 = new Sensors(now, oneMonthAgo, 2, 3.7798, 103.3198);
        s3.ph_level       = "6.5";
        s3.turbidity      = "8.7";   // high — should trigger warning
        s3.temperature    = "31.2";
        s3.water_flow_rate= "9.3";
        s3.status         = "WARNING";
        s3.HP             = 54;

        // ── Sensor 4 — Warning state, low pH ─────────────────────────────────
        Sensors s4 = new Sensors(now, twoMonthsAgo, 1, 3.7901, 103.3355);
        s4.ph_level       = "5.8";   // low pH — acidic, should trigger warning
        s4.turbidity      = "3.4";
        s4.temperature    = "27.8";
        s4.water_flow_rate= "10.1";
        s4.status         = "WARNING";
        s4.HP             = 61;

        // ── Sensor 5 — Disabled / offline ────────────────────────────────────
        Sensors s5 = new Sensors(
                now - TimeUnit.DAYS.toMillis(3), // last seen 3 days ago
                twoMonthsAgo,
                2,
                3.7745, 103.3089);
        s5.ph_level       = null;    // no readings — sensor is offline
        s5.turbidity      = null;
        s5.temperature    = null;
        s5.water_flow_rate= null;
        s5.status         = "DISABLED";
        s5.HP             = 22;

        // ── Sensor 6 — Healthy, different zone ───────────────────────────────
        Sensors s6 = new Sensors(now, oneWeekAgo, 1, 3.7923, 103.3410);
        s6.ph_level       = "7.4";
        s6.turbidity      = "1.2";
        s6.temperature    = "28.0";
        s6.water_flow_rate= "13.7";
        s6.status         = "OPERATIONAL";
        s6.HP             = 91;

        sensors.insert(s1);
        sensors.insert(s2);
        sensors.insert(s3);
        sensors.insert(s4);
        sensors.insert(s5);
        sensors.insert(s6);
    }
}
