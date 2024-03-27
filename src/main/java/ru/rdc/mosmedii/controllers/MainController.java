package ru.rdc.mosmedii.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import ru.rdc.mosmedii.models.Item;
import ru.rdc.mosmedii.models.PresignedDataFields;
import ru.rdc.mosmedii.models.UploadResponse;
import ru.rdc.mosmedii.service.MosMedService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static ru.rdc.mosmedii.constants.AppConstant.DICOM_FOLDER;

@Controller
@Scope("singleton")
public class MainController implements Initializable {

    private Stage mainStage;

    @FXML
    private TextField txtEdit;

    private final MosMedService mosMedService;

    public MainController(MosMedService mosMedService) {
        this.mosMedService = mosMedService;
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mosMedService.getToken().subscribe();
    }

    public void ontest(ActionEvent actionEvent) {
        //Выполняем метод getToken и подписываемся на результат, чтобы метод выполнился.
        //mosMedService.getToken().subscribe();
        mosMedService.getResultForTask(txtEdit.getText().trim());
    }

    public void onFileUpload(ActionEvent actionEvent)  {

        mosMedService.uploadFiles();



        //List<Item> itemList = mosMedService.



        //Вызываем метод для получения presigned-url для загрузки файла
        /*mosMedService.uploadStart(filePath).subscribe(rez -> {
            ObjectMapper responseMapper = new ObjectMapper(); //Создаем маппер

            try {
                UploadResponse uploadResponse = responseMapper.readValue(rez, UploadResponse.class);

                String url = uploadResponse.getPresignedData().getUrl();
                PresignedDataFields fields = uploadResponse.getPresignedData().getFields();
                String taskId = uploadResponse.getTaskId();

                //Вызываем метод отправки файла в Yandex Cloud, куда передаем полученные в результате выполнения метода uploadFile данные
                mosMedService.uploadFileS3(filePath, url, fields).subscribe(response -> {
                    System.out.println(response);
                    //Вызываем метод для уведомления сервиса ИИ о завершении загрузки файла в Yandex Cloud
                    mosMedService.uploadStop(taskId); //Передаем task_id
                }, throwable -> {
                    System.out.println(throwable.getMessage());
                });

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });*/
    }



    public void onStop(ActionEvent actionEvent) throws IOException {

    }


}
