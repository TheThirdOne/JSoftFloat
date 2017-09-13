package operations;

import main.Environment;
import main.Flags;
import types.ExactFloat;
import types.Float32;

/**
 * Groups conversion operations such as integer to float32, float32 to integer, etc
 */
public class Conversions {
    public static Float32 roundToIntegral(Float32 f, Environment env) {
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
        return new ExactFloat(f).roundToIntegral(env).toFloat32(env);
    }

    public static int convertToIntegral(Float32 f, Environment env) {
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
        return new ExactFloat(f).toIntegral(env);
    }
}
