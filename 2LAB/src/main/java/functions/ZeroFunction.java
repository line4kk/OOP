package functions;

import annotations.AnalyticalFunction;

@AnalyticalFunction
public class ZeroFunction extends ConstantFunction {
    public ZeroFunction() {
        super(0.0);
    }
}
