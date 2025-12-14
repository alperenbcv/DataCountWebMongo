package com.example.datacountwebmongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BaseResponse<T> {
    Boolean success;
    String message;
    Integer code;
    T data;
}
