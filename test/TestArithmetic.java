import main.Environment;
import main.Flags;
import main.RoundingMode;
import operations.Arithmetic;
import operations.Conversions;
import org.junit.jupiter.api.Test;
import types.Float32;

import static org.junit.jupiter.api.Assertions.*;

public class TestArithmetic {
    @Test
    public void TestAdd() {

        assertEquals(2, addHelper(1, 1));
        assertEquals(0, addHelper(1, -1));
        assertEquals(-1, addHelper(1, -2));
        assertEquals(1, addHelper(-1, 2));
        assertEquals(2, addHelper(0, 2));

        Environment e = new Environment();
        assertEquals(Float32.NaN.bits, Arithmetic.add(Float32.Infinity, Float32.NegativeInfinity, e).bits);
        assertTrue(e.flags.contains(Flags.invalid));
        e.flags.clear();
        assertEquals(Float32.Infinity.bits, Arithmetic.add(Float32.Infinity, Float32.fromInteger(1), e).bits);
        assertEquals(Float32.Infinity.bits, Arithmetic.add(Float32.fromInteger(1), Float32.Infinity, e).bits);
        assertEquals(Float32.NaN.bits, Arithmetic.add(Float32.NaN, Float32.NegativeInfinity, e).bits);
        assertEquals(Float32.NaN.bits, Arithmetic.add(Float32.NaN, Float32.fromInteger(1), e).bits);
        assertEquals(Float32.Zero.bits, Arithmetic.add(Float32.Zero, Float32.Zero, e).bits);
        assertEquals(Float32.NegativeZero.bits, Arithmetic.add(Float32.NegativeZero, Float32.NegativeZero, e).bits);
        assertEquals(Float32.Zero.bits, Arithmetic.add(Float32.Zero, Float32.Zero, e).bits);
        assertEquals(Float32.Zero.bits, Arithmetic.add(Float32.Zero, Float32.NegativeZero, new Environment(RoundingMode.zero)).bits);
        assertEquals(Float32.Zero.bits, Arithmetic.add(Float32.Zero, Float32.NegativeZero, new Environment(RoundingMode.max)).bits);
        assertEquals(Float32.Zero.bits, Arithmetic.add(Float32.Zero, Float32.NegativeZero, new Environment(RoundingMode.even)).bits);
        assertEquals(Float32.Zero.bits, Arithmetic.add(Float32.Zero, Float32.NegativeZero, new Environment(RoundingMode.away)).bits);
        assertEquals(Float32.NegativeZero.bits, Arithmetic.add(Float32.Zero, Float32.NegativeZero, new Environment(RoundingMode.min)).bits);
        assertTrue(e.flags.isEmpty());

        // Some extra random tests which caught bugs in the F32 specialization
        assertEquals(1964807572,Arithmetic.add(new Float32(1964807572), new Float32(949565186), new Environment()).bits);

    }

    private int addHelper(int a, int b) {
        Environment env = new Environment();
        int out = Conversions.convertToIntegral(Arithmetic.add(Float32.fromInteger(a), Float32.fromInteger(b), env), env);
        assertFalse(env.flags.contains(Flags.inexact));
        return out;
    }

    @Test
    public void TestSub() {
        assertEquals(0, subHelper(1, 1));
        assertEquals(1, subHelper(1, 0));
        assertEquals(-1, subHelper(0, 1));
        assertEquals(-3, subHelper(-1, 2));
        assertEquals(1, subHelper(2, 1));

        assertEquals(Float32.NaN.bits, Arithmetic.subtraction(Float32.Infinity, Float32.Infinity, new Environment()).bits);
        assertEquals(Float32.Infinity.bits, Arithmetic.subtraction(Float32.Infinity, Float32.fromInteger(2), new Environment()).bits);
        assertEquals(Float32.NaN.bits, Arithmetic.subtraction(Float32.NaN, Float32.NegativeInfinity, new Environment()).bits);
        assertEquals(Float32.NaN.bits, Arithmetic.subtraction(Float32.NaN, Float32.fromInteger(2), new Environment()).bits);
    }

    private int subHelper(int a, int b) {
        return Conversions.convertToIntegral(Arithmetic.subtraction(Float32.fromInteger(a), Float32.fromInteger(b), new Environment()), new Environment());
    }

    @Test
    public void TestMult() {
        assertEquals(0, multHelper(0, 1));
        assertEquals(1, multHelper(1, 1));
        assertEquals(4, multHelper(2, 2));
        assertEquals(-1, multHelper(1, -1));
        assertEquals(1, multHelper(-1, -1));

        assertEquals(Float32.Infinity.bits, Arithmetic.multiplication(Float32.Infinity, Float32.Infinity, new Environment()).bits);
        assertEquals(Float32.NegativeInfinity.bits, Arithmetic.multiplication(Float32.Infinity, Float32.NegativeInfinity, new Environment()).bits);
        assertEquals(Float32.NaN.bits, Arithmetic.multiplication(Float32.Infinity, Float32.Zero, new Environment()).bits);


        assertEquals(0,Arithmetic.multiplication(new Float32(1), new Float32(1), new Environment()).bits);
        assertEquals(Float32.Infinity.bits,Arithmetic.multiplication(new Float32(false,80,1), new Float32(false,80,1), new Environment()).bits);

        // Some extra random tests which caught bugs in the F32 specialization
        assertEquals(-607464206,Arithmetic.multiplication(new Float32(2128754534), new Float32(-1671067297), new Environment()).bits);
        assertEquals(-259016364,Arithmetic.multiplication(new Float32(-1218496645), new Float32(2024068370), new Environment()).bits);
        assertEquals(-410506980,Arithmetic.multiplication(new Float32(-1304224058), new Float32(1957864688), new Environment()).bits);
        assertEquals(1214,Arithmetic.multiplication(new Float32(174452931), new Float32(791213703), new Environment()).bits);
    }

