package jsoftfloat.operations;

import jsoftfloat.Environment;
import jsoftfloat.Flags;
import jsoftfloat.RoundingMode;
import jsoftfloat.internal.ExactFloat;
import jsoftfloat.types.Floating;

/**
 * Groups any arithmetic operations such as addition, subtraction, etc
 */
public class Arithmetic {
    public static <T extends Floating<T>> T add(T a, T b, Environment env) {
        // TODO: handle signalling correctly

        // Section 6.2
        if (a.isNaN()) return a;
        if (b.isNaN()) return b;

        // Section 6.1 and 7.2
        if (a.isInfinite()) {
            if (b.isInfinite() && (b.isSignMinus() != a.isSignMinus())) {
                env.flags.add(Flags.invalid);
                return a.NaN(); // inf - inf is undefined
            } else {
                return a;
            }
        } else if (b.isInfinite()) {
            return b;
        }

        // Section 6.3
        if (a.isZero()) {
            if (b.isZero()) {
                if (a.isSignMinus() == b.isSignMinus()) {
                    return a; // They are the same, just pick one
                } else {
                    // Explicitly stated in the spec
                    return (env.mode == RoundingMode.min) ? a.NegativeZero() : a.Zero();
                }
            } else {
                return b;
            }
        } else if (b.isZero()) {
            return a;
        }

        ExactFloat out = (a.toExactFloat()).add(b.toExactFloat());
        // Check to see if it was x + (-x)
        if (out.isZero()) {
            return (env.mode == RoundingMode.min) ? a.NegativeZero() : a.Zero();
        }
        return a.fromExactFloat(out, env);
    }

    public static <T extends Floating<T>> T subtraction(T a, T b, Environment env) {
        // TODO: handle signalling correctly

        // Section 6.2
        if (a.isNaN()) return a;
        if (b.isNaN()) return b;

        // After this it is equivalent to adding a negative
        return add(a, b.negate(), env);
    }

    public static <T extends Floating<T>> T multiplication(T a, T b, Environment env) {
        // TODO: handle signalling correctly

        // Section 6.2
        if (a.isNaN()) return a;
        if (b.isNaN()) return b;

        // Section 7.2
        if ((a.isZero() && b.isInfinite()) || (b.isZero() && a.isInfinite())) {
            env.flags.add(Flags.invalid);
            return a.NaN();
        }

        // Section 6.1
        if (a.isInfinite() || b.isInfinite()) {
            return a.isSignMinus() == b.isSignMinus() ? a.Infinity() : a.NegativeInfinity();
        }

        if (a.isZero() || b.isZero()) {
            return a.isSignMinus() == b.isSignMinus() ? a.Zero() : a.NegativeZero();
        }

        return a.fromExactFloat(a.toExactFloat().multiply(b.toExactFloat()), env);
    }

    public static <T extends Floating<T>> T squareRoot(T a, Environment env) {
        // TODO: handle signalling correctly

        // Section 6.2
        if (a.isNaN()) return a;

        // Section 6.3 or Section 5.4.1
        if (a.isZero()) {
            return a;
        }

        // Section 7.2
        if (a.isSignMinus()) {
            env.flags.add(Flags.invalid);
            return a.NaN();
        }

        // Section 6.1
        if (a.isInfinite()) {
            return a;
        }

        return a.fromExactFloat(a.toExactFloat().squareRoot(a.maxPrecision()), env);
    }

    public static <T extends Floating<T>> T fusedMultiplyAdd(T a, T b, T c, Environment env) {
        // TODO: handle signalling correctly

        // Section 6.2
        if (a.isNaN()) return a;
        if (b.isNaN()) return b;
        // This behaviour is implementation defined - Section 7.2
        if (c.isNaN()) return c;

        // Section 7.2
        if ((a.isZero() && b.isInfinite()) || (b.isZero() && a.isInfinite())) {
            env.flags.add(Flags.invalid);
            return a.NaN();
        }

        // Section 6.1
        if (a.isInfinite() || b.isInfinite()) {
            return add(a.isSignMinus() == b.isSignMinus() ? a.Infinity() : a.NegativeInfinity(), c, env);
        }

        if (a.isZero() || b.isZero()) {
            return add(a.isSignMinus() == b.isSignMinus() ? a.Zero() : a.NegativeZero(), c, env);
        }

        ExactFloat multiplication = a.toExactFloat().multiply(b.toExactFloat());

        return a.fromExactFloat(multiplication.add(c.toExactFloat()), env);
    }

    public static <T extends Floating<T>> T division(T a, T b, Environment env) {
        // TODO: handle signalling correctly

        // Section 6.2
        if (a.isNaN()) return a;
        if (b.isNaN()) return b;

        // Section 7.2
        if ((a.isZero() && b.isZero()) || (a.isInfinite() && b.isInfinite())) {
            env.flags.add(Flags.invalid);
            return a.NaN();
        }

        // Section 6.1
        if (a.isInfinite()) {
            return (a.isSignMinus() == b.isSignMinus()) ? a.Infinity() : a.NegativeInfinity();
        }

        if (b.isInfinite() || a.isZero()) {
            return (a.isSignMinus() == b.isSignMinus()) ? a.Zero() : a.NegativeZero();
        }

        // Section 7.3
        if (b.isZero()) {
            env.flags.add(Flags.divByZero);
            return (a.isSignMinus() == b.isSignMinus()) ? a.Infinity() : a.NegativeInfinity();
        }

        assert a.isFinite() && b.isFinite() : "Both should definitely be finite by this point";

        // TODO: in tie cases round away from zero despite rounding mode unless actually precise
        return a.fromExactFloat(a.toExactFloat().divide(b.toExactFloat(), a.maxPrecision()), env);
    }
}
