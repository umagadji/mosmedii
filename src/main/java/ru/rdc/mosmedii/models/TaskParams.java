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
//Класс описывает сегмент research_params для json, который передаем в методе https://api.test.mosmedai.ru/v1/client/task
public class TaskParams {
    @JsonProperty("modality_type_code")
    private String modality;
    @JsonProperty("anatomical_areas_code")
    private String anatomicalArea;
    @JsonProperty("age_group")
    private String ageGroup;

    @Override
    public String toString() {
        return "TaskParams{" +
                "modality='" + modality + '\'' +
                ", anatomicalArea='" + anatomicalArea + '\'' +
                ", ageGroup='" + ageGroup + '\'' +
                '}';
    }
}