    private int multHelper(int a, int b) {
        Environment env = new Environment();
        int out = Conversions.convertToIntegral(Arithmetic.multiplication(Float32.fromInteger(a), Float32.fromInteger(b), env), env);
        assertFalse(env.flags.contains(Flags.inexact));
        return out;
    }


    @Test
    public void TestDivision() {
        assertEquals(1, intDivHelper(1, 1));
        assertEquals(-1, intDivHelper(1, -1));
        assertEquals(1, intDivHelper(-1, -1));
        assertEquals(20, intDivHelper(20, 1));
        assertEquals(6, intDivHelper(12, 2));
        assertEquals(3, intDivHelper(9, 3));

        Environment e = new Environment();
        assertEquals(1, Conversions.convertToIntegral(Arithmetic.multiplication(divHelper(1, 2, e),
                Float32.fromInteger(2), new Environment()), new Environment()));

        assertEquals(0x3F000000, divHelper(1, 2, e).bits);
        assertEquals(0x3E800000, divHelper(1, 4, e).bits);
        assertEquals(0x3F400000, divHelper(3, 4, e).bits);
        assertFalse(e.flags.contains(Flags.inexact));

        assertFalse(inexactDivision(1, 1));
        assertFalse(inexactDivision(1, 2));
        assertFalse(inexactDivision(345, 690));
        assertTrue(inexactDivision(1, 3));

        e = new Environment();
        assertEquals(Float32.NaN.bits, Arithmetic.division(Float32.Zero, Float32.Zero, e).bits);
        assertEquals(Float32.NaN.bits, Arithmetic.division(Float32.Infinity, Float32.Infinity, e).bits);
        assertEquals(Float32.NaN.bits, Arithmetic.division(Float32.NegativeInfinity, Float32.Infinity, e).bits);
        e.flags.contains(Flags.invalid);
        e.flags.remove(Flags.invalid);
        assertTrue(e.flags.isEmpty());

        assertEquals(Float32.Infinity.bits, Arithmetic.division(Float32.fromInteger(1), Float32.Zero, e).bits);
        assertEquals(Float32.NegativeInfinity.bits, Arithmetic.division(Float32.fromInteger(-1), Float32.Zero, e).bits);
        assertEquals(Float32.NegativeInfinity.bits, Arithmetic.division(Float32.fromInteger(1), Float32.NegativeZero, e).bits);
        e.flags.contains(Flags.divByZero);
        e.flags.remove(Flags.divByZero);
        assertTrue(e.flags.isEmpty());

        assertEquals(Float32.Zero.bits, Arithmetic.division(Float32.Zero, Float32.fromInteger(1), e).bits);
        assertEquals(Float32.NegativeZero.bits, Arithmetic.division(Float32.NegativeZero, Float32.fromInteger(1), e).bits);
        assertEquals(Float32.Zero.bits, Arithmetic.division(Float32.fromInteger(1), Float32.Infinity, e).bits);
        assertEquals(Float32.NegativeZero.bits, Arithmetic.division(Float32.fromInteger(1), Float32.NegativeInfinity, e).bits);
        assertEquals(Float32.Infinity.bits, Arithmetic.division(Float32.Infinity, Float32.fromInteger(1), e).bits);
        assertEquals(Float32.Infinity.bits, Arithmetic.division(Float32.Infinity, Float32.Zero, e).bits);
        assertTrue(e.flags.isEmpty());

    }

    private int intDivHelper(int a, int b) {
        Environment env = new Environment();
        int out = Conversions.convertToIntegral(divHelper(a,b,env),env);
        assertFalse(env.flags.contains(Flags.inexact));
        return out;
    }

    private Float32 divHelper(int a, int b, Environment e) {
        return Arithmetic.division(Float32.fromInteger(a), Float32.fromInteger(b),e);
    }

    private boolean inexactDivision(int a, int b) {
        Environment e = new Environment();
        Arithmetic.division(Float32.fromInteger(a), Float32.fromInteger(b), e);
        return e.flags.contains(Flags.inexact);
    }

    @Test
    public void sqrtTest() {
        assertEquals(7, intSqrtHelper(49));
        assertEquals(5, intSqrtHelper(25));
        assertEquals(4, intSqrtHelper(16));
        assertEquals(1, intSqrtHelper(1));
        assertEquals(0, intSqrtHelper(0));

    }

    private int intSqrtHelper(int a) {
        Environment env = new Environment();
        int out = Conversions.convertToIntegral(sqrtHelper(a,env),env);
        assertFalse(env.flags.contains(Flags.inexact));
        return out;
    }

    private Float32 sqrtHelper(int a, Environment e) {
        return Arithmetic.squareRoot(Float32.fromInteger(a), e);
    }
}
