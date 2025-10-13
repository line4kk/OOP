package io;

import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
import functions.MathFunction;
import operations.MiddleSteppingDifferentialOperator;
import operations.TabulatedDifferentialOperator;

import java.io.*;

public class ArrayTabulatedFunctionSerialization {
    public static void main(String[] args) {
        String filePath = "output/serialized array functions.bin";

        // Сериализация
        System.out.println("Сериализация начата");
        try (
                FileOutputStream fileOut = new FileOutputStream(filePath);
                BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOut)
        ) {
            // Создаём функцию y = sqrt(x)
            double[] xValues = {1.0, 4.0, 9.0, 16.0, 25.0};
            double[] yValues = {1.0, 2.0, 3.0, 4.0, 5.0};
            ArrayTabulatedFunction originalFunction = new ArrayTabulatedFunction(xValues, yValues);

            TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator ();

            // Вычисляем первую производную
            MathFunction derived1 = operator.derive(originalFunction);
            TabulatedFunction firstDerivative = (TabulatedFunction)derived1;

            // Вычисляем вторую производную
            MathFunction derived2 = operator.derive(firstDerivative);
            TabulatedFunction secondDerivative = (TabulatedFunction)derived2;

            // Сериализуем все три функции
            FunctionsIO.serialize(bufferedOut, originalFunction);
            FunctionsIO.serialize(bufferedOut, firstDerivative);
            FunctionsIO.serialize(bufferedOut, secondDerivative);

            System.out.println("Сериализация закончена");

        } catch (IOException e) {
            System.err.println("Ошибка при сериализации:");
            e.printStackTrace();
        }

        // Десериализация
        System.out.println("Десериализация начата");

        try (
                FileInputStream fileIn = new FileInputStream(filePath);
                BufferedInputStream bufferedIn = new BufferedInputStream(fileIn)
        ) {
            TabulatedFunction originalFunction = FunctionsIO.deserialize(bufferedIn);
            System.out.println("Функция 1: " + originalFunction.toString());

            TabulatedFunction firstDerivative = FunctionsIO.deserialize(bufferedIn);
            System.out.println("Функция 2: " + firstDerivative.toString());

            TabulatedFunction secondDerivative = FunctionsIO.deserialize(bufferedIn);
            System.out.println("Функция 3: " + secondDerivative.toString());

            System.out.println("Десериализация завершена");

        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода при десериализации:");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Не найден класс при десериализации");
            e.printStackTrace();
        }
    }
}
