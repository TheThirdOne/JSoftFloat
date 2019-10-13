package jsoftfloat.operations;

import jsoftfloat.Environment;
import jsoftfloat.Flags;
import jsoftfloat.types.Floating;

/**
 * Groups conversion operations such as integer to float32, float32 to integer, etc
 */
public class Conversions {
    public static <T extends Floating<T>> T roundToIntegral(T f, Environment env) {
        // Section 5.9 and 7.2
        if (f.isNaN()) {
            //TODO: signal invalid operation
            return f;
        }
        if (f.isInfinite()) {
            // TODO: handle correctly
            return f;
        }
        if (f.isZero()) {
            return f;
        }
        return f.fromExactFloat(f.toExactFloat().roundToIntegral(env), env);
    }

    public static <T extends Floating<T>> int convertToIntegral(T f, Environment env) {
        // Section 5.9 and 7.2
        if (f.isNaN()) {
            env.flags.add(Flags.invalid);
            return 0;
        }
        if (f.isInfinite()) {
            return f.isSignMinus() ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        if (f.isZero()) {
            return 0;
        }
        return f.toExactFloat().toIntegral(env);
    }
}
