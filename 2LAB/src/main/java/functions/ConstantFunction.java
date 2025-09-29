package functions;

public class ConstantFunction implements  MathFunction {
    private final double CONST;

    public ConstantFunction(double x) {
        this.CONST = x;
    }
    public double getConst() {
        return CONST;
    }
    @Override
    public double apply(double x) {
        return CONST;
    }

}
