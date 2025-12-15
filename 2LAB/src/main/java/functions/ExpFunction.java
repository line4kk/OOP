package functions;

import annotations.AnalyticalFunction;

@AnalyticalFunction
public class ExpFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return Math.exp(x);
    }
}
