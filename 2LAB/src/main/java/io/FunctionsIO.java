package io;

import functions.Point;
import functions.TabulatedFunction;
import functions.factory.TabulatedFunctionFactory;
import operations.TabulatedFunctionOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public final class FunctionsIO {
    private static final Logger logger = LoggerFactory.getLogger(FunctionsIO.class);
    private FunctionsIO() {
        throw new UnsupportedOperationException("Нельзя создать экземпляр служебного класса");
    }

    public static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function) throws IOException {
        logger.debug("Запись функции в бинарный поток, точек: {}", function.getCount());
        try (DataOutputStream stream = new DataOutputStream(outputStream)) {
            Point[] points = TabulatedFunctionOperationService.asPoints(function);
            stream.writeInt(function.getCount());
            for (Point point : points) {
                stream.writeDouble(point.x);
                stream.writeDouble(point.y);
            }
            stream.flush();
            logger.debug("Функция записана в бинарный поток");
        }
    }
    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) throws IOException {
        logger.debug("Запись функции в текстовый поток, точек: {}", function.getCount());
        PrintWriter printWriter = new PrintWriter(writer);

        printWriter.println(function.getCount());

        for (Point point : function) {
            printWriter.printf("%f %f\n", point.x, point.y);
        }

        printWriter.flush();
        logger.debug("Функция записана в текстовый поток");
    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader, TabulatedFunctionFactory factory)
            throws IOException {

        try {
            String countLine = reader.readLine();
            if (countLine == null) {
                logger.error("Попытка чтения из пустого файла");
                throw new IOException("Файл пустой");
            }

            int count = Integer.parseInt(countLine.trim());
            logger.debug("Чтение функции из текстового потока, точек: {}", count);


            double[] xValues = new double[count];
            double[] yValues = new double[count];

            NumberFormat numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

            for (int i = 0; i < count; i++) {
                String line = reader.readLine();
                if (line == null) {
                    logger.error("Неожиданный конец файла на точке {}", i);
                    throw new IOException("Неожиданный конец в файле");
                }

                String[] parts = line.split(" ");
                if (parts.length != 2) {
                    logger.error("Некорректный формат строки: {}", line);
                    throw new IOException("Неправильные данные в строке: " + line);
                }

                try {
                    xValues[i] = numberFormat.parse(parts[0].trim()).doubleValue();
                    yValues[i] = numberFormat.parse(parts[1].trim()).doubleValue();
                } catch (ParseException e) {
                    logger.error("Ошибка парсинга чисел в строке: {}", line, e);
                    throw new IOException("Ошибка парсинга чисел в строке: " + line, e);
                }
            }

            logger.debug("Функция прочитана из текстового потока");
            return factory.create(xValues, yValues);
        } catch(NumberFormatException e) {
            logger.error("Некорректный формат числа", e);
            throw new IOException("Некорректный формат числа", e);
        }
    }

    public static TabulatedFunction readTabulatedFunction(BufferedInputStream inputStream, TabulatedFunctionFactory factory) throws IOException {
        // Оборачиваем в DataInputStream для чтения примитивов
        logger.debug("Чтение функции из бинарного потока");
        try (DataInputStream dataInputStream = new DataInputStream(inputStream)) {

            int count = dataInputStream.readInt();

            double[] xValues = new double[count];
            double[] yValues = new double[count];

            for (int i = 0; i < count; i++) {
                xValues[i] = dataInputStream.readDouble(); // читаем x
                yValues[i] = dataInputStream.readDouble(); // читаем y
            }

            // Используем фабрику для создания функции
            logger.debug("Функция прочитана из бинарного потока");
            return factory.create(xValues, yValues);
        }
    }
    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        // Создаём поток для сериализации
        logger.debug("Сериализация функции, точек: {}", function.getCount());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);
        // Сериализуем функцию
        objectOutputStream.writeObject(function);
        // Поток не закрыт, все данные перекидываются с flush
        objectOutputStream.flush();
        logger.debug("Функция сериализована");
    }

    public static TabulatedFunction deserialize(BufferedInputStream stream)
            throws IOException, ClassNotFoundException {
        logger.debug("Десериализация функции");
        // Оборачиваем BufferedInputStream в ObjectInputStream для десериализации
        ObjectInputStream objectInputStream = new ObjectInputStream(stream);
        // Читаем объект и приводим к типу TabulatedFunction;
        TabulatedFunction function = (TabulatedFunction) objectInputStream.readObject();

        logger.debug("Функция десериализована, точек: {}", function.getCount());
        return function;
    }
}
