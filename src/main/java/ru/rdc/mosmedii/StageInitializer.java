package ru.rdc.mosmedii;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.rdc.mosmedii.controllers.MainController;

import java.io.IOException;
import java.util.Objects;

@Component
public class StageInitializer implements ApplicationListener<MosMedIi.StageReadyEvent> {

    @Value("classpath:/fxml/main.fxml")
    private Resource resource;
    private final String applicationTitle;
    private final ApplicationContext applicationContext;

    public StageInitializer(
            @Value("yyyyyyy") String applicationTitle,
            ApplicationContext applicationContext) {
        this.applicationTitle = applicationTitle;
        this.applicationContext = applicationContext;
        //Вызываем метод, чтобы при открытии приложения версия приложения подтягивалась
        //AppInfo.getAppInfo();
    }

    @Override
    public void onApplicationEvent(MosMedIi.StageReadyEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(resource.getURL());
            loader.setControllerFactory(aClass -> applicationContext.getBean(aClass));
            Parent parent = loader.load();
            Stage stage = event.getStage();
            stage.setScene(new Scene(parent, 1000, 650));
            //stage.setTitle(applicationTitle + " " + AppInfo.getVersion());
            //stage.getIcons().add(new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/images/icon.png"))));

            MainController mainController = loader.getController();
            mainController.setMainStage(stage);

            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
