import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

public class ServerTaskMain {

    //   /products?from=500&to=1000  - все товары от 500 до 1000
    //   /products?from=500          - все товары от 500
    //   /products                   - все товары
    //   additional:
    //   /products?to=1000           - все товары до 1000
    //   /products?to=1000&from=500  - все товары до 1000 от 500

    static List<Product> products = List.of(
            new Product("Apple", 450, ProductType.FRUIT),
            new Product("Banana", 700, ProductType.FRUIT),
            new Product("Carrot", 300, ProductType.VEGETABLE),
            new Product("Tomato", 600, ProductType.VEGETABLE),
            new Product("Orange", 800, ProductType.FRUIT),
            new Product("Cucumber", 400, ProductType.VEGETABLE),
            new Product("Grapes", 1200, ProductType.FRUIT),
            new Product("Potato", 200, ProductType.VEGETABLE),
            new Product("Beef", 4500, ProductType.MEAT),
            new Product("Chicken", 2000, ProductType.MEAT),
            new Product("Pork", 3000, ProductType.MEAT),
            new Product("Milk", 500, ProductType.DAIRY),
            new Product("Cheese", 1500, ProductType.DAIRY),
            new Product("Yogurt", 800, ProductType.DAIRY)
    );

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/products", ServerTaskMain::handleProducts);

        server.start();
    }

    static void handleProducts(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, 0);

        String rawQuery = exchange.getRequestURI().getRawQuery();
        List<Product> productsRes = products;

        int minPriceReq = 0;
        int maxPriceReq = Integer.MAX_VALUE;

        if (rawQuery != null) {
            String[] queries = rawQuery.split("&");
            for (String query : queries) {
                if (query.startsWith("from=")) {
                    minPriceReq = Integer.parseInt(query.split("=")[1]);
                } else if (query.startsWith("to=")) {
                    maxPriceReq = Integer.parseInt(query.split("=")[1]);
                }
            }
        }

        int finalMinPriceReq = minPriceReq;
        int finalMaxPriceReq = maxPriceReq;

        productsRes = productsRes.stream()
                .filter(product -> product.price() >= finalMinPriceReq && product.price() <= finalMaxPriceReq)
                .toList();

        Gson gson = new Gson();
        OutputStream os = exchange.getResponseBody();
        os.write(gson.toJson(productsRes).getBytes());
        os.close();
    }

    record Product(String name, int price, ProductType type) {
    }

    enum ProductType {FRUIT, VEGETABLE, MEAT, DAIRY}
}
