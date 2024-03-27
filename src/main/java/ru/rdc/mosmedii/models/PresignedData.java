package ru.rdc.mosmedii.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
//Класс, для получения в JSON формате в результате выполнения метода /v1/client/upload/start
//Описывает сегмент presigned_data
public class PresignedData {
    private String url;
    private PresignedDataFields fields;

    @Override
    public String toString() {
        return "PresignedData{" +
                "url='" + url + '\'' +
                ", fields=" + fields +
                '}';
    }
}
