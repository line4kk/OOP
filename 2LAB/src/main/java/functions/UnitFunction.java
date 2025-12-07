package functions;

import engine.annotations.AnalyticalFunction;

@AnalyticalFunction
public class UnitFunction extends ConstantFunction {
    public UnitFunction() {
        super(1.0);  // Используем готовую логику родителя
    }
}
