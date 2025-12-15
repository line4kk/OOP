package functions;

import engine.annotations.AnalyticalFunction;

@AnalyticalFunction
public class CosFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return Math.cos(x);
    }
}
