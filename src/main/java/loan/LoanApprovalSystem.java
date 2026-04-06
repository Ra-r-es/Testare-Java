package loan;


public class LoanApprovalSystem {

    
    public LoanDecision evaluateLoan(Customer customer) {

        if (customer.getAge() < 18 || customer.getAge() > 65) {
            return rejected("Age out of range [18-65]");
        }

        if (customer.getCreditScore() < 600) {
            return rejected("Credit score too low (min 600)");
        }

        if (customer.getLatePaymentsCount() > 3) {
            return rejected("Too many late payments (max 3)");
        }

        double dti = customer.getMonthlyDebts() / customer.getNetSalary();
        if (dti > 0.40) {
            return rejected("Debt-to-income ratio exceeds 40%");
        }


        double interestRate;
        int score = customer.getCreditScore();
        if (score >= 750) {
            interestRate = 5.0;
        } else if (score >= 650) {
            interestRate = 7.0;
        } else {
            interestRate = 10.0;
        }

        double maxLoanAmount = (customer.getNetSalary() * 0.4 - customer.getMonthlyDebts()) * 60;

        return new LoanDecision(true, interestRate, maxLoanAmount, null);
    }

    private LoanDecision rejected(String reason) {
        return new LoanDecision(false, 0.0, 0.0, reason);
    }
}
