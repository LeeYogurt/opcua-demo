/*
 *
 *      Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the pig4cloud.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: lengleng (wangiegie@gmail.com)
 *
 */

package com.rookie.opcua.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 响应信息主体
 *
 * @param <T>
 * @author yugo
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ResponseDTO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int SUCCESS = 0;

    private static final int FAIL = 1;

    @Getter
    @Setter
    private int code;

    @Getter
    @Setter
    private String msg;

    @Getter
    @Setter
    private T data;

    public static <T> ResponseDTO<T> ok() {
        return restResult(null, SUCCESS, null);
    }

    public static <T> ResponseDTO<T> ok(T data) {
        return restResult(data, SUCCESS, null);
    }

    public static <T> ResponseDTO<T> ok(T data, String msg) {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> ResponseDTO<T> failed() {
        return restResult(null, FAIL, null);
    }

    public static <T> ResponseDTO<T> failed(String msg) {
        return restResult(null, FAIL, msg);
    }

    public static <T> ResponseDTO<T> failed(T data) {
        return restResult(data, FAIL, null);
    }

    public static <T> ResponseDTO<T> failed(T data, String msg) {
        return restResult(data, FAIL, msg);
    }

    private static <T> ResponseDTO<T> restResult(T data, int code, String msg) {
        ResponseDTO<T> apiResult = new ResponseDTO<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }
}
