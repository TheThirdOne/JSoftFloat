package jsoftfloat;

/**
 * The different ways that rounding can be performed
 */
public enum RoundingMode {
    /**
     * Round to nearest; in event of a tie, round towards even
     */
    even,
    /**
     * Round to nearest; in event of a tie, round away from zero
     */
    away,
    /**
     * Round towards -Infinity
     */
    min,
    /**
     * Round towards +Infinity
     */
    max,
    /**
     * Round towards zero
     */
    zero
}
