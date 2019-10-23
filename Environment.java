package jsoftfloat;

import java.util.EnumSet;

public class Environment {
    public EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
    public RoundingMode mode;

    public Environment(RoundingMode mode) {
        this.mode = mode;
    }

    public Environment() {
        this(RoundingMode.even);
    }
}
