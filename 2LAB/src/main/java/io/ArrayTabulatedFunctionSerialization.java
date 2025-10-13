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

            // Создаём оператор дифференцирования с шагом 0.001
            TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator ();

            // Вычисляем первую производную
            MathFunction derived1 = operator.derive(originalFunction);
            if (!(derived1 instanceof TabulatedFunction)) {
                throw new IllegalStateException("Первая производная не реализует TabulatedFunction: " + derived1.getClass().getName());
            }
            TabulatedFunction firstDerivative = (TabulatedFunction) derived1;

            // Вычисляем вторую производную
            MathFunction derived2 = operator.derive(firstDerivative);
            if (!(derived2 instanceof TabulatedFunction)) {
                throw new IllegalStateException("Вторая производная не реализует TabulatedFunction: " + derived2.getClass().getName());
            }
            TabulatedFunction secondDerivative = (TabulatedFunction) derived2;

            // Сериализуем все три функции в поток
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
            TabulatedFunction firstDerivative = FunctionsIO.deserialize(bufferedIn);
            TabulatedFunction secondDerivative = FunctionsIO.deserialize(bufferedIn);

            System.out.println("Функция 1: " + originalFunction.toString());
            System.out.println("Функция 2: " + firstDerivative.toString());
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
