package loan;


public class Customer {

    private final int age;

    private final int creditScore;

    private final int latePaymentsCount;

    private final double netSalary;

    private final double monthlyDebts;

    
    public Customer(int age, int creditScore, int latePaymentsCount,
                    double netSalary, double monthlyDebts) {
        this.age = age;
        this.creditScore = creditScore;
        this.latePaymentsCount = latePaymentsCount;
        this.netSalary = netSalary;
        this.monthlyDebts = monthlyDebts;
    }

    public int getAge() {
        return age;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public int getLatePaymentsCount() {
        return latePaymentsCount;
    }

    public double getNetSalary() {
        return netSalary;
    }

    public double getMonthlyDebts() {
        return monthlyDebts;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "age=" + age +
                ", creditScore=" + creditScore +
                ", latePaymentsCount=" + latePaymentsCount +
                ", netSalary=" + netSalary +
                ", monthlyDebts=" + monthlyDebts +
                '}';
    }
}
