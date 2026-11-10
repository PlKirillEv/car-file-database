package org.example.database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HashIndex {

    // Основной индекс: ID автомобиля -> позиция записи в файле
    private final Map<Integer, Long> index = new HashMap<>();

    // Вторичные индексы
    private final Map<String, Set<Integer>> brandIndex = new HashMap<>();
    private final Map<String, Set<Integer>> modelIndex = new HashMap<>();
    private final Map<Integer, Set<Integer>> yearIndex = new HashMap<>();
    private final Map<Double, Set<Integer>> priceIndex = new HashMap<>();
    private final Map<Boolean, Set<Integer>> accidentIndex = new HashMap<>();

    public void put(int id, long position) {
        index.put(id, position);
    }

    public Long get(int id) {
        return index.get(id);
    }

    public boolean contains(int id) {
        return index.containsKey(id);
    }

    public void remove(int id) {
        index.remove(id);
    }

    public void addBrand(String brand, int id) {
        addToIndex(brandIndex, normalize(brand), id);
    }

    public void removeBrand(String brand, int id) {
        removeFromIndex(brandIndex, normalize(brand), id);
    }

    public Set<Integer> getByBrand(String brand) {
        return getFromIndex(brandIndex, normalize(brand));
    }

    public void addModel(String model, int id) {
        addToIndex(modelIndex, normalize(model), id);
    }

    public void removeModel(String model, int id) {
        removeFromIndex(modelIndex, normalize(model), id);
    }

    public Set<Integer> getByModel(String model) {
        return getFromIndex(modelIndex, normalize(model));
    }

    public void addYear(int year, int id) {
        addToIndex(yearIndex, year, id);
    }

    public void removeYear(int year, int id) {
        removeFromIndex(yearIndex, year, id);
    }

    public Set<Integer> getByYear(int year) {
        return getFromIndex(yearIndex, year);
    }

    public void addPrice(double price, int id) {
        addToIndex(priceIndex, price, id);
    }

    public void removePrice(double price, int id) {
        removeFromIndex(priceIndex, price, id);
    }

    public Set<Integer> getByPrice(double price) {
        return getFromIndex(priceIndex, price);
    }

    public void addAccident(boolean hadAccident, int id) {
        addToIndex(accidentIndex, hadAccident, id);
    }

    public void removeAccident(boolean hadAccident, int id) {
        removeFromIndex(accidentIndex, hadAccident, id);
    }

    public Set<Integer> getByAccident(boolean hadAccident) {
        return getFromIndex(accidentIndex, hadAccident);
    }

    private <K> void addToIndex(
            Map<K, Set<Integer>> map,
            K key,
            int id
    ) {
        map.computeIfAbsent(key, k -> new HashSet<>()).add(id);
    }

    private <K> void removeFromIndex(
            Map<K, Set<Integer>> map,
            K key,
            int id
    ) {
        Set<Integer> ids = map.get(key);

        if (ids != null) {
            ids.remove(id);

            if (ids.isEmpty()) {
                map.remove(key);
            }
        }
    }

    private <K> Set<Integer> getFromIndex(
            Map<K, Set<Integer>> map,
            K key
    ) {
        Set<Integer> ids = map.get(key);

        if (ids == null) {
            return new HashSet<>();
        }

        return new HashSet<>(ids);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public void clear() {
        index.clear();
        brandIndex.clear();
        modelIndex.clear();
        yearIndex.clear();
        priceIndex.clear();
        accidentIndex.clear();
    }

    public int size() {
        return index.size();
    }
}