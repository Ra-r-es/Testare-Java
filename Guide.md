# LoanApprovalSystem – Unit-Testing Guide

## 1. Class Overview

`LoanApprovalSystem` exposes a single method:

```java
public LoanDecision evaluateLoan(Customer customer)
```

It validates four eligibility criteria in order (age, credit score, late payments, DTI). If any check fails the method returns early with a rejection. When all checks pass it computes an interest rate and a maximum loan amount.

### Formulas

| Computation | Formula |
|---|---|
| Debt-to-income ratio (DTI) | `monthlyDebts / netSalary` |
| Max loan amount | `(netSalary * 0.4 - monthlyDebts) * 60` |

### Interest-rate tiers

| Credit-score range | Interest rate |
|---|---|
| ≥ 750 | 5.0 % |
| 650 – 749 | 7.0 % |
| 600 – 649 | 10.0 % |

---

## 2. Equivalence Classes

### 2.1 `age`

| Class | Range | Expected outcome |
|---|---|---|
| EC1 – below range | age < 18 | Reject – "Age out of range [18-65]" |
| EC2 – valid | 18 ≤ age ≤ 65 | Passes age check |
| EC3 – above range | age > 65 | Reject – "Age out of range [18-65]" |

### 2.2 `creditScore`

| Class | Range | Expected outcome |
|---|---|---|
| EC4 – too low | creditScore < 600 | Reject – "Credit score too low (min 600)" |
| EC5 – low tier | 600 ≤ creditScore < 650 | Approved at 10.0 % |
| EC6 – mid tier | 650 ≤ creditScore < 750 | Approved at 7.0 % |
| EC7 – high tier | creditScore ≥ 750 | Approved at 5.0 % |

### 2.3 `latePaymentsCount`

| Class | Range | Expected outcome |
|---|---|---|
| EC8 – acceptable | latePaymentsCount ≤ 3 | Passes late-payments check |
| EC9 – too many | latePaymentsCount > 3 | Reject – "Too many late payments (max 3)" |

### 2.4 DTI (`monthlyDebts / netSalary`)

| Class | Range | Expected outcome |
|---|---|---|
| EC10 – acceptable | DTI ≤ 0.40 | Passes DTI check |
| EC11 – too high | DTI > 0.40 | Reject – "Debt-to-income ratio exceeds 40%" |

---

## 3. Boundary Value Analysis

### 3.1 `age` boundaries

| Test value | Expected |
|---|---|
| 17 | Reject (below minimum) |
| 18 | Pass (lower boundary – valid) |
| 19 | Pass (just above lower boundary) |
| 64 | Pass (just below upper boundary) |
| 65 | Pass (upper boundary – valid) |
| 66 | Reject (above maximum) |

### 3.2 `creditScore` boundaries

| Test value | Expected |
|---|---|
| 599 | Reject (below minimum) |
| 600 | Approve (lower boundary, 10 % tier) |
| 601 | Approve (just above, 10 % tier) |
| 649 | Approve (upper edge of 10 % tier) |
| 650 | Approve (lower boundary of 7 % tier) |
| 749 | Approve (upper edge of 7 % tier) |
| 750 | Approve (lower boundary of 5 % tier) |

### 3.3 `latePaymentsCount` boundaries

| Test value | Expected |
|---|---|
| 2 | Pass |
| 3 | Pass (boundary – valid) |
| 4 | Reject (boundary – invalid) |

### 3.4 DTI boundaries (netSalary = 5000)

| monthlyDebts | DTI | Expected |
|---|---|---|
| 1999.99 | 0.399998 | Pass |
| 2000.00 | 0.40 | Pass (boundary – valid, `> 0.40` is false) |
| 2000.01 | 0.400002 | Reject |

---

## 4. Independent Paths

The control-flow graph has the following independent basis paths:

| # | Path description | Example input | Expected output |
|---|---|---|---|
| P1 | Reject – age out of range (too young) | `Customer(17, 700, 0, 5000, 1000)` | rejected, "Age out of range [18-65]" |
| P2 | Reject – credit score too low | `Customer(30, 599, 0, 5000, 1000)` | rejected, "Credit score too low (min 600)" |
| P3 | Reject – too many late payments | `Customer(30, 700, 4, 5000, 1000)` | rejected, "Too many late payments (max 3)" |
| P4 | Reject – DTI exceeds 40 % | `Customer(30, 700, 2, 5000, 2500)` | rejected, "Debt-to-income ratio exceeds 40%" |
| P5 | Approved – high credit score (≥ 750) | `Customer(30, 800, 0, 5000, 500)` | approved, 5.0 %, maxLoan = (5000\*0.4 − 500)\*60 = 90 000 |
| P6 | Approved – mid credit score (650–749) | `Customer(30, 700, 2, 5000, 1000)` | approved, 7.0 %, maxLoan = (5000\*0.4 − 1000)\*60 = 60 000 |
| P7 | Approved – low credit score (600–649) | `Customer(30, 620, 1, 5000, 1000)` | approved, 10.0 %, maxLoan = 60 000 |
| P8 | Reject – age out of range (too old) | `Customer(66, 700, 0, 5000, 1000)` | rejected, "Age out of range [18-65]" |

---

## 5. Condition Coverage

The method contains the following atomic conditions:

