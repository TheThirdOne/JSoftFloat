import main.Environment;
import operations.Arithmetic;
import operations.Conversions;
import org.junit.jupiter.api.Test;
import types.Float32;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestArithmetic {
    @Test
    public void TestAdd(){
        assertEquals(2, addHelper(1,1));
        assertEquals(0, addHelper(1,-1));
        assertEquals(-1, addHelper(1,-2));
        assertEquals(1, addHelper(-1,2));
        assertEquals(2, addHelper(0,2));

        assertEquals(Float32.NaN.bits, Arithmetic.add(Float32.Infinity,Float32.NegativeInfinity, new Environment()).bits);
        assertEquals(Float32.NaN.bits, Arithmetic.add(Float32.NaN,Float32.NegativeInfinity, new Environment()).bits);
        assertEquals(Float32.NaN.bits, Arithmetic.add(Float32.NaN,Float32.fromInteger(2), new Environment()).bits);
    }

    public int addHelper(int a, int b){
        return Conversions.convertToIntegral(Arithmetic.add(Float32.fromInteger(a),Float32.fromInteger(b), new Environment()),new Environment());
    }
}
