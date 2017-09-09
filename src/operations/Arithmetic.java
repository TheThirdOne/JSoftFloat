package operations;

import main.Environment;
import main.Flags;
import main.RoundingMode;
import types.ExactFloat;
import types.Float32;

/**
 * Groups any arithmetic operations such as addition, subtraction, etc
 */
public class Arithmetic {
    public static Float32 add(Float32 a, Float32 b, Environment env){
        // TODO: handle signalling correctly

        // Section 6.2
        if(a.isNaN()) return a;
        if(b.isNaN()) return b;

        // Section 6.1 and 7.2
        if(a.isInfinite()){
            if(b.isInfinite() && (b.isSignMinus() != a.isSignMinus())){
                env.flags.add(Flags.invalid);
                return Float32.NaN; // inf - inf is undefined
            }else{
                return a;
            }
        }else if(b.isInfinite()){
            return b;
        }

        // Section 6.3
        if(a.isZero()){
            if(b.isZero()){
                if(a.isSignMinus() == b.isSignMinus()){
                    return a; // They are the same, just pick one
                }else{
                    // Explicitly stated in the spec
                    return (env.mode == RoundingMode.min)?Float32.NegativeZero:Float32.Zero;
                }
            }else{
                return b;
            }
        }else if(b.isZero()){
            return a;
        }

        ExactFloat out = ((new ExactFloat(a)).add(new ExactFloat(b)));
        // Check to see if it was x + (-x)
        if(out.isZero()){
            return (env.mode == RoundingMode.min)?Float32.NegativeZero:Float32.Zero;
        }
        return out.toFloat32(env);
    }

    public static Float32 sub(Float32 a, Float32 b, Environment env){
        // TODO: handle signalling correctly

        // Section 6.2
        if(a.isNaN()) return a;
        if(b.isNaN()) return b;

        // After this it is equivalent to adding a negative
        return add(a,b.negate(),env);
    }

    public static Float32 mult(Float32 a, Float32 b, Environment env){
        // TODO: handle signalling correctly

        // Section 6.2
        if(a.isNaN()) return a;
        if(b.isNaN()) return b;

        // Section 7.2
        if((a.isZero() && b.isInfinite()) || (b.isZero() && a.isInfinite())){
            return Float32.NaN;
        }

        if(a.isZero() ||b.isZero()){
            return a.isSignMinus() == a.isSignMinus()?Float32.Zero:Float32.NegativeZero;
        }

        return (new ExactFloat(a)).multiply(new ExactFloat(b)).toFloat32(env);
    }
}
