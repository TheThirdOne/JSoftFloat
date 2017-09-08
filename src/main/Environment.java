package main;

import types.ExactFloat;
import types.Float32;

import java.util.EnumSet;

public class Environment {
    public EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
    public RoundingMode mode = RoundingMode.even;

    public Float32 add(Float32 a, Float32 b){
        // TODO: handle signalling correctly

        // Section 6.2
        if(a.isNaN()) return a;
        if(b.isNaN()) return b;

        // Section 6.1 and 7.2
        if(a.isInfinite()){
            if(b.isInfinite() && (b.isSignMinus() != a.isSignMinus())){
                // TODO: Signal invalid operation
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
                    return (mode == RoundingMode.min)?Float32.NegativeZero:Float32.Zero;
                }
            }else{
                return b;
            }
        }else if(b.isZero()){
            return a;
        }

        Float32 out = ((new ExactFloat(a)).add(new ExactFloat(b))).toFloat32(this);
        // Check to see if it was x + (-x)
        if(out.isZero()){
            return (mode == RoundingMode.min)?Float32.NegativeZero:Float32.Zero;
        }
        return out;
    }

    public Float32 sub(Float32 a, Float32 b){
        return Float32.Zero;
    }

    public Float32 mult(Float32 a, Float32 b){
        return Float32.Zero;
    }
}
