package functions;

import engine.annotations.AnalyticalFunction;

@AnalyticalFunction
public class IdentityFunction implements MathFunction {
    @Override
    public double apply(double x){
        return x;
    }
}
