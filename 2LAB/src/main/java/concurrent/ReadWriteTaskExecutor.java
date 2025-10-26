package concurrent;

import functions.ConstantFunction;
import functions.LinkedListTabulatedFunction;

public class ReadWriteTaskExecutor {
    static void main(String[] args) {

        // Тождественная функция, равная отрицательному числу -1
        ConstantFunction constantFunction = new ConstantFunction(-1);

        // Передаем ее в LinkedListTabulatedFunction на интервале от 1 до 10000, с кол-вом точек 10000
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(constantFunction, 1, 10000, 10000);

        Thread threadReadTask = new Thread(new ReadTask(function));  // Поток исполнения ReadTask
        Thread threadWriteTask = new Thread(new WriteTask(function, 3));  // Поток исполнения WriteTask. Передается произвольное положительно число - 3

        threadReadTask.start();
        threadWriteTask.start();

    }
}
