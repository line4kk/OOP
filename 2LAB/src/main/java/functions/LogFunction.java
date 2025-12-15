package functions;

import annotations.AnalyticalFunction;

@AnalyticalFunction
public class LogFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return Math.log(Math.abs(x));
    }
}
