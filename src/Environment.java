import types.Float32;

import java.util.EnumSet;

public class Environment {
    EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
    RoundingMode mode = RoundingMode.even;

    public Float32 add(Float32 a, Float32 b){
        return Float32.Zero;
    }

    public Float32 mult(Float32 a, Float32 b){
        return Float32.Zero;
    }
}
