import org.junit.jupiter.api.Test;
import types.Float32;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by benjamin on 9/2/17.
 */
public class Float32Test {
    @Test
    public void fromInteger() {
        assertEquals(0x0, Float32.fromInteger(0).bits);
        assertEquals(0x3f800000, Float32.fromInteger(1).bits);
        assertEquals(0x40000000, Float32.fromInteger(2).bits);
        assertEquals(0xbf800000, Float32.fromInteger(-1).bits);
        assertEquals(0x44000000, Float32.fromInteger(512).bits);
        assertEquals(0x4e5693a4, Float32.fromInteger(900000000).bits);
    }
}