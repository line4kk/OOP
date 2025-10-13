package operations;

import functions.TabulatedFunction;
import functions.Point;

public class TabulatedFunctionOperationService {
    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        if (tabulatedFunction == null) {
            throw new IllegalArgumentException("Функция не была получена");
        }

        int count = tabulatedFunction.getCount();
        Point[] all_points = new Point[count];

        int i = 0;
        for (Point point : tabulatedFunction) {
            all_points[i] = point;
            i++;
        }

        return all_points;
    }
}
