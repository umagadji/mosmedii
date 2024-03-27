package ru.rdc.mosmedii.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.rdc.mosmedii.constants.AppConstant;
import ru.rdc.mosmedii.models.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static ru.rdc.mosmedii.constants.AppConstant.DICOM_FOLDER;

@Service
public class MosMedService {

    private WebClient webClient; //Используем WebClient, чтобы применять возможности реактивного программирования
    private String cachedToken; //хранит кешированный токен
    private LocalDateTime tokenExpirationTime; //хранит время истечения токена

    private final ItemService service;

    @Value("${url}")
    private String URL; //Основная часть URL для выполнения запросов

    @Value("${client_id}")
    private String client_id;

    @Value("${client_secret}")
    private String client_secret;

    public MosMedService(ItemService service) {
        this.service = service;
        this.webClient = WebClient.create();
    }

    //Метод читает файлы из папки с архивами, записывает в БД информацию по ним и переименовывает.
    //Отправляет данные по ним в API и записываем в БД готовую информацию включая task_id
    public void uploadFiles() {
        // Получаем список файлов в папке и выводим их имена
        Arrays.stream(Objects.requireNonNull(new File(DICOM_FOLDER).listFiles()))
                //Фильтрует только файлы, которые имеют расширение .zip
                .filter(file -> file.isFile() && isValidFileName(file.getName())/*file.getName().toLowerCase().endsWith(".zip")*/)
                .forEach(file -> {
                    //Ищем последнее вхождение точки в название файла
                    int lastIndexOf = file.getName().lastIndexOf('.');
                    //Получаем имя файла без расширения
                    String fileNameWithoutExtension = file.getName().substring(0, lastIndexOf);

                    //Делим строку используя разделитель "_"
                    String[] words = fileNameWithoutExtension.split("_");

                    LocalDate localDate = LocalDate.parse(words[1]);
                    LocalDateTime localDateTime = LocalDateTime.of(localDate, LocalTime.MIN);

                    //Получаем каждый блок из разбитой строка
                    String fio = words[0];
                    String date = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    String modality = words[2];
                    String anatomicalArea = words[3];
                    String serviceId = words[4];

                    //Генерируем случайное название для файла, чтобы переименовать архив
                    UUID uuid = UUID.randomUUID();
                    String newFileName = uuid + ".zip";

                    //Создаем новый файл
                    File newFile = new File(DICOM_FOLDER + "\\" + newFileName);

                    //Переименовываем файл
                    if (file.renameTo(newFile)) {
                        System.out.println("Файл переименован!");
                    } else {
                        System.out.println("Не удалось переименовать файл!");
                    }

                    //Создаем запись для сохранения в БД
                    Item dicomItem = new Item();
                    dicomItem.setSourceFileName(file.getName());
                    dicomItem.setFio(fio);
                    dicomItem.setDate(localDate);
                    dicomItem.setModality(modality);
                    dicomItem.setAnatomicalArea(anatomicalArea);
                    dicomItem.setServiceId(serviceId);
                    dicomItem.setUuid(uuid.toString());
                    dicomItem.setNewFileName(newFileName);
                    dicomItem.setFilePath(DICOM_FOLDER + "\\" + newFileName);

                    //Начинаем работу с API
                    try {
                        //Выполняем запрос по ссылке https://api.test.mosmedai.ru/v1/client/upload/start
                        uploadStart(dicomItem.getFilePath()).subscribe(rez -> {
                            //После получения результата создаем маппер
                            ObjectMapper responseMapper = new ObjectMapper(); //Создаем маппер

                            //Создаем объект типа UploadResponse для получения результатов запроса
                            //UploadResponse uploadResponse = responseMapper.readValue(rez, UploadResponse.class);

                            //Вытаскиваем полученные из JSON данные
                            String url = rez.getPresignedData().getUrl(); //Ссылка куда будем заливать архив со снимками
                            PresignedDataFields fields = rez.getPresignedData().getFields(); //Поля необходимые в теле запроса
                            String taskId = rez.getTaskId(); //Номер задачи

                            //После получения task_id записываем его в объект
                            dicomItem.setTaskId(taskId);

                            //Сохраняем в БД
                            service.save(dicomItem);

                            //Вызываем метод отправки файла в Yandex Cloud, куда передаем полученные в результате выполнения метода uploadFile данные
                            uploadFileS3(dicomItem.getFilePath(), url, fields).subscribe(res -> {
                                System.out.println("Код статуса: " + res);
                                //Вызываем метод для уведомления сервиса ИИ о завершении загрузки файла в Yandex Cloud
                                uploadStop(taskId)
                                        .then(createTask(dicomItem))//Здесь вызываем then, чтобы выполнить код дальше, т.к. сервер ничего не возвращает
                                        .doOnNext(task -> System.out.println("Задача для файла " + dicomItem.getFilePath() + " создана: " + task))
                                        .doOnError(error -> System.out.println("Ошибка: " + error))
                                        .doOnSuccess(response1 -> System.out.println("Ответ на запрос uploadStop: " + response1))
                                        .subscribe();
                            }, throwable -> {
                                System.out.println(throwable.getMessage());
                            });

                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public static boolean isValidFileName(String fileName) {
        // Регулярное выражение для проверки названия файла
        String regex = "^[\\w\\s.-]+_[\\w\\s.-]+_[\\w\\s.-]+_[\\w\\s.-]+_[\\w\\s.-]+\\.zip$";
        // Проверяем, соответствует ли имя файла шаблону
        return Pattern.matches(regex, fileName);
    }

    //Метод создает заявку в ИИ сервис для каждой записи (каждого снимка DICOM)
    public Mono<Task> createTask(Item item) {

        TaskParams taskParams = new TaskParams();
        taskParams.setModality(item.getModality());
        taskParams.setAnatomicalArea(item.getAnatomicalArea());
        taskParams.setAgeGroup(AppConstant.AGE_GROUP);

        Task task = new Task();
        task.setFrmoID(AppConstant.CODE_FRMO);
        task.setNameMo(AppConstant.MO_NAME);
        task.setAeTitle(AppConstant.AE_TITLE);
        task.setStudyUUID(item.getUuid());
        task.setServiceID(item.getServiceId());
        task.setStudyDate(item.getDate().toString());
        task.setTaskParams(taskParams);
        task.setTaskId(item.getTaskId());

        // Создаем строку, содержащую json, который будем передавать в запросе
        ObjectMapper mapper = new ObjectMapper(); // Создаем объект ObjectMapper для работы с JSON
        String json = null; // Преобразуем объект UploadFile в JSON строку
        try {
            json = mapper.writeValueAsString(task);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        System.out.println(json);

        String url = URL + "client/task"; // Составляем URL для запроса на получение токена
        return webClient.post() // Начинаем POST запрос с помощью WebClient
                .uri(url) // Устанавливаем URI для запроса
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cachedToken) // Устанавливаем заголовок Authorization с токеном аутентификации
                .contentType(MediaType.APPLICATION_JSON) // Устанавливаем Content-Type заголовок как JSON
                .body(BodyInserters.fromValue(json)) // Устанавливаем тело запроса с помощью JSON строки
                .retrieve() // Инициируем отправку запроса и получение ответа
                .bodyToMono(Task.class);
    }

    //Метод получает результат обработки из ИИ сервиса по указанному Task_id
    public void getResultForTask(String taskId) {
        String url = URL + "client/task/" + taskId; // Составляем URL

        Optional<Item> item = service.findByTaskId(taskId);

        //Если в БД найден элемент с указанным task_id
        if (item.isPresent()) {
            webClient.get() // Начинаем POST запрос с помощью WebClient
                    .uri(url) // Устанавливаем URI для запроса
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cachedToken) // Устанавливаем заголовок Authorization с токеном аутентификации
                    .retrieve() // Инициируем отправку запроса и получение ответа
                    .bodyToMono(String.class)
                    .subscribe(response -> {
                        System.out.println(response);
                    });
        }

    }

    //Запрос для получения presigned-url, чтобы по ней далее загрузить zip-архив
    public Mono<UploadResponse> uploadStart(String filePath) throws IOException {
        //Ссылка для выполнения запроса
        String url = URL + "client/upload/start";

        File file = new File(filePath);

        // Создаем строку, содержащую json, который будем передавать в запросе
        ObjectMapper mapper = new ObjectMapper(); // Создаем объект ObjectMapper для работы с JSON
        String json = mapper.writeValueAsString(new UploadFile(file.getName(), "zip")); // Преобразуем объект UploadFile в JSON строку

        // Отправляем POST запрос с данными в теле
        return webClient.post() // Начинаем POST запрос с помощью WebClient
                .uri(url) // Устанавливаем URI для запроса
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cachedToken) // Устанавливаем заголовок Authorization с токеном аутентификации
                .contentType(MediaType.APPLICATION_JSON) // Устанавливаем Content-Type заголовок как JSON
                .body(BodyInserters.fromValue(json)) // Устанавливаем тело запроса с помощью JSON строки
                .retrieve() // Инициируем отправку запроса и получение ответа
                .bodyToMono(UploadResponse.class); // Преобразуем ответ в Mono<String> и возвращаем его

    }

    //Метод сообщает что загрузка файла в Yandex Cloud завершена
    public Mono<String> uploadStop(String taskId) {
        //Ссылка для выполнения запроса
        String url = URL + "client/upload/stop";

        // Создаем строку, содержащую json, который будем передавать в запросе
        ObjectMapper mapper = new ObjectMapper(); // Создаем объект ObjectMapper для работы с JSON
        // Создаем Map для передачи task_id
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("task_id", taskId);

        // Преобразуем Map в JSON строку
        String json = null;
        try {
            json = mapper.writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // Отправляем POST запрос с данными в теле
        return webClient.post() // Начинаем POST запрос с помощью WebClient
                .uri(url) // Устанавливаем URI для запроса
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cachedToken) // Устанавливаем заголовок Authorization с токеном аутентификации
                .contentType(MediaType.APPLICATION_JSON) // Устанавливаем Content-Type заголовок как JSON
                .body(BodyInserters.fromValue(json)) // Устанавливаем тело запроса с помощью JSON строки
                .retrieve() // Инициируем отправку запроса и получение ответа
                .bodyToMono(String.class);
    }

    //Метод загружает файл в S3 хранилище по параметрам, которые получены в JSON
    public Mono<Integer> uploadFileS3(String filePath, String url, PresignedDataFields fields) {
        //Полученные из JSON параметры для Yandex Cloud добавляем в тело запроса
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("acl", fields.getAcl());
        body.add("key", fields.getKey());
        body.add("Content-Type", fields.getContentType());
        body.add("x-amz-algorithm", fields.getXAmzAlgorithm());
        body.add("x-amz-credential", fields.getXAmzCredential());
        body.add("x-amz-date", fields.getXAmzDate());
        body.add("policy", fields.getPolicy());
        body.add("x-amz-signature", fields.getXAmzSignature());

        // Создание заголовков для файла
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        //Получаем сам файл и добавляем его в тело запроса
        FileSystemResource resource = new FileSystemResource(new File(filePath));
        body.add("file", new HttpEntity<>(resource, fileHeaders));

        // Отправка запроса с помощью WebClient и обработка ответа
        return webClient.post()
                .uri(url)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .exchangeToMono(response -> {
                    //Если успешно выводим код статуса
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.just(response.statusCode().value());
                    } else {
                        return response.createException()
                                .flatMap(Mono::error);
                    }
                });
    }

    //Запрос на получение токена. Для его получения передаем client_id и client_secret, предоставленные МосМедИИ
    public Mono<String> getToken() {
        // Проверяем, истек ли токен или он еще действителен
        if (cachedToken != null && tokenExpirationTime != null && LocalDateTime.now().isBefore(tokenExpirationTime)) {
            return Mono.just(cachedToken); // Возвращаем закэшированный токен. just создает новый Mono с заданным значением
        } else {
            String url = URL + "auth/access_token"; // Составляем URL для запроса на получение токена
            HttpHeaders headers = new HttpHeaders(); // Создаем объект для хранения HTTP заголовков
            headers.setContentType(MediaType.APPLICATION_JSON); // Устанавливаем заголовок Content-Type как JSON
            return webClient.post() // Начинаем POST запрос с помощью WebClient
                    .uri(url) // Устанавливаем URI для запроса
                    .headers(httpHeaders -> httpHeaders.addAll(headers)) // Устанавливаем HTTP заголовки для запроса
                    .body(BodyInserters.fromValue("{\"client_id\": \"" + client_id + "\", \"client_secret\": \"" + client_secret + "\"}")) // Устанавливаем тело запроса с помощью BodyInserters.fromValue
                    .retrieve() // Инициируем отправку запроса и получение ответа
                    .bodyToMono(String.class) // Преобразуем ответ в Mono<String>
                    .map(responseBody -> {
                        //JSONObject jsonObject = new JSONObject(responseBody); // создаем объект JSONObject из строки JSON
                        //return jsonObject.getString("token"); // извлекаем значение токена из JSON и возвращаем его
                        ObjectMapper objectMapper = new ObjectMapper(); // создаем объект ObjectMapper
                        JsonNode jsonNode = null; // читаем JSON строку в JsonNode
                        try {
                            jsonNode = objectMapper.readTree(responseBody);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        return jsonNode.get("token").asText(); // извлекаем значение токена из JsonNode и возвращаем его
                    })
                    .doOnNext(token -> { // При получении нового токена обновляем кэш и время его истечения
                        cachedToken = token; // Обновляем значение закэшированного токена
                        System.out.println(cachedToken); //Выводим токен, можно удалить
                        tokenExpirationTime = LocalDateTime.now().plus(12, ChronoUnit.HOURS); // Устанавливаем время истечения токена через 12 часов
                    });
        }
    }
}