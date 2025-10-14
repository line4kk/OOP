package io;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.factory.LinkedListTabulatedFunctionFactory;
import operations.TabulatedDifferentialOperator;

import java.io.*;

public class LinkedListTabulatedFunctionSerialization {

    static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("output/serialized linked list functions.bin");
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            // Создаем исходную функцию
            double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
            double[] yValues = {2.0, 4.0, 6.0, 8.0, 10.0};
            TabulatedFunction originalFunction = new LinkedListTabulatedFunction(xValues, yValues);
            LinkedListTabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();

            // Находим первую и вторую производные
            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator(factory);
            TabulatedFunction firstDerivative = differentialOperator.derive(originalFunction);
            TabulatedFunction secondDerivative = differentialOperator.derive(firstDerivative);

            FunctionsIO.serialize(bos, originalFunction);
            FunctionsIO.serialize(bos, firstDerivative);
            FunctionsIO.serialize(bos, secondDerivative);

            System.out.println("Функции успешно сериализованы в файл");

        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileInputStream fis = new FileInputStream("output/serialized linked list functions.bin");
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            TabulatedFunction deserializedOriginal = FunctionsIO.deserialize(bis);
            TabulatedFunction deserializedFirstDerivative = FunctionsIO.deserialize(bis);
            TabulatedFunction deserializedSecondDerivative = FunctionsIO.deserialize(bis);

            // Выводим функции в консоль
            System.out.println("\nИсходная функция:");
            System.out.println(deserializedOriginal.toString());

            System.out.println("\nПервая производная:");
            System.out.println(deserializedFirstDerivative.toString());

            System.out.println("\nВторая производная:");
            System.out.println(deserializedSecondDerivative.toString());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}