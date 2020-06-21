package jsoftfloat.operations;

import jsoftfloat.Environment;
import jsoftfloat.Flags;
import jsoftfloat.internal.ExactFloat;
import jsoftfloat.types.Floating;

import java.math.BigInteger;

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

    public static <T extends Floating<T>> BigInteger convertToIntegral(T f, BigInteger max, BigInteger min, Environment env, boolean quiet) {
        // Section 5.9 and 7.2
        if (f.isNaN()) {
            env.flags.add(Flags.invalid);
            return max;
        }

        if (f.isInfinite()) {
            env.flags.add(Flags.invalid);
            return f.isSignMinus() ? min : max;
        }

        Environment copy = new Environment();
        copy.mode = env.mode;
        BigInteger rounded;
        if(f.isZero()){
            rounded = BigInteger.ZERO;
        } else {
            rounded = f.toExactFloat().toIntegral(env);
        }

        // Section 5.8
        if (rounded.compareTo(max) > 0 || rounded.compareTo(min) < 0){
            env.flags.add(Flags.invalid);
        } else if (!quiet && copy.flags.contains(Flags.inexact)){
            env.flags.add(Flags.inexact);
        }
        return rounded.min(max).max(min); // clamp rounded to between max and min
    }

    public static <T extends Floating<T>> int convertToInt(T f, Environment env) {
        return convertToInt(f,env,false);
    }

    public static <T extends Floating<T>> int convertToInt(T f, Environment env, boolean quiet) {
        BigInteger rounded = convertToIntegral(f,BigInteger.valueOf(Integer.MAX_VALUE),BigInteger.valueOf(Integer.MIN_VALUE),env,quiet);
        return rounded.intValueExact();
    }


    public static <T extends Floating<T>> int convertToUnsignedInt(T f, Environment env, boolean quiet) {
        BigInteger rounded = convertToIntegral(f,BigInteger.valueOf(0xFFFFFFFFL),BigInteger.ZERO,env,quiet);
        return (int)(rounded.longValueExact()&0xFFFFFFFFL);
    }

    public static <T extends Floating<T>> long convertToLong(T f, Environment env, boolean quiet) {
        BigInteger rounded = convertToIntegral(f,BigInteger.valueOf(Long.MAX_VALUE),BigInteger.valueOf(Long.MIN_VALUE),env,quiet);
        return rounded.longValueExact();
    }


    public static <T extends Floating<T>> long convertToUnsignedLong(T f, Environment env, boolean quiet) {
        BigInteger rounded = convertToIntegral(f,BigInteger.valueOf(-1).add(BigInteger.ONE.shiftLeft(64)),BigInteger.ZERO,env,quiet);
        return rounded.longValue();
    }


    public static <T extends Floating<T>> T convertFromInt(BigInteger i, Environment env, T helper) {
        if(i.equals(BigInteger.ZERO)){
            return helper.Zero();
        }
        return helper.fromExactFloat(new ExactFloat(i),env);
    }
}
