package loan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;

public class LoanApprovalSystemTest {

    LoanApprovalSystem tester = new LoanApprovalSystem();

    // verifica calea de baza - client valid (age=30, score=700, DTI=0.2) trebuie aprobat cu rata de 7%
    @Test
    public void testMain() {
        LoanDecision d = tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000));
        assertTrue(d.isApproved());
    }

    // un input reprezentativ per clasa de echivalenta (EC1-EC11); acopera toate partitiile de varsta, credit, plati si DTI
    @Test
    public void equivalencePartitioning() {
        assertFalse(tester.evaluateLoan(new Customer(10, 700, 1, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(80, 700, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 500, 1, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(30, 620, 1, 5000, 1000)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(30, 800, 1, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 700, 5, 5000, 1000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 3000)).isApproved());
    }

    // valorile exact la frontiera si imediat adiacente pentru age (17/18/19, 64/65/66),
    // creditScore (599/600/601, 649/650/651, 749/750/751), latePayments (2/3/4) si DTI (1999/2000/2001)
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
        assertTrue(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1999)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 2000)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 700, 1, 5000, 2001)).isApproved());
        assertEquals(10.0, tester.evaluateLoan(new Customer(30, 649, 1, 5000, 1000)).getInterestRate(), 0.001);
        assertEquals(7.0, tester.evaluateLoan(new Customer(30, 650, 1, 5000, 1000)).getInterestRate(), 0.001);
        assertEquals(7.0, tester.evaluateLoan(new Customer(30, 651, 1, 5000, 1000)).getInterestRate(), 0.001);
        assertEquals(7.0, tester.evaluateLoan(new Customer(30, 749, 1, 5000, 1000)).getInterestRate(), 0.001);
        assertEquals(5.0, tester.evaluateLoan(new Customer(30, 750, 1, 5000, 1000)).getInterestRate(), 0.001);
        assertEquals(5.0, tester.evaluateLoan(new Customer(30, 751, 1, 5000, 1000)).getInterestRate(), 0.001);
    }

    // combinatii structurate: 5 scenarii de respingere, 3 niveluri de dobanda (5%/7%/10%)
    // si verificarea explicita a tuturor getter-elor LoanDecision (isApproved, getInterestRate, getMaxLoanAmount, getRejectionReason)
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

    // 7 inputuri fara assertii, fiecare activand o instructiune noua; impreuna acopera toate cele 19 linii executabile din LoanApprovalSystem
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

    // 8 inputuri care asigura ambele ramuri (true si false) ale fiecarei decizii;
    // include age=17 (age<18 true) si age=66 (age>65 true) pentru a acoperi ambele sub-conditii ale D1
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

    // 9 inputuri care asigura ca fiecare conditie atomica (C1: age<18, C2: age>65, C3: credit<600 etc.)
    // ia atat valoarea true cat si false, independent de celelalte conditii
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

    // V(G) = e - n + 2 = 20 - 15 + 2 = 7, deci 7 cai liniar independente (setul de baza McCabe);
    // cate un input per cale P1-P7, garanteaza acoperire la nivel de ramura (branch coverage)
    @Test
    public void circuitsCoverage() {
        tester.evaluateLoan(new Customer(17, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 599, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 4, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 1, 5000, 3000));
        tester.evaluateLoan(new Customer(30, 750, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 700, 1, 5000, 1000));
        tester.evaluateLoan(new Customer(30, 620, 1, 5000, 1000));
    }

    // assertii stricte la frontierele operatorilor (18/17, 65/66, 600/599, 3/4, 2000/2001)
    // si la pragurile de dobanda (749/750, 649/650) pentru a detecta mutantii PIT generati de pitest
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