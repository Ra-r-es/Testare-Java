package loan;


public class LoanDecision {

    private final boolean approved;

    private final double interestRate;

    private final double maxLoanAmount;

    private final String rejectionReason;

    public LoanDecision(boolean approved, double interestRate,
                        double maxLoanAmount, String rejectionReason) {
        this.approved = approved;
        this.interestRate = interestRate;
        this.maxLoanAmount = maxLoanAmount;
        this.rejectionReason = rejectionReason;
    }

    public boolean isApproved() {
        return approved;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getMaxLoanAmount() {
        return maxLoanAmount;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    @Override
    public String toString() {
        if (approved) {
            return "LoanDecision{APPROVED, interestRate=" + interestRate +
                    "%, maxLoanAmount=" + maxLoanAmount + '}';
        }
        return "LoanDecision{REJECTED, reason='" + rejectionReason + "'}";
    }
}
