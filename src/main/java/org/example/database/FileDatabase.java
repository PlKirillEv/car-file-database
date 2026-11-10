package org.example.database;

import org.example.model.Car;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FileDatabase {

    private static final int TEXT_SIZE = 40;
    private static final int RECORD_SIZE = 98;

    private final Path path;
    private final HashIndex index = new HashIndex();

    public FileDatabase(Path path) {
        this.path = path;
    }

    // Создание базы данных
    public void create() throws IOException {
        Path parent = path.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (RandomAccessFile file =
                     new RandomAccessFile(path.toFile(), "rw")) {
            file.setLength(0);
        }

        index.clear();
    }

    // Открытие базы и построение всех индексов
    public void open() throws IOException {
        index.clear();

        try (RandomAccessFile file =
                     new RandomAccessFile(path.toFile(), "r")) {

            if (file.length() % RECORD_SIZE != 0) {
                throw new IOException(
                        "Файл базы повреждён или имеет старый формат"
                );
            }

            for (long pos = 0; pos < file.length(); pos += RECORD_SIZE) {
                file.seek(pos);

                int id = file.readInt();
                String brand = readText(file);
                String model = readText(file);
                int year = file.readInt();
                double price = file.readDouble();
                boolean hadAccident = file.readBoolean();
                boolean active = file.readBoolean();

                if (active) {
                    if (index.contains(id)) {
                        throw new IOException(
                                "Обнаружен повторяющийся ID: " + id
                        );
                    }

                    index.put(id, pos);
                    addToIndexes(
                            id, brand, model, year, price, hadAccident
                    );
                }
            }
        }
    }

    // Добавление автомобиля
    public void add(Car car) throws IOException {
        validate(car);

        if (index.contains(car.getId())) {
            throw new IllegalArgumentException(
                    "Автомобиль с таким ID уже существует"
            );
        }

        long pos;

        try (RandomAccessFile file =
                     new RandomAccessFile(path.toFile(), "rw")) {

            pos = file.length();

            if (pos % RECORD_SIZE != 0) {
                throw new IOException("Файл базы повреждён");
            }

            file.seek(pos);
            writeCar(file, car);
        }

        index.put(car.getId(), pos);

        addToIndexes(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getYear(),
                car.getPrice(),
                car.isHadAccident()
        );
    }

    // Поиск по ID
    public Car findById(int id) throws IOException {
        Long pos = index.get(id);

        return pos == null ? null : read(pos);
    }

    // Удаление по ID
    public void deleteById(int id) throws IOException {
        Long pos = index.get(id);

        if (pos == null) {
            throw new IllegalArgumentException(
                    "Автомобиль не найден"
            );
        }

        Car car = read(pos);

        try (RandomAccessFile file =
                     new RandomAccessFile(path.toFile(), "rw")) {

            file.seek(pos + RECORD_SIZE - 1);
            file.writeBoolean(false);
        }

        index.remove(id);

        removeFromIndexes(
                id,
                car.getBrand(),
                car.getModel(),
                car.getYear(),
                car.getPrice(),
                car.isHadAccident()
        );
    }

    // Редактирование автомобиля
    public void update(Car car) throws IOException {
        validate(car);

        Long pos = index.get(car.getId());

        if (pos == null) {
            throw new IllegalArgumentException(
                    "Автомобиль не найден"
            );
        }

        Car old = read(pos);

        try (RandomAccessFile file =
                     new RandomAccessFile(path.toFile(), "rw")) {

            file.seek(pos);
            writeCar(file, car);
        }

        removeFromIndexes(
                old.getId(),
                old.getBrand(),
                old.getModel(),
                old.getYear(),
                old.getPrice(),
                old.isHadAccident()
        );

        addToIndexes(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getYear(),
                car.getPrice(),
                car.isHadAccident()
        );
    }

    // Получение всех автомобилей для GUI
    public List<Car> getAll() throws IOException {
        List<Car> cars = new ArrayList<>();

        try (RandomAccessFile file =
                     new RandomAccessFile(path.toFile(), "r")) {

            if (file.length() % RECORD_SIZE != 0) {
                throw new IOException("Файл базы повреждён");
            }

            for (long pos = 0; pos < file.length(); pos += RECORD_SIZE) {
                Car car = read(pos);

                if (car != null) {
                    cars.add(car);
                }
            }
        }

        return cars;
    }

    // Чтение одной записи
    public Car read(long pos) throws IOException {
        try (RandomAccessFile file =
                     new RandomAccessFile(path.toFile(), "r")) {

            if (pos < 0
                    || pos % RECORD_SIZE != 0
                    || pos + RECORD_SIZE > file.length()) {

                throw new IOException(
                        "Некорректная позиция записи"
                );
            }

            file.seek(pos);

            int id = file.readInt();
            String brand = readText(file);
            String model = readText(file);
            int year = file.readInt();
            double price = file.readDouble();
            boolean hadAccident = file.readBoolean();
            boolean active = file.readBoolean();

            if (!active) {
                return null;
            }

            return new Car(
                    id, brand, model, year, price, hadAccident
            );
        }
    }

    // Количество записей в файле
    public long getRecordCount() throws IOException {
        try (RandomAccessFile file =
                     new RandomAccessFile(path.toFile(), "r")) {

            if (file.length() % RECORD_SIZE != 0) {
                throw new IOException("Файл базы повреждён");
            }

            return file.length() / RECORD_SIZE;
        }
    }

    // Поиск по любому полю через индекс
    public List<Car> searchByField(
            String field,
            String value
    ) throws IOException {

        String normalizedField = normalizeField(field);
        Set<Integer> ids = getIdsByField(normalizedField, value);

        List<Car> result = new ArrayList<>();

        for (int id : ids) {
            Car car = findById(id);

            if (car != null) {
                result.add(car);
            }
        }

        return result;
    }

    // Удаление по любому полю через индекс
    public int deleteByField(
            String field,
            String value
    ) throws IOException {

        String normalizedField = normalizeField(field);

        Set<Integer> ids = getIdsByField(
                normalizedField, value
        );

        int deleted = 0;

        // Копия множества позволяет безопасно менять индексы
        for (int id : ids) {
            if (index.contains(id)) {
                deleteById(id);
                deleted++;
            }
        }

        return deleted;
    }

    // Получение ID из нужного индекса
    private Set<Integer> getIdsByField(
            String field,
            String value
    ) {

        String v = value.trim();

        switch (field) {
            case "id":
                try {
                    int id = Integer.parseInt(v);

                    if (index.contains(id)) {
                        return Set.of(id);
                    }

                    return Set.of();

                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Некорректный ID"
                    );
                }

            case "brand":
                return index.getByBrand(v);

            case "model":
                return index.getByModel(v);

            case "year":
                try {
                    return index.getByYear(
                            Integer.parseInt(v)
                    );

                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Некорректный год"
                    );
                }

            case "price":
                try {
                    double price = Double.parseDouble(
                            v.replace(',', '.')
                    );

                    if (!Double.isFinite(price)) {
                        throw new NumberFormatException();
                    }

                    return index.getByPrice(price);

                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Некорректная цена"
                    );
                }

            case "hadaccident":
                if (v.equalsIgnoreCase("да")
                        || v.equalsIgnoreCase("true")) {

                    return index.getByAccident(true);
                }

                if (v.equalsIgnoreCase("нет")
                        || v.equalsIgnoreCase("false")) {

                    return index.getByAccident(false);
                }

                throw new IllegalArgumentException(
                        "Введите «да» или «нет» для поля ДТП"
                );

            default:
                throw new IllegalArgumentException(
                        "Неизвестное поле: " + field
                );
        }
    }

    // Проверка и нормализация названия поля
    private String normalizeField(String field) {
        if (field == null) {
            throw new IllegalArgumentException(
                    "Поле не указано"
            );
        }

        String normalized = field.trim().toLowerCase(Locale.ROOT);

        switch (normalized) {
            case "id":
            case "brand":
            case "model":
            case "year":
            case "price":
            case "hadaccident":
                return normalized;

            default:
                throw new IllegalArgumentException(
                        "Неизвестное поле: " + field
                );
        }
    }

    // Добавление автомобиля во все вторичные индексы
    private void addToIndexes(
            int id,
            String brand,
            String model,
            int year,
            double price,
            boolean hadAccident
    ) {
        index.addBrand(brand, id);
        index.addModel(model, id);
        index.addYear(year, id);
        index.addPrice(price, id);
        index.addAccident(hadAccident, id);
    }

    // Удаление автомобиля из всех вторичных индексов
    private void removeFromIndexes(
            int id,
            String brand,
            String model,
            int year,
            double price,
            boolean hadAccident
    ) {
        index.removeBrand(brand, id);
        index.removeModel(model, id);
        index.removeYear(year, id);
        index.removePrice(price, id);
        index.removeAccident(hadAccident, id);
    }

    // Проверка автомобиля
    private void validate(Car car) {
        if (car.getBrand() == null
                || car.getModel() == null
                || car.getBrand().isBlank()
                || car.getModel().isBlank()) {

            throw new IllegalArgumentException(
                    "Марка и модель обязательны"
            );
        }

        if (car.getYear() < 1886 || car.getYear() > 2100) {
            throw new IllegalArgumentException(
                    "Проверьте год выпуска"
            );
        }

        if (!Double.isFinite(car.getPrice())
                || car.getPrice() < 0) {

            throw new IllegalArgumentException(
                    "Цена не может быть отрицательной"
            );
        }

        if (car.getBrand().getBytes(StandardCharsets.UTF_8).length
                > TEXT_SIZE
                || car.getModel().getBytes(StandardCharsets.UTF_8).length
                > TEXT_SIZE) {

            throw new IllegalArgumentException(
                    "Марка и модель должны занимать не более 40 байт UTF-8"
            );
        }
    }

    // Запись автомобиля в файл
    private void writeCar(
            RandomAccessFile file,
            Car car
    ) throws IOException {

        file.writeInt(car.getId());
        writeText(file, car.getBrand());
        writeText(file, car.getModel());
        file.writeInt(car.getYear());
        file.writeDouble(car.getPrice());
        file.writeBoolean(car.isHadAccident());
        file.writeBoolean(true);
    }

    // Запись строки фиксированного размера
    private void writeText(
            RandomAccessFile file,
            String value
    ) throws IOException {

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        file.write(bytes);

        for (int i = bytes.length; i < TEXT_SIZE; i++) {
            file.writeByte(0);
        }
    }

    // Чтение строки фиксированного размера
    private String readText(
            RandomAccessFile file
    ) throws IOException {

        byte[] bytes = new byte[TEXT_SIZE];

        file.readFully(bytes);

        int length = 0;

        while (length < bytes.length
                && bytes[length] != 0) {

            length++;
        }

        return new String(
                bytes,
                0,
                length,
                StandardCharsets.UTF_8
        );
    }

    // Создание резервной копии
    public void backup(Path backupPath) throws IOException {
        Path parent = backupPath.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.copy(
                path,
                backupPath,
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    // Восстановление резервной копии
    public void restore(Path backupPath) throws IOException {
        Files.copy(
                backupPath,
                path,
                StandardCopyOption.REPLACE_EXISTING
        );

        open();
    }

    // Очистка базы
    public void clear() throws IOException {
        create();
    }

    // Удаление файла базы
    public void deleteDatabase() throws IOException {
        Files.deleteIfExists(path);
        index.clear();
    }

    // Поиск по марке
    public List<Car> searchByBrand(
            String brand
    ) throws IOException {

        return searchByField("brand", brand);
    }
}