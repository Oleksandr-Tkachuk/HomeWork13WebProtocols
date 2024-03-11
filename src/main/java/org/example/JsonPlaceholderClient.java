package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.FileWriter;
import java.util.Scanner;

public class JsonPlaceholderClient {
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/users";

    // ЗАВДАННЯ 1 ( СТВОРЕННЯ, ОНОВЛЕННЯ, ВИДАЛЕННЯ, ОТРИМАННЯ ІНФИ )
    public String createUser(String userJson) throws IOException {
        return executeRequest("POST", BASE_URL, userJson);
    }

    public String updateUser(int userId, String updatedUserJson) throws IOException {
        String url = BASE_URL + "/" + userId;
        return executeRequest("PUT", url, updatedUserJson);
    }

    public int deleteUser(int userId) throws IOException {
        String url = BASE_URL + "/" + userId;
        String response = executeRequest("DELETE", url, null);
        return response != null ? 200 : -1;
    }

    public String getAllUsers() throws IOException {
        return executeRequest("GET", BASE_URL, null);
    }

    public String getUserById(int userId) throws IOException {
        String url = BASE_URL + "/" + userId;
        return executeRequest("GET", url, null);
    }

    // ЗАВДАННЯ 2 ( КОМЕНТАРІ )
    public void writeCommentsToFile(int userId) throws IOException {
        // Отримування постів користувача
        String userPostsJson = executeRequest("GET", BASE_URL + "/users/" + userId + "/posts", null);
        int lastPostId = getLastPostId(userPostsJson);

        // Отримання коментарів до останнього постаа
        String commentsJson = executeRequest("GET", BASE_URL + "/posts/" + lastPostId + "/comments", null);

        // Записування коментарів у файл
        String fileName = "user-" + userId + "-post-" + lastPostId + "-comments.json";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(commentsJson);
            System.out.println("Коментарі до останнього поста користувача записуються у файл " + fileName);
        }
    }

    private int getLastPostId(String userPostsJson) {
        // Отримання останнього поста за найбільшим ідентифікатором
        int lastPostId = -1;
        try (Scanner scanner = new Scanner(userPostsJson)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("\"id\":")) {
                    int postId = Integer.parseInt(line.split(":")[1].trim().replace(",", ""));
                    lastPostId = Math.max(lastPostId, postId);
                }
            }
        }
        return lastPostId;
    }

    // ЗАВДАННЯ 3 ( ВИВЕДЕННЯ ВСІХ ВІДРИТИХ ЗАДАЧ ДЛЯ КОРИСТУВАЧА З ІДЕНТИФІІАТОРОМ Х )
    public String getOpenTodosForUser(int userId) throws IOException {
        String url = BASE_URL + "/" + userId + "/todos";
        String todosJson = executeRequest("GET", url, null);
        StringBuilder openTodos = new StringBuilder();

        // Розділяяємо відкриті та закриті задачі
        try (Scanner scanner = new Scanner(todosJson)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("\"completed\": false")) {
                    openTodos.append(line).append("\n");
                }
            }
        }

        return openTodos.toString();
    }

    public String getUserByUsername(String username) throws IOException {
        String url = BASE_URL + "?username=" + username;
        return executeRequest("GET", url, null);
    }

    private String executeRequest(String method, String urlString, String requestBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);

        if (requestBody != null) {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }


    }

    public static void main(String[] args) {
        JsonPlaceholderClient client = new JsonPlaceholderClient();

        try {

            String newUserJson = "{\"name\": \"John Doe\", \"username\": \"johndoe\", \"email\": \"johndoe@example.com\"}";
            String createdUser = client.createUser(newUserJson);
            System.out.println("Створений користувач: " + createdUser);

            int userIdToUpdate = 1;
            String updatedUserJson = "{\"name\": \"John Doe\", \"username\": \"johndoe\", \"email\": \"johndoe_updated@example.com\"}";
            String updatedUser = client.updateUser(userIdToUpdate, updatedUserJson);
            System.out.println("Оновлений інформації об'єкту: " + updatedUser);

            int userIdToDelete = 1;
            int deleteStatusCode = client.deleteUser(userIdToDelete);
            System.out.println("Видалення об'єкта: " + deleteStatusCode);

            String allUsers = client.getAllUsers();
            System.out.println("Інформація про всіх користувачів: " + allUsers);

            int userIdToFetch = 2;
            String userById = client.getUserById(userIdToFetch);
            System.out.println("Отримання інформації про користувача за id: " + userById);

            String usernameToFetch = "Bret";
            String userByUsername = client.getUserByUsername(usernameToFetch);
            System.out.println("Отримання інформації про користувача за username: " + userByUsername);

            String openTodosForUser = client.getOpenTodosForUser(userIdToFetch);
            System.out.println("Відкриті задачі для користувача з id " + userIdToFetch + ":");
            System.out.println(openTodosForUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}