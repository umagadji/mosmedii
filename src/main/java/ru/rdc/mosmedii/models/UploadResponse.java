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
//Класс, для получения в JSON формате в результате выполнения метода /v1/client/upload/start
//Описывает сам JSON - верхний уровень
public class UploadResponse {
    @JsonProperty("presigned_data")
    private PresignedData presignedData;
    @JsonProperty("task_id")
    private String taskId;
}
