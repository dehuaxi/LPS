package com.defei.lps.result;

import lombok.Data;

/**
 * 返回的数据格式类
 * @param <T>
 */
@Data
public class Result<T> {
    private int code;//返回码
    private String msg;//返回的信息
    private T data;//返回的内容
}
