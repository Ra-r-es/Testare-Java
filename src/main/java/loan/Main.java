package loan;

public class Main {
    public static void main(String[] args) {
        LoanApprovalSystem system = new LoanApprovalSystem();

        Customer[] customers = {
            new Customer(30, 800, 0, 5000, 500),
            new Customer(30, 700, 2, 5000, 1000),
            new Customer(30, 620, 1, 5000, 1000),
            new Customer(17, 700, 0, 5000, 1000),
            new Customer(30, 500, 0, 5000, 1000),
            new Customer(30, 700, 5, 5000, 1000),
            new Customer(30, 700, 0, 5000, 2500),
        };

        for (Customer c : customers) {
            LoanDecision d = system.evaluateLoan(c);
            System.out.println(c);
            System.out.println("  -> " + d);
            System.out.println();
        }
    }
}
