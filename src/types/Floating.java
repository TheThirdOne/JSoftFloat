package types;

public abstract class Floating {
    public abstract boolean isSignMinus();
    public abstract boolean isInfinite();
    public boolean isFinite(){
        return isZero() || isNormal() || isSubnormal();
    }
    public abstract boolean isNormal();
    public abstract boolean isSubnormal();
    public abstract boolean isNaN();
    public abstract boolean isSignalling();
    public abstract boolean isCanonical();
    public abstract boolean isZero();
}
