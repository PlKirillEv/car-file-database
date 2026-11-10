package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import org.example.database.FileDatabase;
import org.example.model.Car;

import java.nio.file.Path;
import java.util.Locale;

public class Main extends Application {

    private final FileDatabase database =
            new FileDatabase(Path.of("data", "cars.dat"));

    private final TableView<Car> table = new TableView<>();
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        createTable();

        Button createButton = new Button("Создать БД");
        Button openButton = new Button("Открыть БД");
        Button addButton = new Button("Добавить");
        Button searchButton = new Button("Найти по ID");
        Button searchFieldButton = new Button("Поиск по полю");
        Button editButton = new Button("Редактировать");
        Button deleteButton = new Button("Удалить");
        Button deleteFieldButton = new Button("Удалить по полю");
        Button backupButton = new Button("Создать копию");
        Button restoreButton = new Button("Восстановить копию");
        Button clearButton = new Button("Очистить БД");
        Button deleteDatabaseButton = new Button("Удалить файл БД");
        Button exportButton = new Button("Экспорт в Excel");

        createButton.setOnAction(e -> createDatabase());
        openButton.setOnAction(e -> openDatabase());
        addButton.setOnAction(e -> addCar());
        searchButton.setOnAction(e -> searchCar());
        searchFieldButton.setOnAction(e -> searchByField());
        editButton.setOnAction(e -> editCar());
        deleteButton.setOnAction(e -> deleteCar());
        deleteFieldButton.setOnAction(e -> deleteByField());
        backupButton.setOnAction(e -> backupDatabase());
        restoreButton.setOnAction(e -> restoreDatabase());
        clearButton.setOnAction(e -> clearDatabase());
        deleteDatabaseButton.setOnAction(e -> deleteDatabase());
        exportButton.setOnAction(e -> exportExcel());

        Button[] allButtons = {
                createButton, openButton, addButton, searchButton,
                searchFieldButton, editButton, deleteButton,
                deleteFieldButton, backupButton, restoreButton,
                clearButton, deleteDatabaseButton, exportButton
        };

        for (Button button : allButtons) {
            button.setStyle(
                    "-fx-background-color: #2563EB;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 10 14 10 14;"
            );
            button.setMaxWidth(Double.MAX_VALUE);
            button.setPrefHeight(38);
        }

        String dangerStyle =
                "-fx-background-color: #DC2626;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10 14 10 14;";

        clearButton.setStyle(dangerStyle);
        deleteDatabaseButton.setStyle(dangerStyle);

