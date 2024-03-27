package ru.rdc.mosmedii.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
//Класс для передачи в методе /v1/client/upload/start
public class UploadFile {
    private String file_name;
    private String file_type;
}
