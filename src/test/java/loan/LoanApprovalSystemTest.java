package loan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;

public class LoanApprovalSystemTest {

    LoanApprovalSystem tester = new LoanApprovalSystem();

    @Test
    public void testMain() {
        LoanDecision d = tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000));
        assertTrue(d.isApproved());
    }

    @Test
    public void equivalencePartitioning() {
        assertTrue(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(10, 700, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(80, 700, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 500, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 700, 5, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 3000)).isApproved());
    }

    @Test
    public void boundaryValueAnalysis() {
        assertFalse(tester.evaluateLoan(new Customer(17, 700, 1, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(18, 700, 1, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(19, 700, 1, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(64, 700, 1, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(65, 700, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(66, 700, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 599, 1, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(30, 600, 1, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(30, 601, 1, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(30, 700, 2, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(30, 700, 3, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 700, 4, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1950)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 2000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 2050)).isApproved());
        assertEquals(10.0, tester.evaluateLoan(new Customer(30, 649, 1, 5000, 1000)).getInterestRate(), 0.001);
        assertEquals(7.0, tester.evaluateLoan(new Customer(30, 650, 1, 5000, 1000)).getInterestRate(), 0.001);
        assertEquals(7.0, tester.evaluateLoan(new Customer(30, 651, 1, 5000, 1000)).getInterestRate(), 0.001);
        assertEquals(7.0, tester.evaluateLoan(new Customer(30, 749, 1, 5000, 1000)).getInterestRate(), 0.001);
        assertEquals(5.0, tester.evaluateLoan(new Customer(30, 750, 1, 5000, 1000)).getInterestRate(), 0.001);
        assertEquals(5.0, tester.evaluateLoan(new Customer(30, 751, 1, 5000, 1000)).getInterestRate(), 0.001);
    }

    @Test
    public void categoryPartitioning() {
        assertFalse(tester.evaluateLoan(new Customer(17, 700, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(66, 700, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 599, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 700, 4, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 2050)).isApproved());

        LoanDecision tier3 = tester.evaluateLoan(new Customer(30, 620, 0, 5000, 1000));
        assertTrue(tier3.isApproved());
        assertEquals(10.0, tier3.getInterestRate(), 0.001);

        LoanDecision tier2 = tester.evaluateLoan(new Customer(30, 700, 0, 5000, 1000));
        assertTrue(tier2.isApproved());
        assertEquals(7.0, tier2.getInterestRate(), 0.001);

        LoanDecision tier1 = tester.evaluateLoan(new Customer(30, 800, 0, 5000, 1000));
        assertTrue(tier1.isApproved());
        assertEquals(5.0, tier1.getInterestRate(), 0.001);

        assertEquals("Age out of range [18-65]",
                tester.evaluateLoan(new Customer(17, 500, 5, 5000, 3000)).getRejectionReason());

        assertEquals("Credit score too low (min 600)",
                tester.evaluateLoan(new Customer(30, 500, 5, 5000, 1000)).getRejectionReason());

        assertEquals("Too many late payments (max 3)",
                tester.evaluateLoan(new Customer(30, 700, 5, 5000, 3000)).getRejectionReason());

        assertNull(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000)).getRejectionReason());

        LoanDecision rejected = tester.evaluateLoan(new Customer(17, 700, 1, 5000, 1000));
        assertEquals(0.0, rejected.getInterestRate(), 0.001);
        assertEquals(0.0, rejected.getMaxLoanAmount(), 0.001);
    }

    @Test
    public void statementCoverage() {
        tester.evaluateLoan(new Customer(17, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 500, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 4, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 1, 5000, 3000));
        tester.evaluateLoan(new Customer(30, 750, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 620, 1, 5000, 1000));
    }

    @Test
    public void branchCoverage() {
        tester.evaluateLoan(new Customer(17, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(66, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 599, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 4, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 1, 5000, 3000));
        tester.evaluateLoan(new Customer(30, 750, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 620, 1, 5000, 1000));
    }

    @Test
    public void conditionCoverage() {
        tester.evaluateLoan(new Customer(17, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(66, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 599, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 4, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 1, 5000, 3000));
        tester.evaluateLoan(new Customer(30, 750, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 620, 1, 5000, 1000));
    }

    @Test
    public void circuitsCoverage() {
        tester.evaluateLoan(new Customer(17, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(66, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 599, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 4, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 1, 5000, 3000));
        tester.evaluateLoan(new Customer(30, 750, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 620, 1, 5000, 1000));
    }

    @Test
    void killMutants() throws Exception {
        String text = tapSystemOut(() -> {
            assertTrue(tester.evaluateLoan(new Customer(18, 700, 1, 5000, 1000)).isApproved());
            assertFalse(tester.evaluateLoan(new Customer(17, 700, 1, 5000, 1000)).isApproved());

            assertTrue(tester.evaluateLoan(new Customer(65, 700, 1, 5000, 1000)).isApproved());
            assertFalse(tester.evaluateLoan(new Customer(66, 700, 1, 5000, 1000)).isApproved());

            assertTrue(tester.evaluateLoan(new Customer(30, 600, 1, 5000, 1000)).isApproved());
            assertFalse(tester.evaluateLoan(new Customer(30, 599, 1, 5000, 1000)).isApproved());

            assertTrue(tester.evaluateLoan(new Customer(30, 700, 3, 5000, 1000)).isApproved());
            assertFalse(tester.evaluateLoan(new Customer(30, 700, 4, 5000, 1000)).isApproved());

            assertTrue(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 2000)).isApproved());
            assertFalse(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 2001)).isApproved());

            assertEquals(5.0, tester.evaluateLoan(new Customer(30, 750, 1, 5000, 1000)).getInterestRate(), 0.001);
            assertEquals(7.0, tester.evaluateLoan(new Customer(30, 749, 1, 5000, 1000)).getInterestRate(), 0.001);

            assertEquals(7.0, tester.evaluateLoan(new Customer(30, 650, 1, 5000, 1000)).getInterestRate(), 0.001);
            assertEquals(10.0, tester.evaluateLoan(new Customer(30, 649, 1, 5000, 1000)).getInterestRate(), 0.001);

            assertEquals(5.0, tester.evaluateLoan(new Customer(30, 800, 1, 5000, 1000)).getInterestRate(), 0.001);
            assertEquals(7.0, tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000)).getInterestRate(), 0.001);
            assertEquals(10.0, tester.evaluateLoan(new Customer(30, 620, 1, 5000, 1000)).getInterestRate(), 0.001);

            assertEquals(60_000.0, tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000)).getMaxLoanAmount(), 0.01);
            assertEquals(0.0, tester.evaluateLoan(new Customer(30, 700, 1, 5000, 2000)).getMaxLoanAmount(), 0.01);

            LoanDecision rej = tester.evaluateLoan(new Customer(17, 700, 1, 5000, 1000));
            assertFalse(rej.isApproved());
            assertNotNull(rej.getRejectionReason());
            assertEquals(0.0, rej.getInterestRate(), 0.001);
            assertEquals(0.0, rej.getMaxLoanAmount(), 0.001);
        });

        assertEquals("", text);
    }
}