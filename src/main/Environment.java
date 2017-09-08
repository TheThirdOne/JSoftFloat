package main;

import java.util.EnumSet;

public class Environment {
    public EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
    public RoundingMode mode = RoundingMode.even;
}
