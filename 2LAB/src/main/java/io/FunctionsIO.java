package io;

import functions.Point;
import functions.TabulatedFunction;
import functions.factory.TabulatedFunctionFactory;
import operations.TabulatedFunctionOperationService;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

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
    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) throws IOException {
        PrintWriter printWriter = new PrintWriter(writer);

        printWriter.println(function.getCount());

        for (Point point : function) {
            printWriter.printf("%f %f\n", point.x, point.y);
        }

        printWriter.flush();
    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader, TabulatedFunctionFactory factory)
            throws IOException {

        try {
            String countLine = reader.readLine();
            if (countLine == null) {
                throw new IOException("Файл пустой");
            }

            int count = Integer.parseInt(countLine.trim());


            double[] xValues = new double[count];
            double[] yValues = new double[count];

            NumberFormat numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

            for (int i = 0; i < count; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("Неожиданный конец в файле");
                }

                String[] parts = line.split(" ");
                if (parts.length != 2) {
                    throw new IOException("Неправильные данные в строке: " + line);
                }

                try {
                    xValues[i] = numberFormat.parse(parts[0].trim()).doubleValue();
                    yValues[i] = numberFormat.parse(parts[1].trim()).doubleValue();
                } catch (ParseException e) {
                    throw new IOException("Ошибка парсинга чисел в строке: " + line, e);
                }
            }

            return factory.create(xValues, yValues);
        } catch(NumberFormatException e) {
            throw new IOException("Некорректный формат числа", e);
        }
    }

    public static TabulatedFunction readTabulatedFunction(BufferedInputStream inputStream, TabulatedFunctionFactory factory) throws IOException {
        // Оборачиваем в DataInputStream для чтения примитивов
        try (DataInputStream dataInputStream = new DataInputStream(inputStream)) {

            int count = dataInputStream.readInt();

            double[] xValues = new double[count];
            double[] yValues = new double[count];

            for (int i = 0; i < count; i++) {
                xValues[i] = dataInputStream.readDouble(); // читаем x
                yValues[i] = dataInputStream.readDouble(); // читаем y
            }

            // Используем фабрику для создания функции
            return factory.create(xValues, yValues);
        }
    }
    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        // Создаём поток для сериализации
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);
        // Сериализуем функцию
        objectOutputStream.writeObject(function);
        // Поток не закрыт, все данные перекидываются с flush
        objectOutputStream.flush();
    }

    public static TabulatedFunction deserialize(BufferedInputStream stream)
            throws IOException, ClassNotFoundException {
        // Оборачиваем BufferedInputStream в ObjectInputStream для десериализации
        try (ObjectInputStream objectInputStream = new ObjectInputStream(stream)) {

            // Читаем объект и приводим к типу TabulatedFunction
            TabulatedFunction function = (TabulatedFunction) objectInputStream.readObject();

            return function;
        }
    }
}
