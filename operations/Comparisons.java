package jsoftfloat.operations;

import jsoftfloat.Environment;
import jsoftfloat.Flags;
import jsoftfloat.types.Floating;

public class Comparisons {

    private static <T extends Floating<T>> int compareNoNAN(T a, T b) {
        if(a.isZero()){
            if(b.isZero()){
                return 0;
            }else{
                return b.isSignMinus()?1:-1;
            }
        }
        if(b.isZero()){
            return a.isSignMinus()?-1:1;
        }
        if(a.isInfinite()){
            if(b.isInfinite() && a.isSignMinus() == b.isSignMinus()){
                return 0;
            }else{
                return a.isSignMinus()?1:-1;
            }
        }
        if(b.isInfinite()){
            return b.isSignMinus()?1:-1;
        }
        return a.toExactFloat().compareTo(b.toExactFloat());
    }
    
    private static <T extends Floating<T>> T nonNaNmin(T a, T b) {
        // If signs are different it is easy
        // Also explicitly handles -0 vs +0
        if (a.isSignMinus() != b.isSignMinus()) {
            return (a.isSignMinus() ? a : b);
        }
        // Handle the infinite cases first
        if (a.isInfinite() || b.isInfinite()) {
            if (a.isInfinite() == a.isSignMinus()) {
                return a;
            } else {
                return b;
            }
        }
        if (compareNoNAN(a,b) <= 0) {
            return a;
        } else {
            return b;
        }
    }

    private static <T extends Floating<T>> T handleNaN(T a, T b, Environment env) {
        // Section 5.3.1
        if (a.isNaN()) {
            if (b.isNaN()) {
                return b.NaN(); // Canonicalize in the case of two NaNs
            } else {
                return b;
            }
        }
        if (b.isNaN()) {
            return a;
        }
        return null;
    }

    private static <T extends Floating<T>> T handleNaNNumber(T a, T b, Environment env) {
        if (a.isSignalling() || b.isSignalling()) {
            env.flags.add(Flags.invalid);
        }
        // I think this handles the remaining cases of NaNs
        return handleNaN(a, b, env);
    }

    // minimum and minimumNumber are from the 201x revision
    public static <T extends Floating<T>> T minimum(T a, T b, Environment env) {
        T tmp = handleNaN(a, b, env);
        if (tmp != null) return tmp;
        return nonNaNmin(a, b);
    }

    public static <T extends Floating<T>> T maximum(T a, T b, Environment env) {
        T tmp = handleNaN(a, b, env);
        if (tmp != null) return tmp;
        tmp = nonNaNmin(a, b);
        return (a == tmp) ? b : a; // flip for max rather than min
    }

    // Literally the same code as above, but with a different NaN handler
    public static <T extends Floating<T>> T minimumNumber(T a, T b, Environment env) {
        T tmp = handleNaNNumber(a, b, env);
        if (tmp != null) return tmp;
        return nonNaNmin(a, b);
    }

    public static <T extends Floating<T>> T maximumNumber(T a, T b, Environment env) {
        T tmp = handleNaNNumber(a, b, env);
        if (tmp != null) return tmp;
        tmp = nonNaNmin(a, b);
        return (a == tmp) ? b : a; // flip for max rather than min
    }

    // Difference from minimumNumber explained by https://freenode.logbot.info/riscv/20191012
    // > (TLDR: minNum(a, sNaN) == minNum(sNaN, a) == qNaN, whereas minimumNumber(a, sNaN) == minimumNumber(sNaN, a) == a, where a is not NaN)
    public static <T extends Floating<T>> T minNum(T a, T b, Environment env) {
        if (a.isSignalling() || b.isSignalling()) {
            env.flags.add(Flags.invalid);
            return a.NaN();
        }
        T tmp = handleNaN(a, b, env);
        if (tmp != null) return tmp;
        return nonNaNmin(a, b);
    }

    public static <T extends Floating<T>> T maxNum(T a, T b, Environment env) {
        if (a.isSignalling() || b.isSignalling()) {
            env.flags.add(Flags.invalid);
            return a.NaN();
        }
        T tmp = handleNaN(a, b, env);
        if (tmp != null) return tmp;
        tmp = nonNaNmin(a, b);
        return (a == tmp) ? b : a; // flip for max rather than min
    }

    // All compares covered in Section 5.11
    public static <T extends Floating<T>> boolean compareQuietEqual(T a, T b, Environment env) {
        if (a.isSignalling() || b.isSignalling()) {
            env.flags.add(Flags.invalid);
        }
        if (a.isNaN() || b.isNaN()) {
            return false;
        }
        return compareNoNAN(a,b) == 0;
    }

    public static <T extends Floating<T>> boolean equalSignaling(T a, T b, Environment env) {
        if (a.isNaN() || b.isNaN()) {
            env.flags.add(Flags.invalid);
        }
        return compareQuietEqual(a, b, env);
    }

    public static <T extends Floating<T>> boolean compareQuietLessThan(T a, T b, Environment env) {
        if (a.isSignalling() || b.isSignalling()) {
            env.flags.add(Flags.invalid);
        }
        if (a.isNaN() || b.isNaN()) {
            return false;
        }
        return compareNoNAN(a,b) < 0;
    }

    public static <T extends Floating<T>> boolean compareSignalingLessThan(T a, T b, Environment env) {
        if (a.isNaN() || b.isNaN()) {
            env.flags.add(Flags.invalid);
        }
        return compareQuietLessThan(a, b, env);
    }

    public static <T extends Floating<T>> boolean compareQuietLessThanEqual(T a, T b, Environment env) {
        if (a.isSignalling() || b.isSignalling()) {
            env.flags.add(Flags.invalid);
        }
        if (a.isNaN() || b.isNaN()) {
            return false;
        }
        return compareNoNAN(a,b) <= 0;
    }

    public static <T extends Floating<T>> boolean compareSignalingLessThanEqual(T a, T b, Environment env) {
        if (a.isNaN() || b.isNaN()) {
            env.flags.add(Flags.invalid);
        }
        return compareQuietLessThanEqual(a, b, env);
    }

    public static <T extends Floating<T>> boolean compareQuietGreaterThan(T a, T b, Environment env) {
        if (a.isSignalling() || b.isSignalling()) {
            env.flags.add(Flags.invalid);
        }
        if (a.isNaN() || b.isNaN()) {
            return false;
        }
        return compareNoNAN(a,b) > 0;
    }

    public static <T extends Floating<T>> boolean compareSignalingGreaterThan(T a, T b, Environment env) {
        if (a.isNaN() || b.isNaN()) {
            env.flags.add(Flags.invalid);
        }
        return compareQuietGreaterThan(a, b, env);
    }

    public static <T extends Floating<T>> boolean compareQuietGreaterThanEqual(T a, T b, Environment env) {
        if (a.isSignalling() || b.isSignalling()) {
            env.flags.add(Flags.invalid);
        }
        if (a.isNaN() || b.isNaN()) {
            return false;
        }
        return compareNoNAN(a,b) >= 0;
    }

    public static <T extends Floating<T>> boolean compareSignalingGreaterThanEqual(T a, T b, Environment env) {
        if (a.isNaN() || b.isNaN()) {
            env.flags.add(Flags.invalid);
        }
        return compareQuietGreaterThanEqual(a, b, env);
    }

    public static <T extends Floating<T>> boolean compareQuietUnordered(T a, T b, Environment env) {
        if (a.isSignalling() || b.isSignalling()) {
            env.flags.add(Flags.invalid);
        }
        return a.isNaN() || b.isNaN();
    }
}
