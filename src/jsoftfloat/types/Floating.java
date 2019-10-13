package jsoftfloat.types;

import jsoftfloat.Environment;
import jsoftfloat.internal.ExactFloat;

/**
 * General classifications that any floating point class needs to provide.
 */
public abstract class Floating<T extends Floating<T>> {
    // TODO: order/group these
    public abstract boolean isSignMinus();

    public abstract boolean isInfinite();

    public boolean isFinite() {
        return isZero() || isNormal() || isSubnormal();
    }

    public abstract boolean isNormal();

    public abstract boolean isSubnormal();

    public abstract boolean isNaN();

    public abstract boolean isSignalling();

    public abstract boolean isCanonical();

    public abstract boolean isZero();

    // TODO: consider making a full bit field representation method for generic conversions
    public abstract int maxPrecision();

    public abstract T NaN();

    public abstract T Zero();

    public abstract T NegativeZero();

    public abstract T Infinity();

    public abstract T NegativeInfinity();

    public abstract T fromExactFloat(ExactFloat f, Environment env);

    public abstract ExactFloat toExactFloat();

    public abstract T negate();

}
