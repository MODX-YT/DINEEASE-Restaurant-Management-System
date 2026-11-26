package dineease;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DINEEASEIntegrationTest {
    private static int passed;
    private static int failed;

    // Simulates interactive console input without writing production records.
    private static String runApp(String inputs) throws Exception {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        String previousTestMode = System.getProperty("dineease.testMode");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(output, true, "UTF-8");
        try {
            System.setProperty("dineease.testMode", "true");
            System.setIn(new ByteArrayInputStream(inputs.getBytes("UTF-8")));
            System.setOut(capture);
            Main.main(new String[0]);
            capture.flush();
            return output.toString("UTF-8");
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
            if (previousTestMode == null) System.clearProperty("dineease.testMode");
            else System.setProperty("dineease.testMode", previousTestMode);
            capture.close();
        }
    }

    // Checks a displayed result from the running application.
    private static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("[PASS] " + name); }
        else { failed++; System.out.println("[FAIL] " + name); }
    }

    // Tests cash checkout of a takeout order.
    private static void testTakeout() throws Exception {
        String input = "3\nJuan\n2\n1\n101\n2\n1\n301\n1\n6\n400\n500\n4\n";
        String result = runApp(input);
        check("Takeout order created", result.contains("Order draft created"));
        check("Insufficient payment rejected", result.contains("Insufficient payment"));
        check("Correct change: PHP 85.00", result.contains("CHANGE   : PHP 85.00"));
        check("Receipt printed", result.contains("OFFICIAL RECEIPT"));
        check("Checkout completed", result.contains("Checkout completed successfully"));
    }

    // Tests occupied-table updates after dine-in checkout.
    private static void testDineIn() throws Exception {
        String input = "3\nMaria\n1\n3\n1\n101\n1\n6\n200\n1\n5\n1\n0\n0\n4\n";
        String result = runApp(input);
        check("Dine-In billing selected", result.contains("Billing Type : DINE-IN"));
        check("Dine-In checkout completed", result.contains("Checkout completed successfully"));
        check("Table 3 becomes occupied", result.contains("Table 3 is now OCCUPIED"));
        check("Occupied table count updated", result.contains("Occupied Tables : 1"));
    }

    // Tests denial of an unauthorized customer's booked table.
    private static void testReservation() throws Exception {
        String date = LocalDateTime.now().plusMinutes(30)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String input = "1\n2\nJuan\n1\n2\n" + date + "\n0\n3\nMaria\n1\n1\n1\n4\n";
        String result = runApp(input);
        check("Reservation created", result.contains("RESERVATION SUCCESSFUL"));
        check("Reservation ID requested", result.contains("Enter Reservation ID"));
        check("Unauthorized customer rejected", result.contains("Table reservation validation failed"));
        check("Unauthorized order not created", !result.contains("Order draft created"));
    }

    // Executes the automated console flows.
    public static void main(String[] args) throws Exception {
        System.out.println("========================================");
        System.out.println("   DINEEASE V1.6 INTEGRATION TESTS");
        System.out.println("========================================");
        testTakeout();
        testDineIn();
        testReservation();
        System.out.println("\n========================================");
        System.out.println("PASSED : " + passed);
        System.out.println("FAILED : " + failed);
        System.out.println("========================================");
        if (failed > 0) throw new AssertionError(failed + " integration tests failed.");
        System.out.println("ALL INTEGRATION TESTS PASSED!");
    }
}
