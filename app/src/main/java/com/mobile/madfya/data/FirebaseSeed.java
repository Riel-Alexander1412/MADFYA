package com.mobile.madfya.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseSeed {

    public static void populate() {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();

        // Only seed if users node is empty
        root.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) return; // already seeded

                seedUsers(root);
                seedSensors(root);
                seedAlerts(root);
                seedReports(root);
                seedCommunityNotices(root);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // =========================================================================
    // USERS
    // =========================================================================

    private static void seedUsers(DatabaseReference root) {
        DatabaseReference usersRef = root.child("users");

        User admin = new User("Admin", "Admin", true, System.currentTimeMillis());
        admin.password = "admin123";
        usersRef.push().setValue(admin);

        User maintenance = new User("Ali Hassan", "Maintenance", true, System.currentTimeMillis());
        maintenance.password = "maintenance123";
        usersRef.push().setValue(maintenance);

        User resident1 = new User("Siti Aminah", "Resident", true, System.currentTimeMillis());
        resident1.password = "resident123";
        usersRef.push().setValue(resident1);

        User resident2 = new User("Kumar Raj", "Resident", true, System.currentTimeMillis());
        resident2.password = "resident123";
        usersRef.push().setValue(resident2);
    }

    // =========================================================================
    // SENSORS  — 4 filters × ~5 readings each (spaced 10 min apart)
    // filterId 1 = Main Filter, 2 = Sub-Filter A, 3 = Sub-Filter B, 4 = Sub-Filter C
    // =========================================================================

    private static void seedSensors(DatabaseReference root) {
        DatabaseReference ref = root.child("sensors");
        long now = System.currentTimeMillis();
        long min = 60_000L;

        // ── Filter 1 — Main Filter (healthy trend) ────────────────────────────
        String[] f1Ph    = {"7.0", "7.1", "7.2", "7.3", "7.2", "7.4", "7.1", "7.0", "7.3", "7.2", "7.5", "7.6", "7.4", "7.3", "7.5"};
        String[] f1Turb  = {"1.2", "1.4", "1.5", "1.6", "1.8", "2.0", "1.9", "1.7", "1.5", "1.3", "1.4", "1.6", "1.8", "2.0", "1.7"};
        String[] f1Temp  = {"26.0", "26.5", "27.0", "27.5", "28.0", "28.5", "28.0", "27.5", "27.0", "26.5", "26.0", "26.8", "27.2", "27.8", "28.2"};
        String[] f1Flow  = {"110", "112", "108", "115", "105", "100", "98",  "102", "107", "113", "109", "111", "106", "103", "116"};
        int[]    f1Hp    = {96, 95, 95, 94, 94, 93, 93, 92, 92, 91, 91, 90, 90, 89, 89};

        for (int i = 0; i < f1Ph.length; i++) {
            Sensors s = new Sensors(now - ((f1Ph.length - 1 - i) * 10 * min),
                    now - 604_800_000L, "admin_key", 3.7812, 103.3261);
            s.filterId       = 1;
            s.ph_level       = f1Ph[i];
            s.turbidity      = f1Turb[i];
            s.temperature    = f1Temp[i];
            s.water_flow_rate = f1Flow[i];
            s.status         = "OPERATIONAL";
            s.HP             = f1Hp[i];
            ref.push().setValue(s);
        }

        // ── Filter 2 — Sub-Filter A (stable) ─────────────────────────────────
        String[] f2Ph    = {"6.9", "7.0", "7.1", "7.0", "6.9", "7.0", "7.1", "7.2", "7.1", "7.0", "6.9", "7.0"};
        String[] f2Turb  = {"2.0", "2.1", "2.3", "2.2", "2.0", "1.9", "2.1", "2.2", "2.0", "1.8", "2.0", "2.1"};
        String[] f2Temp  = {"27.0", "27.2", "27.5", "27.8", "27.6", "27.4", "27.1", "27.0", "27.3", "27.5", "27.2", "27.0"};
        String[] f2Flow  = {"80", "82", "79", "83", "81", "80", "78", "82", "84", "80", "79", "81"};
        int[]    f2Hp    = {88, 87, 87, 86, 86, 85, 85, 84, 84, 83, 83, 82};

        for (int i = 0; i < f2Ph.length; i++) {
            Sensors s = new Sensors(now - ((f2Ph.length - 1 - i) * 10 * min),
                    now - 1_296_000_000L, "admin_key", 3.7854, 103.3312);
            s.filterId        = 2;
            s.ph_level        = f2Ph[i];
            s.turbidity       = f2Turb[i];
            s.temperature     = f2Temp[i];
            s.water_flow_rate = f2Flow[i];
            s.status          = "OPERATIONAL";
            s.HP              = f2Hp[i];
            ref.push().setValue(s);
        }

        // ── Filter 3 — Sub-Filter B (degrading, WARNING) ──────────────────────
        String[] f3Ph    = {"7.4", "7.2", "7.0", "6.8", "6.6", "6.4", "6.2", "6.0", "5.9", "5.8", "5.7", "5.6"};
        String[] f3Turb  = {"3.0", "3.5", "4.0", "4.5", "5.0", "5.5", "6.0", "6.5", "7.0", "7.5", "8.0", "8.7"};
        String[] f3Temp  = {"29.0", "29.5", "30.0", "30.5", "31.0", "31.5", "32.0", "32.5", "33.0", "33.5", "34.0", "34.5"};
        String[] f3Flow  = {"70", "65", "60", "55", "50", "45", "40", "38", "35", "32", "28", "25"};
        int[]    f3Hp    = {70, 65, 60, 55, 50, 47, 44, 40, 37, 34, 30, 27};

        for (int i = 0; i < f3Ph.length; i++) {
            Sensors s = new Sensors(now - ((f3Ph.length - 1 - i) * 10 * min),
                    now - 2_592_000_000L, "admin_key", 3.7745, 103.3089);
            s.filterId        = 3;
            s.ph_level        = f3Ph[i];
            s.turbidity       = f3Turb[i];
            s.temperature     = f3Temp[i];
            s.water_flow_rate = f3Flow[i];
            s.status          = i < 4 ? "OPERATIONAL" : "WARNING";
            s.HP              = f3Hp[i];
            ref.push().setValue(s);
        }

        // ── Filter 4 — Sub-Filter C (offline / disabled) ──────────────────────
        String[] f4Ph    = {"7.0", "7.1", "7.0", null, null, null, null, null, null, null};
        String[] f4Turb  = {"1.8", "1.9", "2.0", null, null, null, null, null, null, null};
        String[] f4Temp  = {"26.0", "26.2", "26.5", null, null, null, null, null, null, null};
        String[] f4Flow  = {"95", "90", "85", null, null, null, null, null, null, null};
        int[]    f4Hp    = {50, 40, 30, 25, 22, 20, 18, 15, 12, 10};

        for (int i = 0; i < f4Ph.length; i++) {
            Sensors s = new Sensors(now - ((f4Ph.length - 1 - i) * 10 * min),
                    now - 5_184_000_000L, "admin_key", 3.7698, 103.3205);
            s.filterId        = 4;
            s.ph_level        = f4Ph[i];
            s.turbidity       = f4Turb[i];
            s.temperature     = f4Temp[i];
            s.water_flow_rate = f4Flow[i];
            s.status          = i < 3 ? "OPERATIONAL" : "DISABLED";
            s.HP              = f4Hp[i];
            ref.push().setValue(s);
        }
    }

    // =========================================================================
    // ALERTS  — 15 entries across all categories and types
    // =========================================================================

    private static void seedAlerts(DatabaseReference root) {
        DatabaseReference ref = root.child("alerts");
        long now = System.currentTimeMillis();
        long hr  = 3_600_000L;
        long day = 86_400_000L;

        Object[][] alerts = {
                // title, message, category, type, offset, read
                {"System Maintenance Scheduled",   "Tomorrow 10 AM–2 PM. Water may be temporarily unavailable.",          "System",    "critical",    0,          false},
                {"Water Quality Notice",            "Turbidity elevated in Zone B. Boil-water advisory in effect.",        "Community", "community",   hr,         false},
                {"Filter B Inspection Required",   "Sub-Filter B has dropped below 30% health. Schedule maintenance.",    "System",    "maintenance", 2 * hr,     false},
                {"Flow Rate Drop Detected",         "Main filter flow rate fell below 50 L/min at 14:32.",                 "System",    "flow",        3 * hr,     false},
                {"pH Level Warning — Zone C",       "pH reading of 5.6 detected. Outside safe range (6.5–8.5).",          "System",    "critical",    5 * hr,     true },
                {"Community Clean-Up Event",        "Join us Saturday 8 AM at Taman Pekan for a river clean-up.",         "Community", "community",   day,        false},
                {"Pipe Leak Reported — Jln Utama", "Resident report of visible water leak near Jln Utama roundabout.",   "Community", "report",      day + hr,   false},
                {"Scheduled Filter Replacement",   "Filter 4 (Sub-Filter C) is due for replacement this week.",           "System",    "maintenance", 2 * day,    true },
                {"Water Restored — Zone A",         "Supply has been fully restored following yesterday's maintenance.",   "System",    "announce",    2 * day + hr, true},
                {"High Temperature Alert",          "Sensor 3 recorded 34.5°C. Possible heat stress on filter media.",    "System",    "critical",    3 * day,    false},
                {"Personal Reminder: Bill Due",     "Your water utility bill is due in 3 days.",                          "Personal",  "personal",    3 * day + hr, false},
                {"Turbidity Spike — Main Filter",  "Turbidity reached 8.7 NTU at 09:15. Investigating cause.",           "System",    "critical",    4 * day,    false},
                {"Community Notice: Water Saving",  "Please reduce non-essential usage during the dry season.",           "Community", "announce",    4 * day + hr, true},
                {"Maintenance Complete — Filter A", "Sub-Filter A serviced and returned to full operation.",              "System",    "maintenance", 5 * day,    true },
                {"Emergency Shutoff — Zone B",      "Unplanned shutoff due to burst main. Estimated 4 hr downtime.",      "System",    "critical",    6 * day,    false},
        };

        for (Object[] a : alerts) {
            Alert alert = new Alert(
                    (String)  a[0],
                    (String)  a[1],
                    (String)  a[2],
                    (String)  a[3],
                    now - ((Number) a[4]).longValue(),
                    (boolean) a[5]
            );
            ref.push().setValue(alert);
        }
    }

    // =========================================================================
    // REPORTS  — 15 entries across all three categories
    // =========================================================================

    private static void seedReports(DatabaseReference root) {
        DatabaseReference ref = root.child("reports");
        long now = System.currentTimeMillis();
        long hr  = 3_600_000L;
        long day = 86_400_000L;

        Object[][] reports = {
                // title, description, category, reportedBy, lat, lng, imagePath, offset
                {"Burst Pipe Near School",          "Large water leak spotted outside SK Pekan. Road partially flooded.",          "Damaged Pipes",    "Kumar Raj",    3.7790, 103.3250, null,   0          },
                {"Discoloured Water in Tap",         "Brown water coming from kitchen tap since morning. Possible rust.",           "Unusual Behavior", "Siti Aminah",  3.7812, 103.3261, null,   hr         },
                {"Low Water Pressure",               "Water pressure very low since yesterday. Hard to shower.",                    "Unusual Behavior", "Kumar Raj",    3.7834, 103.3290, null,   2 * hr     },
                {"Exposed Pipe on Jln Mawar",        "Old pipe sticking out of ground near playground. Safety hazard.",            "Damaged Pipes",    "Siti Aminah",  3.7760, 103.3180, null,   4 * hr     },
                {"Sewage Smell from Drain",          "Strong sewage odour near community hall. Possible cross-contamination.",      "Unusual Behavior", "Kumar Raj",    3.7800, 103.3300, null,   day        },
                {"Cracked Water Main",               "Visible crack on main supply pipe behind block C. Water pooling.",           "Damaged Pipes",    "Siti Aminah",  3.7745, 103.3089, null,   day + hr   },
                {"No Water Supply for 6 Hours",      "Entire row of houses with no water. Children unable to go to school.",       "Miscellaneous",    "Kumar Raj",    3.7820, 103.3270, null,   2 * day    },
                {"Meter Box Flooded",                "Water meter box is submerged. Cannot read meter and risk of damage.",        "Damaged Pipes",    "Siti Aminah",  3.7698, 103.3205, null,   2 * day + hr},
                {"Unusual Noise from Pipes",         "Loud banging noise from pipes every night around 11 PM.",                    "Unusual Behavior", "Kumar Raj",    3.7855, 103.3310, null,   3 * day    },
                {"Oil Sheen on Water Surface",       "Oily film visible on water in the drain near the old factory.",              "Unusual Behavior", "Siti Aminah",  3.7710, 103.3150, null,   3 * day + hr},
                {"Leaking Valve at Pump Station",   "Maintenance staff noticed slow drip from pressure relief valve.",             "Damaged Pipes",    "Ali Hassan",   3.7812, 103.3261, null,   4 * day    },
                {"Fallen Tree Damaged Water Line",  "Storm last night knocked tree onto supply line. Water lost.",                 "Damaged Pipes",    "Ali Hassan",   3.7770, 103.3200, null,   4 * day + hr},
                {"Chlorine Taste Complaint",         "Multiple residents complained water tastes strongly of chlorine.",           "Miscellaneous",    "Siti Aminah",  3.7830, 103.3285, null,   5 * day    },
                {"Overflow at Reservoir",            "Water overflowing from secondary reservoir. Risk of contamination.",         "Unusual Behavior", "Ali Hassan",   3.7748, 103.3092, null,   5 * day + hr},
                {"Vandalism to Fire Hydrant",        "Fire hydrant on Jln Bendahara broken open. Water wasted continuously.",      "Miscellaneous",    "Kumar Raj",    3.7865, 103.3320, null,   6 * day    },
        };

        for (Object[] r : reports) {
            Reports report = new Reports(
                    (String)  r[0],
                    (String)  r[1],
                    (String)  r[2],
                    (String)  r[3],
                    (double)  r[4],
                    (double)  r[5],
                    (String)  r[6]
            );
            report.ReportedTimeStamps = now - ((Number) r[7]).longValue();
            ref.push().setValue(report);
        }
    }

    // =========================================================================
    // COMMUNITY NOTICES + COMMENTS  — 12 notices, ~2 comments each
    // =========================================================================

    private static void seedCommunityNotices(DatabaseReference root) {
        DatabaseReference noticesRef  = root.child("notices");
        DatabaseReference commentsRef = root.child("comments");
        long now = System.currentTimeMillis();
        long hr  = 3_600_000L;
        long day = 86_400_000L;

        Object[][] notices = {
                // title, body, tag, author, isAdmin, offset, location, distKm, likes, comments, pinned, important
                {"Boil-Water Advisory — Zone B",
                        "Due to elevated turbidity readings, all residents in Zone B should boil water before consumption until further notice.",
                        "Alert", "Admin", true, 0, "Zone B", 0.3, 24, 3, true, true},

                {"Scheduled Maintenance This Saturday",
                        "Water supply will be interrupted from 8 AM to 12 PM this Saturday for routine filter maintenance. Please store water in advance.",
                        "Maintenance", "Admin", true, hr, "Pekan Town", 0.0, 18, 2, false, true},

                {"River Clean-Up Drive",
                        "Join us this Sunday at Taman Pekan for a community river clean-up! Bring gloves and wear comfortable clothes. Free breakfast for volunteers.",
                        "Event", "Siti Aminah", false, 2 * hr, "Taman Pekan", 1.2, 35, 4, false, false},

                {"Reminder: Report Leaks Promptly",
                        "Please report any visible pipe leaks or unusual water discolouration using the Reports feature in the app. Early reports help prevent bigger problems.",
                        "General", "Admin", true, day, "All Areas", 0.0, 12, 2, false, false},

                {"Water Conservation Tips for Dry Season",
                        "We are entering the dry season. Please reduce non-essential water use. Fix dripping taps, take shorter showers, and avoid watering gardens at peak hours.",
                        "General", "Admin", true, day + hr, "All Areas", 0.0, 20, 2, false, false},

                {"Pipe Replacement — Jln Bendahara",
                        "Old cast-iron pipes on Jln Bendahara will be replaced next week. Temporary lane closures expected. Water supply will be maintained via bypass.",
                        "Maintenance", "Ali Hassan", false, 2 * day, "Jln Bendahara", 0.8, 9, 2, false, false},

                {"New Water Quality Dashboard",
                        "The Madfya app now shows real-time water quality data including pH, turbidity, temperature and flow rate. Check the Status screen for live readings!",
                        "General", "Admin", true, 2 * day + hr, "All Areas", 0.0, 41, 2, false, false},

                {"Eid Gathering at Community Hall",
                        "The residents association is organising a Hari Raya gathering at the community hall. All residents are welcome. Potluck — please bring a dish!",
                        "Event", "Siti Aminah", false, 3 * day, "Community Hall", 0.5, 55, 3, false, false},

                {"Sub-Filter C Offline",
                        "Sub-Filter C is currently offline for emergency repairs. Zones served by this filter are running on reduced capacity. We apologise for the inconvenience.",
                        "Alert", "Admin", true, 3 * day + hr, "Zone C", 1.4, 30, 2, true, true},

                {"Water Bill Assistance Programme",
                        "Low-income households may apply for bill assistance at the main office. Bring your latest bill and MyKad. Applications close end of month.",
                        "General", "Admin", true, 4 * day, "Main Office", 0.6, 14, 2, false, false},

                {"Gotong-Royong — Drain Clearing",
                        "Residents of Taman Mawar are invited to join this Saturday's gotong-royong to clear blocked drains before the monsoon season. Meet at 7:30 AM.",
                        "Event", "Kumar Raj", false, 4 * day + hr, "Taman Mawar", 2.1, 22, 2, false, false},

                {"Water Restored — All Zones",
                        "Water supply has been fully restored across all zones following last night's emergency repair. Thank you for your patience.",
                        "General", "Admin", true, 5 * day, "All Areas", 0.0, 38, 2, false, false},
        };

        // Comments per notice — two per notice
        String[][][] commentSets = {
                {{"Kumar Raj", "Thank you for the heads up. How long will the advisory last?"},
                        {"Ali Hassan", "We expect to lift it within 48 hours once readings return to normal."}},
                {{"Siti Aminah", "What time exactly will the water come back on?"},
                        {"Kumar Raj", "Hope it finishes before noon, I need water for cooking."}},
                {{"Siti Aminah", "I'll be there! Can I bring my kids?"},
                        {"Kumar Raj", "Count me in. Great initiative."},
                        {"Admin", "Kids are very welcome — we have activities for them too!"},
                        {"Ali Hassan", "Will bring extra trash bags."}},
                {{"Kumar Raj", "Good reminder. I reported a leak last week — any update?"},
                        {"Admin", "Your report has been assigned to the maintenance team."}},
                {{"Siti Aminah", "We installed a low-flow showerhead last month, highly recommend."},
                        {"Kumar Raj", "Good tips. Any chance of rainwater harvesting support?"}},
                {{"Siti Aminah", "Will the work affect Jln Mawar too?"},
                        {"Ali Hassan", "No, only Jln Bendahara from block 12 to 18."}},
                {{"Siti Aminah", "This is really useful! The ph graph is great."},
                        {"Kumar Raj", "Love the new feature. Makes it easy to spot problems."}},
                {{"Siti Aminah", "I'll bring kuih! What time does it start?"},
                        {"Admin", "Starts at 6 PM. See you there!"},
                        {"Kumar Raj", "Looking forward to it."}},
                {{"Siti Aminah", "How long until it's back online?"},
                        {"Ali Hassan", "Targeting 72 hours. Parts have been ordered."}},
                {{"Kumar Raj", "What documents are needed exactly?"},
                        {"Admin", "Latest water bill, MyKad, and income proof if available."}},
                {{"Siti Aminah", "I'll join. Should we bring our own tools?"},
                        {"Kumar Raj", "I have extra rakes if anyone needs one."}},
                {{"Siti Aminah", "Great news! Was worried this morning."},
                        {"Kumar Raj", "Thanks to the team for working through the night!"}},
        };

        for (int i = 0; i < notices.length; i++) {
            Object[] n = notices[i];
            DatabaseReference noticeRef = noticesRef.push();
            String noticeKey = noticeRef.getKey();

            CommunityNotice notice = new CommunityNotice(
                    (String)  n[0],
                    (String)  n[1],
                    (String)  n[2],
                    (String)  n[3],
                    (boolean) n[4],
                    now - ((Number) n[5]).longValue(),
                    (String)  n[6],
                    (double)  n[7],
                    (int)     n[8],
                    (int)     n[9],
                    (boolean) n[10],
                    (boolean) n[11]
            );
            noticeRef.setValue(notice);

            // Seed comments for this notice
            String[][] noticeCmts = commentSets[i];
            for (int j = 0; j < noticeCmts.length; j++) {
                Comment comment = new Comment(
                        noticeKey,
                        noticeCmts[j][0],
                        noticeCmts[j][1],
                        now - ((Number) n[5]).longValue() + ((j + 1) * 10 * 60_000L)
                );
                commentsRef.push().setValue(comment);
            }
        }
    }
}
