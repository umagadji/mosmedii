package ru.rdc.mosmedii;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class MosMedIi extends Application {
    ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(MosMedIiApplication.class).run();
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }

    @Override
    public void start(Stage stage) {
        applicationContext.publishEvent(new StageReadyEvent(stage));
    }

    static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }

        public Stage getStage() {
            return ((Stage) getSource());
        }
    }
}