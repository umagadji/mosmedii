package ru.rdc.mosmedii.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//Класс содержит описание таблицы item в БД. Таблица item хранит данные о передаваемых в ИИ сервис снимках
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "source_file_name")
    private String sourceFileName;
    @Column(name = "fio")
    private String fio;
    @Column(name = "date")
    private LocalDate date;
    @Column(name = "modality")
    private String modality;
    @Column(name = "anatomical_area")
    private String anatomicalArea;
    @Column(name = "serviceid")
    private String serviceId;
    @Column(name = "new_file_name")
    private String newFileName;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "file_path")
    private String filePath;
    @Column(name = "task_id")
    private String taskId;
    @Column(name = "result")
    private String result;

    @Override
    public String toString() {
        return "Item{" +
                "sourceFileName='" + sourceFileName + '\'' +
                ", fio='" + fio + '\'' +
                ", date='" + date + '\'' +
                ", modality='" + modality + '\'' +
                ", anatomicalArea='" + anatomicalArea + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", newFileName='" + newFileName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", filePath='" + filePath + '\'' +
                ", taskId='" + taskId + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
