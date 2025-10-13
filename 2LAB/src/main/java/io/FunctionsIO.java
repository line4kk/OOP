package io;

import functions.Point;
import functions.TabulatedFunction;
import operations.TabulatedFunctionOperationService;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class FunctionsIO {
    private FunctionsIO() {
        throw new UnsupportedOperationException("Нельзя создать экземпляр служебного класса");
    }

    public static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function) throws IOException {
        try (DataOutputStream stream = new DataOutputStream(outputStream)) {
            Point[] points = TabulatedFunctionOperationService.asPoints(function);
            stream.writeInt(function.getCount());
            for (Point point : points) {
                stream.writeDouble(point.x);
                stream.writeDouble(point.y);
            }
            stream.flush();
        }
    }
}
