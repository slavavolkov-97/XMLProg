import org.postgresql.util.PSQLException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class XMLParser {
    private static final ArrayList<Catalog> catalogs = new ArrayList<>();
    private static Catalog currentCatalog;
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        AdvancedXMLHandler handler = new AdvancedXMLHandler();
        FileChooser fileChooser = new FileChooser();
        File[] files = fileChooser.getFiles();
        if (files != null) {
            for (File xmlfile : files) {
                parser.parse(xmlfile, handler);
                currentCatalog = null;
            }
            insertDatabase();
        } else System.out.println("ОШИБКА! Вы не выбрали файлы.");
        System.out.println("Программа завершена.\nНажмите Enter для выхода.");
        Scanner in = new Scanner(System.in);
        in.nextLine();
        in.close();
    }

    private static class AdvancedXMLHandler extends DefaultHandler {
        private String common, botanical, zone, light, price, availability;
        private String currentElementName;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes){
            currentElementName = qName;
            if (currentElementName.equals("CATALOG")) {
                String uuid = attributes.getValue("uuid");
                String deliveryDate = attributes.getValue("date");
                String company = attributes.getValue("company");
                if ((uuid != null && !uuid.isEmpty()) &&
                    (deliveryDate != null && !deliveryDate.isEmpty()) &&
                    (company != null && !company.isEmpty())){
                    currentCatalog = new Catalog(uuid, deliveryDate, company);
                    catalogs.add(currentCatalog);
                }
                else System.out.printf("ОШИБКА! Не хватает данных о каталоге: uuid='%s', delivery_date='%s', company='%s'\n",uuid, deliveryDate, company);
            }
            if (currentElementName.equals("PLANT")){
                common = null;
                botanical = null;
                zone = null;
                light = null;
                price = null;
                availability = null;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length){
            String information = new String(ch, start, length);

            information = information.replace("\n", "").trim();

            if (!information.isEmpty()) {
                if (currentElementName.equals("COMMON"))
                    common = information;
                if (currentElementName.equals("BOTANICAL"))
                    botanical = information;
                if (currentElementName.equals("ZONE"))
                    zone = information;
                if (currentElementName.equals("LIGHT"))
                    light = information;
                if (currentElementName.equals("PRICE"))
                    price = information;
                if (currentElementName.equals("AVAILABILITY"))
                    availability = information;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName){
            if ((currentCatalog != null) &&
                (common != null && !common.isEmpty()) &&
                (botanical != null && !botanical.isEmpty()) &&
                (zone != null && !zone.isEmpty()) &&
                (light != null && !light.isEmpty()) &&
                (price != null && !price.isEmpty()) &&
                (availability != null && !availability.isEmpty())) {
                currentCatalog.addPlant(common, botanical, zone, light, price, availability);
                common = null;
                botanical = null;
                zone = null;
                light = null;
                price = null;
                availability = null;
            }
        }
    }

    private static void insertDatabase() {
        try {
            //Подключение к БД
            Class.forName("org.postgresql.Driver");
            Scanner in = new Scanner(System.in);
            System.out.print("Введите данные БД\nIP: ");
            String url = "jdbc:postgresql://" + in.nextLine() + ':';
            System.out.print("Порт: ");
            url += in.nextLine() + '/';
            System.out.print("Имя: ");
            url += in.nextLine();
            System.out.print("Введите данные пользователя\nЛогин: ");
            String login = in.nextLine();
            System.out.print("Пароль: ");
            String password = in.nextLine();

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                System.out.println("ОШИБКА! PostgreSQL JDBC Driver не найден.");
                e.printStackTrace();
                return;
            }
            Connection con;
            try {
                con = DriverManager.getConnection(url, login, password);
            } catch (SQLException e) {
                System.out.println("Ошибка подключения! Проверьте данные для подключения.");
                return;
            }

            if (con != null) {
                System.out.println("Вы успешно подключились к БД");
            } else {
                System.out.println("ОШИБКА! Не удалось подключиться к БД.");
                return;
            }

            String sql = "";
            PreparedStatement stmt = con.prepareStatement(sql);
            try {
                for (Catalog catalog : catalogs) {
                    //Получение данных о каталоге
                    String uuid = catalog.getUuid();
                    Timestamp deliveryDate = catalog.getDeliveryDate();
                    String company = catalog.getCompany();
                    // Запрос с указанием мест для параметров в виде знака "?"
                    sql = "INSERT INTO d_cat_catalog (uuid, delivery_date, company) VALUES (?, ?, ?)";
                    // Создание запроса.
                    stmt = con.prepareStatement(sql);
                    // Установка параметров
                    stmt.setString(1, uuid);
                    stmt.setTimestamp(2, deliveryDate);
                    stmt.setString(3, company);
                    // Выполнение запроса на добавление каталога
                    try {
                        stmt.executeUpdate();
                    } catch (PSQLException e) {
                        if (e.getSQLState().equals("23505")) { //Нарушение уникальности первичного ключа
                            System.out.printf("ОШИБКА! Каталог с uuid='%s' компании %s уже существует. Запись пропущена.\n", uuid, company);
                            continue; //Переход на следующий каталог
                        } else {
                            e.printStackTrace();
                            return;
                        }
                    }
                    System.out.printf("Каталог с uuid='%s' компании %s успешно добавлен в БД.\n", uuid, company);
                    //Получаем id добавленного каталога
                    sql = "SELECT id FROM d_cat_catalog WHERE uuid='" + uuid + "'";
                    Statement stm = con.createStatement();
                    ResultSet resultSet = stm.executeQuery(sql);
                    int catalogID;
                    resultSet.next();
                    catalogID = resultSet.getInt("id");

                    //Добавляем расстения в каталог
                    ArrayList<Plant> plants = catalog.getPlants();
                    for (Plant plant : plants) {
                        // Получение данных о расстении
                        String common = plant.getCommon();
                        String botanical = plant.getBotanical();
                        String zone = plant.getZone();
                        String light = plant.getLight();
                        BigDecimal price = plant.getPrice();
                        Integer availability = plant.getAvailability();
                        sql = "INSERT INTO f_cat_plants (common, botanical, zone, light, price, availability, catalog_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        stmt = con.prepareStatement(sql);
                        stmt.setString(1, common);
                        stmt.setString(2, botanical);
                        stmt.setString(3, zone);
                        stmt.setString(4, light);
                        stmt.setBigDecimal(5, price);
                        stmt.setInt(6, availability);
                        stmt.setInt(7, catalogID);
                        try {
                            stmt.executeUpdate();
                        } catch (PSQLException e) {
                            if (e.getSQLState().equals("23505")) { //Нарушение уникальности первичного ключа
                                System.out.printf("     ОШИБКА! Расстение %s уже существует в БД. Запись пропущена.\n", common);
                                continue; //Переход на следующее расстение
                            } else {
                                e.printStackTrace();
                                return;
                            }
                        }
                        System.out.printf("     Расстение %s успешно добавлено в каталог с uuid='%s' компании %s.\n", common, uuid, company);
                    }
                }
            } finally {
                stmt.close();
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
