package operations;

import main.Environment;
import main.RoundingMode;
import types.ExactFloat;
import types.Float32;

/**
 * Created by benjamin on 9/7/17.
 */
public class Conversions {
    public Float32 roundToIntegral(Float32 f, Environment env){
        // Section 5.9 and 7.2
        if(f.isNaN()){
            //TODO: signal invalid operation
            return f;
        }
        if(f.isInfinite()){
            return f;
        }
        if(f.isZero()){
            return f;
        }
        return new ExactFloat(f).roundToIntegral(env).toFloat32(env);
    }
}
