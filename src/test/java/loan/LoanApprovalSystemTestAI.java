package loan;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoanApprovalSystemTestAI {

    LoanApprovalSystem tester = new LoanApprovalSystem();

    @Test
    public void testMain() {
        // Testează execuția fluxului principal din Main pentru a asigura că nu crapă
        Main.main(new String[]{});
        assertNotNull(tester);
    }

    @Test
    public void equivalencePartitioning() {
        // Clase de echivalență: Aprobat (Scor mare), Respins (Vârstă), Respins (Scor mic)

        // Aprobat: Scor bun, DTI mic
        Customer approved = new Customer(30, 800, 0, 5000, 500);
        assertTrue(tester.evaluateLoan(approved).isApproved());

        // Respins: Scor sub limită (< 600)
        Customer lowCredit = new Customer(30, 500, 0, 5000, 500);
        assertFalse(tester.evaluateLoan(lowCredit).isApproved());
    }

    @Test
    public void boundaryValueAnalysis() {
        // Testăm limitele: Vârsta (18, 65), Scor (600, 650, 750), DTI (0.40)

        // Vârstă la limită (18 - OK, 17 - REJECTED)
        assertTrue(tester.evaluateLoan(new Customer(18, 700, 0, 5000, 500)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(17, 700, 0, 5000, 500)).isApproved());

        // Vârstă la limită superioară (65 - OK, 66 - REJECTED)
        assertTrue(tester.evaluateLoan(new Customer(65, 700, 0, 5000, 500)).isApproved());
        assertFalse(tester.evaluateLoan(new Customer(66, 700, 0, 5000, 500)).isApproved());

        // Scor credit (599 - REJECTED, 600 - OK)
        assertFalse(tester.evaluateLoan(new Customer(30, 599, 0, 5000, 500)).isApproved());
        assertTrue(tester.evaluateLoan(new Customer(30, 600, 0, 5000, 500)).isApproved());
    }

    @Test
    public void categoryPartitioning() {
        // Categorii de interes: Rata dobânzii bazată pe scor

        // Categoria 10% (600 - 649)
        assertEquals(10.0, tester.evaluateLoan(new Customer(30, 620, 0, 5000, 500)).getInterestRate());
        // Categoria 7% (650 - 749)
        assertEquals(7.0, tester.evaluateLoan(new Customer(30, 700, 0, 5000, 500)).getInterestRate());
        // Categoria 5% (>= 750)
        assertEquals(5.0, tester.evaluateLoan(new Customer(30, 750, 0, 5000, 500)).getInterestRate());
    }

    @Test
    public void statementCoverage() {
        // Acoperim toate liniile de cod prin diverse scenarii de respingere și succes

        // Respingere vârstă
        assertEquals("Age out of range [18-65]", tester.evaluateLoan(new Customer(10, 700, 0, 5000, 500)).getRejectionReason());
        // Respingere scor
        assertEquals("Credit score too low (min 600)", tester.evaluateLoan(new Customer(30, 500, 0, 5000, 500)).getRejectionReason());
        // Respingere plăți întârziate
        assertEquals("Too many late payments (max 3)", tester.evaluateLoan(new Customer(30, 700, 5, 5000, 500)).getRejectionReason());
        // Respingere DTI
        assertEquals("Debt-to-income ratio exceeds 40%", tester.evaluateLoan(new Customer(30, 700, 0, 5000, 3000)).getRejectionReason());
    }

    @Test
    public void branchCoverage() {
        // Ne asigurăm că ambele ramuri (true/false) ale fiecărui 'if' sunt executate
        // De exemplu, pentru scorul de credit:
        assertTrue(tester.evaluateLoan(new Customer(30, 600, 0, 5000, 500)).isApproved()); // scor >= 600
        assertFalse(tester.evaluateLoan(new Customer(30, 599, 0, 5000, 500)).isApproved()); // scor < 600
    }

    @Test
    public void conditionCoverage() {
        // Condiții compuse (în cazul nostru vârsta: age < 18 || age > 65)

        assertFalse(tester.evaluateLoan(new Customer(15, 700, 0, 5000, 500)).isApproved()); // age < 18 is true
        assertFalse(tester.evaluateLoan(new Customer(70, 700, 0, 5000, 500)).isApproved()); // age > 65 is true
        assertTrue(tester.evaluateLoan(new Customer(30, 700, 0, 5000, 500)).isApproved());  // ambele false
    }

    @Test
    public void circuitsCoverage() {
        // Similar cu branch coverage, dar se concentrează pe drumurile prin logica de calcul a dobânzii

        LoanDecision d1 = tester.evaluateLoan(new Customer(30, 760, 0, 5000, 500));
        assertEquals(5.0, d1.getInterestRate()); // Drumul 1: Scor >= 750

        LoanDecision d2 = tester.evaluateLoan(new Customer(30, 660, 0, 5000, 500));
        assertEquals(7.0, d2.getInterestRate()); // Drumul 2: 650 <= Scor < 750

        LoanDecision d3 = tester.evaluateLoan(new Customer(30, 610, 0, 5000, 500));
        assertEquals(10.0, d3.getInterestRate()); // Drumul 3: Scor < 650
    }

    @Test
    void killMutants() throws Exception {
        // Mutant 1: Schimbarea pragului DTI de la 0.40 la 0.41 (Boundary survival)
        // Omoară mutantul care ar permite un DTI de fix 0.40 dacă operatorul > devine >=
        Customer dtiLimit = new Customer(30, 700, 0, 1000, 400); // DTI fix 0.40
        LoanDecision decision = tester.evaluateLoan(dtiLimit);
        assertTrue(decision.isApproved(), "Mutant omorât: DTI de 0.40 ar trebui să fie aprobat.");

        // Mutant 2: Schimbarea calculului maxLoanAmount (modificarea constantei 60 cu 12)
        // Verificăm valoarea exactă a împrumutului maxim
        // (5000 * 0.4 - 500) * 60 = (2000 - 500) * 60 = 1500 * 60 = 90000
        Customer loanCalc = new Customer(30, 800, 0, 5000, 500);
        assertEquals(90000.0, tester.evaluateLoan(loanCalc).getMaxLoanAmount(), 0.001);

        // Mutant 3: Schimbarea condiției latePaymentsCount > 3 în >= 3
        Customer latePaymentsLimit = new Customer(30, 700, 3, 5000, 500);
        assertTrue(tester.evaluateLoan(latePaymentsLimit).isApproved(), "Mutant omorât: 3 plăți întârziate ar trebui să fie OK.");
    }
}