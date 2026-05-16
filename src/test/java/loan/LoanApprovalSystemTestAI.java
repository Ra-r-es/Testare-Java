package loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.github.stefanbirkner.systemlambda.SystemLambda;

import static org.junit.jupiter.api.Assertions.*;

public class LoanApprovalSystemTestAI {

    private LoanApprovalSystem tester;

    @BeforeEach
    void setUp() {
        tester = new LoanApprovalSystem();
    }
    // --- 1. STATEMENT COVERAGE ---
    @Test
    public void testStatementCoverage() {
        // Calea de succes (Aprobare) cu scor maxim
        Customer approvedCustomer = new Customer(30, 760, 1, 5000.0, 1500.0);
        LoanDecision decisionArr = tester.evaluateLoan(approvedCustomer);

        assertAll("Aprobare completă",
                () -> assertTrue(decisionArr.isApproved()),
                () -> assertEquals(5.0, decisionArr.getInterestRate()),
                () -> assertEquals(60000.0, decisionArr.getMaxLoanAmount()),
                () -> assertNull(decisionArr.getRejectionReason())
        );

        // Linie de respingere (Vârstă) pentru a acoperi blocurile if de respingere
        Customer rejectedCustomer = new Customer(17, 700, 0, 5000.0, 1000.0);
        LoanDecision decisionRej = tester.evaluateLoan(rejectedCustomer);

        assertAll("Respingere vârstă",
                () -> assertFalse(decisionRej.isApproved()),
                () -> assertEquals(0.0, decisionRej.getInterestRate()),
                () -> assertEquals(0.0, decisionRej.getMaxLoanAmount()),
                () -> assertEquals("Age out of range [18-65]", decisionRej.getRejectionReason())
        );
    }

    // --- 2. BRANCH COVERAGE ---
    @Test
    @DisplayName("Branch Coverage - Acoperirea tuturor ramurilor de decizie (True/False)")
    public void testBranchCoverage() {
        // Ramura: Vârstă invalidă (True la prima verificare)
        Customer c1 = new Customer(70, 700, 0, 5000.0, 1000.0);
        assertEquals("Age out of range [18-65]", tester.evaluateLoan(c1).getRejectionReason());

        // Ramura: Vârstă validă (False), Scor credit invalid (True la a doua verificare)
        Customer c2 = new Customer(25, 550, 0, 5000.0, 1000.0);
        assertEquals("Credit score too low (min 600)", tester.evaluateLoan(c2).getRejectionReason());

        // Ramura: Vârstă & Scor valide, Late payments invalid
        Customer c3 = new Customer(25, 620, 4, 5000.0, 1000.0);
        assertEquals("Too many late payments (max 3)", tester.evaluateLoan(c3).getRejectionReason());

        // Ramura: Vârstă, Scor, Plăți valide, Debt-to-Income invalid
        Customer c4 = new Customer(25, 620, 2, 5000.0, 2500.0);
        LoanDecision d4 = tester.evaluateLoan(c4);
        assertFalse(d4.isApproved());
        assertNotNull(d4.getRejectionReason());

        // Ramurile pentru ratele dobânzilor (7% și 10%)
        Customer c5 = new Customer(25, 680, 0, 5000.0, 1000.0);
        assertEquals(7.0, tester.evaluateLoan(c5).getInterestRate());

        Customer c6 = new Customer(25, 610, 0, 5000.0, 1000.0);
        assertEquals(10.0, tester.evaluateLoan(c6).getInterestRate());
    }

    // --- 3. CONDITION COVERAGE ---
    @Test
    public void testConditionCoverage() {
        // Condiția age < 18 este Adevărată
        Customer ageTooLow = new Customer(15, 700, 0, 5000.0, 1000.0);
        assertEquals("Age out of range [18-65]", tester.evaluateLoan(ageTooLow).getRejectionReason());

        // Condiția age > 65 este Adevărată
        Customer ageTooHigh = new Customer(66, 700, 0, 5000.0, 1000.0);
        assertEquals("Age out of range [18-65]", tester.evaluateLoan(ageTooHigh).getRejectionReason());
    }

    // --- 4. BOUNDARY VALUE ANALYSIS ---
    @Test
    @DisplayName("Boundary Value Analysis - Testarea valorilor de graniță")
    public void testBoundaryValueAnalysis() {
        // Granițe Vârstă: 18 (aprobat), 65 (aprobat)
        assertTrue(tester.evaluateLoan(new Customer(18, 750, 0, 5000.0, 1000.0)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(65, 750, 0, 5000.0, 1000.0)).isApproved());

        // Granițe Credit Score & Dobândă: 599 (respins), 600 (10%), 649 (10%), 650 (7%), 749 (7%), 750 (5%)
        assertEquals("Credit score too low (min 600)", tester.evaluateLoan(new Customer(30, 599, 0, 5000.0, 1000.0)).getRejectionReason());
        assertEquals(10.0, tester.evaluateLoan(new Customer(30, 600, 0, 5000.0, 1000.0)).getInterestRate());
        assertEquals(10.0, tester.evaluateLoan(new Customer(30, 649, 0, 5000.0, 1000.0)).getInterestRate());
        assertEquals(7.0, tester.evaluateLoan(new Customer(30, 650, 0, 5000.0, 1000.0)).getInterestRate());
        assertEquals(7.0, tester.evaluateLoan(new Customer(30, 749, 0, 5000.0, 1000.0)).getInterestRate());
        assertEquals(5.0, tester.evaluateLoan(new Customer(30, 750, 0, 5000.0, 1000.0)).getInterestRate());

        // Granițe Late Payments: 3 (aprobat), 4 (respins)
        assertTrue(tester.evaluateLoan(new Customer(30, 750, 3, 5000.0, 1000.0)).isApproved());
        assertEquals("Too many late payments (max 3)", tester.evaluateLoan(new Customer(30, 750, 4, 5000.0, 1000.0)).getRejectionReason());

        // Granițe Debt-to-Income (requested / income <= 0.40): exact 0.40 (aprobat), 0.4001 (respins)
        assertTrue(tester.evaluateLoan(new Customer(30, 750, 0, 10000.0, 4000.0)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(30, 750, 0, 10000.0, 4001.0)).isApproved());
    }

