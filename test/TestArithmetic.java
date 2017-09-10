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

    private int addHelper(int a, int b){
        return Conversions.convertToIntegral(Arithmetic.add(Float32.fromInteger(a),Float32.fromInteger(b), new Environment()),new Environment());
    }

    @Test
    public void TestSub(){
        assertEquals(0, subHelper(1,1));
        assertEquals(1, subHelper(1,0));
        assertEquals(-1, subHelper(0,1));
        assertEquals(-3, subHelper(-1,2));
        assertEquals(1, subHelper(2,1));

        assertEquals(Float32.NaN.bits, Arithmetic.subtraction(Float32.Infinity,Float32.Infinity, new Environment()).bits);
        assertEquals(Float32.NaN.bits, Arithmetic.subtraction(Float32.NaN,Float32.NegativeInfinity, new Environment()).bits);
        assertEquals(Float32.NaN.bits, Arithmetic.subtraction(Float32.NaN,Float32.fromInteger(2), new Environment()).bits);
    }

    private int subHelper(int a, int b){
        return Conversions.convertToIntegral(Arithmetic.subtraction(Float32.fromInteger(a),Float32.fromInteger(b), new Environment()),new Environment());
    }

    @Test
    public void TestMult(){
        assertEquals(0, multHelper(0,1));
        assertEquals(1, multHelper(1,1));
        assertEquals(4, multHelper(2,2));
        assertEquals(-1, multHelper(1,-1));
        assertEquals(1, multHelper(-1,-1));
    }
    private int multHelper(int a, int b){
        return Conversions.convertToIntegral(Arithmetic.multiplication(Float32.fromInteger(a),Float32.fromInteger(b), new Environment()),new Environment());
    }

    @Test
    public void TestDivision(){
        assertEquals(1, divHelper(1,1));
        assertEquals(-1, divHelper(1,-1));
        assertEquals(1, divHelper(-1,-1));
        assertEquals(20, divHelper(20,1));
        assertEquals(6, divHelper(12,2));
        assertEquals(3, divHelper(9,3));
    }
    private int divHelper(int a, int b){
        return Conversions.convertToIntegral(Arithmetic.division(Float32.fromInteger(a),Float32.fromInteger(b), new Environment()),new Environment());
    }
}
