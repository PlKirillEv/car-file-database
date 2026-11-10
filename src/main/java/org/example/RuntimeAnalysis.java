package org.example;

import org.example.database.FileDatabase;
import org.example.model.Car;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class RuntimeAnalysis {

    private static final int[] SIZES = {100, 500, 1000};

    private static final int SEARCH_REPEATS = 500;
    private static final int ADD_REPEATS = 100;
    private static final int DELETE_REPEATS = 100;

    public static void main(String[] args) throws IOException {

        System.out.println(
                "N;Add_ID (ms);Find_ID (ms);Search_Brand (ms);Search_Model (ms);"
                        + "Delete_ID (ms);Delete_Brand (ms)"
        );

        for (int size : SIZES) {
            runTest(size);
        }
    }

    private static void runTest(int size) throws IOException {

        FileDatabase searchDb = createDatabase("search_" + size, size);
        FileDatabase addDb = createDatabase("add_" + size, size);
        FileDatabase deleteDb = createDatabase("delete_" + size, size);

        double findIdTime = measureFindById(searchDb, size);
        double searchBrandTime = measureSearch(searchDb, "brand", "Brand_0");
        double searchModelTime = measureSearch(searchDb, "model", "Model_0");
        double addTime = measureAdd(addDb, size);
        double deleteIdTime = measureDeleteById(deleteDb, size);

        FileDatabase deleteBrandDb = createDatabase("delete_brand_" + size, size);
        double deleteBrandTime = measureDeleteByBrand(deleteBrandDb);

        // Вывод с округлением до 5 знаков после запятой (для удобной вставки в Excel)
        System.out.printf(Locale.US,
                "%d;%.5f;%.5f;%.5f;%.5f;%.5f;%.5f%n",
                size, addTime, findIdTime, searchBrandTime,
                searchModelTime, deleteIdTime, deleteBrandTime
        );
    }

    private static FileDatabase createDatabase(String name, int count) throws IOException {
        Path path = Path.of("data", "benchmark", name + ".dat");
        FileDatabase db = new FileDatabase(path);
        db.create();

        for (int i = 0; i < count; i++) {
            db.add(createCar(i));
        }

        return db;
    }

    private static Car createCar(int id) {
        return new Car(
                id,
                "Brand_" + (id % 10),
                "Model_" + (id % 10),
                2000 + (id % 25),
                1000000.0 + id,
                id % 2 == 0
        );
    }

    // Возвращает миллисекунды
    private static double measureAdd(FileDatabase db, int startId) throws IOException {
        long totalTime = 0;

        for (int i = 0; i < ADD_REPEATS; i++) {
            Car car = createCar(startId + i + 100000);

            long start = System.nanoTime();
            db.add(car);
            long end = System.nanoTime();

            totalTime += (end - start);
        }

        return (totalTime / (double) ADD_REPEATS) / 1_000_000.0;
    }

    // Возвращает миллисекунды
    private static double measureFindById(FileDatabase db, int size) throws IOException {
        long totalTime = 0;

        for (int i = 0; i < SEARCH_REPEATS; i++) {
            int id = i % size;

            long start = System.nanoTime();
            db.findById(id);
            long end = System.nanoTime();

            totalTime += (end - start);
        }

        return (totalTime / (double) SEARCH_REPEATS) / 1_000_000.0;
    }

    // Возвращает миллисекунды
    private static double measureSearch(FileDatabase db, String field, String value) throws IOException {
        long totalTime = 0;

        for (int i = 0; i < SEARCH_REPEATS; i++) {
            long start = System.nanoTime();
            List<Car> result = db.searchByField(field, value);
            long end = System.nanoTime();

            totalTime += (end - start);
        }

        return (totalTime / (double) SEARCH_REPEATS) / 1_000_000.0;
    }

    // Возвращает миллисекунды
    private static double measureDeleteById(FileDatabase db, int size) throws IOException {
        long totalTime = 0;

        for (int i = 0; i < DELETE_REPEATS; i++) {
            int id = i % size;

            long start = System.nanoTime();
            db.deleteById(id);
            long end = System.nanoTime();

            totalTime += (end - start);
        }

        return (totalTime / (double) DELETE_REPEATS) / 1_000_000.0;
    }

    // Возвращает миллисекунды
    private static double measureDeleteByBrand(FileDatabase db) throws IOException {
        long start = System.nanoTime();
        db.deleteByField("brand", "Brand_0");
        long end = System.nanoTime();

        return (end - start) / 1_000_000.0;
    }
}