    // --- 5. EQUIVALENCE PARTITIONING ---
    @Test
    @DisplayName("Equivalence Partitioning - Clase de echivalență")
    public void testEquivalencePartitioning() {
        // Clasa Valide (Toți parametrii în interiorul intervalelor permise)
        Customer validCustomer = new Customer(40, 700, 1, 6000.0, 2000.0);
        LoanDecision validDecision = tester.evaluateLoan(validCustomer);
        assertTrue(validDecision.isApproved());
        assertEquals(6000.0 * 12, validDecision.getMaxLoanAmount());

        // Clasa Invalidă: requestedAmount disproporționat față de venit (DTI picat)
        Customer invalidDebt = new Customer(40, 700, 1, 3000.0, 2500.0);
        LoanDecision invalidDecision = tester.evaluateLoan(invalidDebt);
        assertFalse(invalidDecision.isApproved());
        assertEquals(0.0, invalidDecision.getMaxLoanAmount());
        assertEquals(0.0, invalidDecision.getInterestRate());
    }

    // --- 6. CATEGORY PARTITIONING ---
    @Test
    @DisplayName("Category Partitioning - Combinații structurate de categorii")
    public void testCategoryPartitioning() {
        // Categoria A: Client Tânăr de Risc Scăzut (Aprobare rapidă, dobândă minimă)
        Customer youngLowRisk = new Customer(21, 800, 0, 8000.0, 1000.0);
        LoanDecision decA = tester.evaluateLoan(youngLowRisk);
        assertTrue(decA.isApproved());
        assertEquals(5.0, decA.getInterestRate());

        // Categoria B: Client Senior la Limită (Vârstă mare dar eligibilă, Scor mediu)
        Customer seniorMediumRisk = new Customer(64, 660, 2, 5000.0, 1500.0);
        LoanDecision decB = tester.evaluateLoan(seniorMediumRisk);
        assertTrue(decB.isApproved());
        assertEquals(7.0, decB.getInterestRate());
    }

    @Test
    public void testCircuitsCoverage() {
        // Traseul 1: Respingere directă la prima barieră (Vârstă)
        Customer pathAge = new Customer(16, 750, 0, 5000.0, 500.0);
        assertFalse(tester.evaluateLoan(pathAge).isApproved());

        // Traseul 2: Trece de vârstă, blocat la a doua barieră (Credit Score)
        Customer pathScore = new Customer(30, 550, 0, 5000.0, 500.0);
        assertFalse(tester.evaluateLoan(pathScore).isApproved());

        // Traseul 3: Trece de vârstă și credit, blocat la a treia barieră (Late Payments)
        Customer pathLate = new Customer(30, 700, 4, 5000.0, 500.0);
        assertFalse(tester.evaluateLoan(pathLate).isApproved());

        // Traseul 4: Trece de primele 3, blocat la ultima barieră (Debt-to-Income)
        Customer pathDti = new Customer(30, 700, 0, 5000.0, 3000.0);
        assertFalse(tester.evaluateLoan(pathDti).isApproved());

        // Traseul 5: Succes complet - Circuitul pentru Dobândă de 5% (Scor >= 750)
        LoanDecision d5 = tester.evaluateLoan(new Customer(30, 760, 0, 5000.0, 500.0));
        assertEquals(5.0, d5.getInterestRate());

        // Traseul 6: Succes complet - Circuitul pentru Dobândă de 7% (650 <= Scor < 750)
        LoanDecision d7 = tester.evaluateLoan(new Customer(30, 680, 0, 5000.0, 500.0));
        assertEquals(7.0, d7.getInterestRate());

        // Traseul 7: Succes complet - Circuitul pentru Dobândă de 10% (600 <= Scor < 650)
        LoanDecision d10 = tester.evaluateLoan(new Customer(30, 610, 0, 5000.0, 500.0));
        assertEquals(10.0, d10.getInterestRate());
    }
    // --- 7. KILL MUTANTS ---
    @Test
    @DisplayName("Kill Mutants - Capturare output consolă și aserțiuni stricte anti-mutație")
    public void testKillMutants() throws Exception {
        Customer standardCustomer = new Customer(35, 600, 3, 5000.0, 2000.0);

        // Folosim tapSystemOut din system-lambda pentru a intercepta printările nedorite
        String consoleOutput = SystemLambda.tapSystemOut(() -> {
            LoanDecision decision = tester.evaluateLoan(standardCustomer);

            assertNotNull(decision, "Mutantul a returnat null în loc de obiect");
            assertTrue(decision.isApproved(), "Mutantul a alterat condiția de aprobare la graniță");
            assertEquals(10.0, decision.getInterestRate(), "Mutantul a modificat pragul de dobândă pentru scorul 600");
            assertEquals(60000.0, decision.getMaxLoanAmount(), "Mutantul a modificat formula multiplicatorului venitului (*12)");
        });

        // Ne asigurăm că nu există output la consolă
        assertTrue(consoleOutput.isEmpty(), "Codul a printat în consolă! Posibil mutant sau cod redundant.");
    }
}