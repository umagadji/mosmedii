package ru.rdc.mosmedii.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
//Класс, описывает саму задачу, передаваемую в Json при выполнении метода https://api.test.mosmedai.ru/v1/client/task
public class Task {
    @JsonProperty("frmroid_mo")
    private String frmoID;
    @JsonProperty("mo_name")
    private String nameMo;
    @JsonProperty("aetitle")
    private String aeTitle;
    @JsonProperty("studyuuid")
    private String studyUUID;
    @JsonProperty("serviceid")
    private String serviceID;
    @JsonProperty("studydate")
    private String studyDate;
    @JsonProperty("research_params")
    private TaskParams taskParams;
    @JsonProperty("task_id")
    private String taskId;

    @Override
    public String toString() {
        return "Task{" +
                "frmoID='" + frmoID + '\'' +
                ", nameMo='" + nameMo + '\'' +
                ", aeTitle='" + aeTitle + '\'' +
                ", studyUUID='" + studyUUID + '\'' +
                ", serviceID='" + serviceID + '\'' +
                ", studyDate='" + studyDate + '\'' +
                ", taskParams=" + taskParams +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}