        Label title = new Label("АвтоБаза");
        title.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #172554;"
        );

        Label subtitle = new Label(
                "Управление базой данных автомобилей"
        );
        subtitle.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #64748B;"
        );

        VBox header = new VBox(5, title, subtitle);
        header.setPadding(new Insets(5, 5, 15, 5));

        Label databaseLabel = new Label("БАЗА ДАННЫХ");
        databaseLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #64748B;"
        );

        VBox databaseCard = new VBox(
                12,
                databaseLabel,
                new HBox(8, createButton, openButton),
                exportButton
        );

        Label recordsLabel = new Label("АВТОМОБИЛИ");
        recordsLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #64748B;"
        );

        HBox recordRow1 = new HBox(
                8, addButton, searchButton, searchFieldButton
        );

        HBox recordRow2 = new HBox(
                8, editButton, deleteButton, deleteFieldButton
        );

        VBox recordsCard = new VBox(
                12, recordsLabel, recordRow1, recordRow2
        );

        Label serviceLabel = new Label("КОПИИ И ОБСЛУЖИВАНИЕ");
        serviceLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #64748B;"
        );

        HBox backupRow = new HBox(8, backupButton, restoreButton);
        HBox maintenanceRow = new HBox(
                8, clearButton, deleteDatabaseButton
        );

        VBox serviceCard = new VBox(
                12, serviceLabel, backupRow, maintenanceRow
        );

        String cardStyle =
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-radius: 12;";

        VBox[] cards = {databaseCard, recordsCard, serviceCard};

        for (VBox card : cards) {
            card.setStyle(cardStyle);
            card.setPadding(new Insets(16));
            card.setSpacing(12);
        }

        databaseCard.setMinWidth(240);
        recordsCard.setMinWidth(450);
        serviceCard.setMinWidth(300);

        HBox actions = new HBox(
                12, databaseCard, recordsCard, serviceCard
        );

        actions.setFillHeight(true);

        VBox top = new VBox(12, header, actions);
        top.setPadding(new Insets(10, 10, 18, 10));

        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 13px;"
        );

        table.setFixedCellSize(42);
        table.setPlaceholder(new Label("Нет данных для отображения"));

        Label tableTitle = new Label("Список автомобилей");
        tableTitle.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #172554;"
        );

        Label tableHint = new Label(
                "Для редактирования или удаления выбери строку"
        );
        tableHint.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #64748B;"
        );

        VBox tableHeader = new VBox(4, tableTitle, tableHint);
        tableHeader.setPadding(new Insets(0, 5, 12, 5));

        VBox center = new VBox(10, tableHeader, table);
        center.setPadding(new Insets(0, 15, 15, 15));
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(center);
        root.setStyle("-fx-background-color: #F1F5F9;");

        Scene scene = new Scene(root, 1150, 750);

        stage.setTitle("АвтоБаза — управление автомобилями");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setMaximized(true);

        try {
            if (java.nio.file.Files.exists(
                    Path.of("data", "cars.dat"))) {
                database.open();
                refreshTable();
            }
        } catch (Exception ex) {
            error("Не удалось открыть базу данных: " + ex.getMessage());
        }

        stage.show();
    }

    private void createTable() {
        TableColumn<Car, String> id = new TableColumn<>("ID");
        id.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(c.getValue().getId())));

        TableColumn<Car, String> brand = new TableColumn<>("Марка");
        brand.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getBrand()));

        TableColumn<Car, String> model = new TableColumn<>("Модель");
        model.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getModel()));

        TableColumn<Car, String> year = new TableColumn<>("Год");
        year.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(c.getValue().getYear())));

        TableColumn<Car, String> price = new TableColumn<>("Цена");
        price.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        java.text.NumberFormat.getNumberInstance(
                                Locale.FRANCE
                        ).format(c.getValue().getPrice())));

        TableColumn<Car, String> accident =
                new TableColumn<>("Были ДТП?");
        accident.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().isHadAccident() ? "Да" : "Нет"));

        table.getColumns().setAll(
                id, brand, model, year, price, accident
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void createDatabase() {
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Создать новую базу? Существующие данные будут удалены.",
                ButtonType.YES, ButtonType.NO);

        confirmation.showAndWait().ifPresent(answer -> {
            if (answer == ButtonType.YES) {
                try {
                    database.create();
                    refreshTable();
                    info("База данных создана");
                } catch (Exception ex) {
                    error(ex.getMessage());
                }
            }
        });
    }

    private void openDatabase() {
        try {
            database.open();
            refreshTable();
            info("База данных открыта");
        } catch (Exception ex) {
            error("Не удалось открыть базу. Сначала создай её.\n"
                    + ex.getMessage());
        }
    }

    private void addCar() {
        try {
            Car car = carForm(null);

            if (car != null) {
                database.add(car);
                refreshTable();
                info("Автомобиль добавлен");
            }
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void searchCar() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Поиск");
        dialog.setHeaderText("Введите ID автомобиля");

        dialog.showAndWait().ifPresent(value -> {
            try {
                int id = Integer.parseInt(value.trim());
                Car car = database.findById(id);

                if (car == null) {
                    info("Автомобиль не найден");
                } else {
                    for (Car item : table.getItems()) {
                        if (item.getId() == id) {
                            table.getSelectionModel().select(item);
                            table.scrollTo(item);
                            break;
                        }
                    }

                    info("Найден: " + car.getBrand() + " " + car.getModel());
                }
            } catch (Exception ex) {
                error("Введите корректный ID");
            }
        });
    }

    private void editCar() {
        Car selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            info("Сначала выбери автомобиль в таблице");
            return;
        }

        try {
            Car updated = carForm(selected);

            if (updated != null) {
                database.update(updated);
                refreshTable();
                info("Данные обновлены");
            }
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void deleteCar() {
        Car selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            info("Сначала выбери автомобиль в таблице");
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Удалить автомобиль " + selected.getBrand()
                        + " " + selected.getModel() + "?",
                ButtonType.YES, ButtonType.NO);

        confirmation.showAndWait().ifPresent(answer -> {
            if (answer == ButtonType.YES) {
                try {
                    database.deleteById(selected.getId());
                    refreshTable();
                    info("Автомобиль удалён");
                } catch (Exception ex) {
                    error(ex.getMessage());
                }
            }
        });
    }

    private Car carForm(Car old) {
        int id;

        if (old == null) {
            TextInputDialog idDialog = new TextInputDialog();
            idDialog.setTitle("Данные автомобиля");
            idDialog.setHeaderText("ID автомобиля");

            var result = idDialog.showAndWait();
            if (result.isEmpty()) return null;

            id = Integer.parseInt(result.get().trim());
        } else {
            id = old.getId();
        }

        TextInputDialog brandDialog = new TextInputDialog(
                old == null ? "" : old.getBrand());
        brandDialog.setTitle("Данные автомобиля");
        brandDialog.setHeaderText("Марка");

        var brandResult = brandDialog.showAndWait();
        if (brandResult.isEmpty()) return null;

        TextInputDialog modelDialog = new TextInputDialog(
                old == null ? "" : old.getModel());
        modelDialog.setTitle("Данные автомобиля");
        modelDialog.setHeaderText("Модель");

        var modelResult = modelDialog.showAndWait();
        if (modelResult.isEmpty()) return null;

        TextInputDialog yearDialog = new TextInputDialog(
                old == null ? "" : String.valueOf(old.getYear()));
        yearDialog.setTitle("Данные автомобиля");
        yearDialog.setHeaderText("Год выпуска");

        var yearResult = yearDialog.showAndWait();
        if (yearResult.isEmpty()) return null;

        TextInputDialog priceDialog = new TextInputDialog(
                old == null ? "" : String.valueOf(old.getPrice()));
        priceDialog.setTitle("Данные автомобиля");
        priceDialog.setHeaderText("Цена");

        var priceResult = priceDialog.showAndWait();
        if (priceResult.isEmpty()) return null;

        ChoiceDialog<String> accidentDialog = new ChoiceDialog<>(
                old != null && old.isHadAccident() ? "Да" : "Нет",
                "Да", "Нет"
        );
        accidentDialog.setTitle("Данные автомобиля");
        accidentDialog.setHeaderText("Были ли ДТП?");

        var accidentResult = accidentDialog.showAndWait();
        if (accidentResult.isEmpty()) return null;

        boolean hadAccident = accidentResult.get().equals("Да");

        return new Car(
                id,
                brandResult.get().trim(),
                modelResult.get().trim(),
                Integer.parseInt(yearResult.get().trim()),
                Double.parseDouble(
                        priceResult.get().trim().replace(',', '.')),
                hadAccident
        );
    }

    private void searchByField() {
        ChoiceDialog<String> fieldDialog = new ChoiceDialog<>(
                "Марка", "ID", "Марка", "Модель", "Год", "Цена",
                "Были ДТП?");

        fieldDialog.setTitle("Поиск по полю");
        fieldDialog.setHeaderText("Выбери поле для поиска");

        fieldDialog.showAndWait().ifPresent(field -> {
            TextInputDialog valueDialog = new TextInputDialog();
            valueDialog.setTitle("Поиск по полю");
            valueDialog.setHeaderText(
                    field.equals("Были ДТП?")
                            ? "Введи «Да» или «Нет»"
                            : "Введи значение для поиска"
            );

            valueDialog.showAndWait().ifPresent(value -> {
                try {
                    String fieldName;

                    switch (field) {
                        case "ID": fieldName = "id"; break;
                        case "Марка": fieldName = "brand"; break;
                        case "Модель": fieldName = "model"; break;
                        case "Год": fieldName = "year"; break;
                        case "Цена": fieldName = "price"; break;
                        default: fieldName = "hadAccident";
                    }

                    var result = database.searchByField(fieldName, value.trim());

                    int limit = 1;

                    table.setItems(
                            FXCollections.observableArrayList(result));

                    info("Найдено записей: " + result.size());
                } catch (Exception ex) {
                    error(ex.getMessage());
                }
            });
        });
    }

    private void deleteByField() {
        ChoiceDialog<String> fieldDialog = new ChoiceDialog<>(
                "Марка", "ID", "Марка", "Модель", "Год", "Цена",
                "Были ДТП?");

        fieldDialog.setTitle("Удаление по полю");
        fieldDialog.setHeaderText("Выбери поле");

        fieldDialog.showAndWait().ifPresent(field -> {
            TextInputDialog valueDialog = new TextInputDialog();
            valueDialog.setTitle("Удаление по полю");
            valueDialog.setHeaderText(
                    field.equals("Были ДТП?")
                            ? "Введи «Да» или «Нет»"
                            : "Введи значение"
            );

            valueDialog.showAndWait().ifPresent(value -> {
                try {
                    String fieldName;

                    switch (field) {
                        case "ID": fieldName = "id"; break;
                        case "Марка": fieldName = "brand"; break;
                        case "Модель": fieldName = "model"; break;
                        case "Год": fieldName = "year"; break;
                        case "Цена": fieldName = "price"; break;
                        default: fieldName = "hadAccident";
                    }

                    Alert confirmation = new Alert(
                            Alert.AlertType.CONFIRMATION,
                            "Удалить все записи, где " + field
                                    + " = " + value + "?",
                            ButtonType.YES, ButtonType.NO);

                    confirmation.showAndWait().ifPresent(answer -> {
                        if (answer == ButtonType.YES) {
                            try {
                                int deleted = database.deleteByField(
                                        fieldName, value.trim());

                                refreshTable();
                                info("Удалено записей: " + deleted);
                            } catch (Exception ex) {
                                error(ex.getMessage());
                            }
                        }
                    });
                } catch (Exception ex) {
                    error(ex.getMessage());
                }
            });
        });
    }

    private void backupDatabase() {
        try {
            database.backup(Path.of("data", "cars-backup.dat"));
            info("Резервная копия создана");
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void restoreDatabase() {
        try {
            Path backupPath = Path.of("data", "cars-backup.dat");

            if (!java.nio.file.Files.exists(backupPath)) {
                error("Резервная копия не найдена");
                return;
            }

            database.restore(backupPath);
            refreshTable();
            info("База данных восстановлена");
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void clearDatabase() {
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Удалить все записи из базы?",
                ButtonType.YES, ButtonType.NO);

        confirmation.showAndWait().ifPresent(answer -> {
            if (answer == ButtonType.YES) {
                try {
                    database.clear();
                    refreshTable();
                    info("База очищена");
                } catch (Exception ex) {
                    error(ex.getMessage());
                }
            }
        });
    }

    private void deleteDatabase() {
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Полностью удалить файл базы данных?",
                ButtonType.YES, ButtonType.NO);

        confirmation.showAndWait().ifPresent(answer -> {
            if (answer == ButtonType.YES) {
                try {
                    database.deleteDatabase();
                    table.getItems().clear();
                    info("Файл базы удалён");
                } catch (Exception ex) {
                    error(ex.getMessage());
                }
            }
        });
    }

    private void exportExcel() {
        javafx.stage.FileChooser chooser =
                new javafx.stage.FileChooser();

        chooser.setTitle("Сохранить таблицу в Excel");
        chooser.setInitialFileName("cars.xlsx");
        chooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter(
                        "Excel (*.xlsx)", "*.xlsx"
                )
        );

        java.io.File file = chooser.showSaveDialog(stage);

        if (file == null) return;

        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new java.io.File(file.getAbsolutePath() + ".xlsx");
        }

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Автомобили");

            String[] headers = {
                    "ID", "Марка", "Модель", "Год выпуска",
                    "Цена", "Были ДТП?"
            };

            org.apache.poi.ss.usermodel.Row headerRow =
                    sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            var cars = database.getAll();
            int rowNumber = 1;

            for (Car car : cars) {
                var row = sheet.createRow(rowNumber++);

                row.createCell(0).setCellValue(car.getId());
                row.createCell(1).setCellValue(car.getBrand());
                row.createCell(2).setCellValue(car.getModel());
                row.createCell(3).setCellValue(car.getYear());
                row.createCell(4).setCellValue(car.getPrice());
                row.createCell(5).setCellValue(
                        car.isHadAccident() ? "Да" : "Нет");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (java.io.FileOutputStream out =
                         new java.io.FileOutputStream(file)) {
                workbook.write(out);
            }

            info("Экспорт завершён. Выгружено автомобилей: "
                    + cars.size() + "\nФайл: " + file.getAbsolutePath());

        } catch (Exception ex) {
            error("Ошибка экспорта: " + ex.getMessage());
        }
    }

    private void refreshTable() throws Exception {
        table.setItems(FXCollections.observableArrayList(
                database.getAll()));
    }

    private void info(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private void error(String message) {
        new Alert(
                Alert.AlertType.ERROR,
                message == null ? "Неизвестная ошибка" : message
        ).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
