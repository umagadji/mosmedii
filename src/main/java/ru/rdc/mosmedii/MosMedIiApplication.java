package ru.rdc.mosmedii;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;

@SpringBootApplication
public class MosMedIiApplication {

    //Начало выполнения программы
    public static void main(String[] args) {
        Application.launch(MosMedIi.class, args);
    }

}
