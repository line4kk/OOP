package functions;

import engine.annotations.AnalyticalFunction;

@AnalyticalFunction
public class SinFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return Math.sin(x);
    }
}