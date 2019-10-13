package jsoftfloat;

/**
 * Exception flags which can be set in certain conditions
 */
public enum Flags {
    /**
     * Triggered when a result differs from what would have been computed were both exponent range and precision unbounded
     * <p>
     * For example, it would be triggered by 1/3
     */
    inexact,
    /**
     * Triggered when a result is tiny (|result| < b^emin; smaller than the smallest normal number)
     */
    // TODO: handling of underflow was incorrect up till this point subnormal numbers result in underflow
    underflow,
    /**
     * Triggered if a result is larger than the largest finite representable number
     */
    overflow,
    /**
     * Triggered by creating an infinite number using zero
     * <p>
     * For example, it would be triggered by 1/0 or log(0)
     */
    divByZero,
    /**
     * Triggered when an operation produces no meaningful value
     * <p>
     * For example, it would be triggered by 0/0, Infinity - Infinity, etc
     */
    invalid
}
