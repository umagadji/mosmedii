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
//Описывает сегмент fields
public class PresignedDataFields {
    private String acl;
    @JsonProperty("Content-Type")
    private String contentType;
    private String key;
    @JsonProperty("x-amz-algorithm")
    private String xAmzAlgorithm;
    @JsonProperty("x-amz-credential")
    private String xAmzCredential;
    @JsonProperty("x-amz-date")
    private String xAmzDate;
    private String policy;
    @JsonProperty("x-amz-signature")
    private String xAmzSignature;

    @Override
    public String toString() {
        return "PresignedDataFields{" +
                "acl='" + acl + '\'' +
                ", contentType='" + contentType + '\'' +
                ", key='" + key + '\'' +
                ", xAmzAlgorithm='" + xAmzAlgorithm + '\'' +
                ", xAmzCredential='" + xAmzCredential + '\'' +
                ", xAmzDate='" + xAmzDate + '\'' +
                ", policy='" + policy + '\'' +
                ", xAmzSignature='" + xAmzSignature + '\'' +
                '}';
    }
}