| ID | Condition | True example | False example |
|---|---|---|---|
| C1 | `age < 18` | age = 17 | age = 18 |
| C2 | `age > 65` | age = 66 | age = 65 |
| C3 | `creditScore < 600` | score = 599 | score = 600 |
| C4 | `latePaymentsCount > 3` | count = 4 | count = 3 |
| C5 | `dti > 0.40` | debts/salary = 0.41 | debts/salary = 0.40 |
| C6 | `score >= 750` | score = 750 | score = 749 |
| C7 | `score >= 650` | score = 650 | score = 649 |

### Compound condition in age check

```java
if (customer.getAge() < 18 || customer.getAge() > 65)
```

For **full condition coverage** of this single `if`:

| Test | C1 (`age < 18`) | C2 (`age > 65`) | Outcome |
|---|---|---|---|
| age = 17 | T | F | Reject |
| age = 66 | F | T | Reject |
| age = 30 | F | F | Pass |

(T + T is impossible for a single value.)

---

## 6. Mutation Testing – Suggested Mutants

### M1 – Relational operator: `creditScore < 600` → `creditScore <= 600`

* **Effect:** A customer with exactly 600 would be rejected instead of approved.
* **Kill test:** `Customer(30, 600, 0, 5000, 1000)` → must be **approved** at 10.0 %.

### M2 – Logical connector: `age < 18 || age > 65` → `age < 18 && age > 65`

* **Effect:** The age check would never reject anyone (no integer is both < 18 and > 65).
* **Kill test:** `Customer(17, 700, 0, 5000, 1000)` → must be **rejected**.

### M3 – Relational operator: `latePaymentsCount > 3` → `latePaymentsCount >= 3`

* **Effect:** A customer with exactly 3 late payments would be rejected instead of approved.
* **Kill test:** `Customer(30, 700, 3, 5000, 1000)` → must be **approved**.

### M4 – Constant change: `interestRate = 5.0` → `interestRate = 7.0`

* **Effect:** High-tier customers (score ≥ 750) would get 7 % instead of 5 %.
* **Kill test:** `Customer(30, 800, 0, 5000, 500)` → assert `interestRate == 5.0`.

### M5 – Arithmetic operator: `netSalary * 0.4` → `netSalary * 0.5` in max-loan formula

* **Effect:** Max loan amount would be inflated.
* **Kill test:** `Customer(30, 700, 0, 5000, 1000)` → assert `maxLoanAmount == (5000*0.4 - 1000)*60 = 60000`.

---

## 7. How to Kill Specific Mutants (Detailed)

### Killing M1 (`< 600` → `<= 600`)

Test input: `Customer(30, 600, 0, 5000, 1000)`

* **Original code:** `600 < 600` is `false` → passes check → **approved**, 10.0 %.
* **Mutant code:** `600 <= 600` is `true` → **rejected**, "Credit score too low".
* **Assertion:** `assertTrue(decision.isApproved())` **kills the mutant**.

### Killing M3 (`> 3` → `>= 3`)

Test input: `Customer(30, 700, 3, 5000, 1000)`

* **Original code:** `3 > 3` is `false` → passes check → **approved**.
* **Mutant code:** `3 >= 3` is `true` → **rejected**, "Too many late payments".
* **Assertion:** `assertTrue(decision.isApproved())` **kills the mutant**.

---

## 8. Coverage Test-Set Examples

### 8.1 Statement Coverage (minimum set to execute every line)

| # | Input | Covers |
|---|---|---|
| T1 | `Customer(17, 700, 0, 5000, 1000)` | Age reject branch |
| T2 | `Customer(30, 500, 0, 5000, 1000)` | Credit-score reject branch |
| T3 | `Customer(30, 700, 5, 5000, 1000)` | Late-payments reject branch |
| T4 | `Customer(30, 700, 0, 5000, 2500)` | DTI reject branch |
| T5 | `Customer(30, 800, 0, 5000, 500)` | Approval path, score ≥ 750 (5 %) |
| T6 | `Customer(30, 700, 0, 5000, 1000)` | Approval path, 650 ≤ score < 750 (7 %) |
| T7 | `Customer(30, 620, 0, 5000, 1000)` | Approval path, 600 ≤ score < 650 (10 %) |

T1–T7 **execute every statement** in the method.

### 8.2 Decision (Branch) Coverage

All decisions and both their `true` / `false` outcomes are covered by T1–T7 above, because:

* Age check: **true** (T1), **false** (T2–T7).
* Credit-score check: **true** (T2), **false** (T3–T7).
* Late-payments check: **true** (T3), **false** (T4–T7).
* DTI check: **true** (T4), **false** (T5–T7).
* `score >= 750`: **true** (T5), **false** (T6, T7).
* `score >= 650`: **true** (T6), **false** (T7).

### 8.3 Condition Coverage

Add T8 to cover the second atomic condition in the age check independently:

| # | Input | Purpose |
|---|---|---|
| T8 | `Customer(66, 700, 0, 5000, 1000)` | `age > 65` is **true** (C2 = T) |

Combined test set **T1–T8** achieves full condition coverage (every atomic condition evaluated to both `true` and `false` at least once).

---

## 9. Quick Reference – Rejection Reasons

| Condition | Rejection reason string |
|---|---|
| age < 18 ∨ age > 65 | `"Age out of range [18-65]"` |
| creditScore < 600 | `"Credit score too low (min 600)"` |
| latePaymentsCount > 3 | `"Too many late payments (max 3)"` |
| DTI > 0.40 | `"Debt-to-income ratio exceeds 40%"` |

---

*This guide is intended as a companion document for writing JUnit 5 tests against the `LoanApprovalSystem` class. All values are deterministic and reproducible.*
