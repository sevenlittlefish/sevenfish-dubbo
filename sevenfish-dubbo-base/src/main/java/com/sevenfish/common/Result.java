package com.sevenfish.common;

import java.io.Serializable;

public class Result<T> implements Serializable {

    private int code;

    private String msg;

    private T data;

    public Result(int code, String msg, T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> Result<T> success(int code, String msg, T data){
        return new Result<T>(code, msg, data);
    }

    public static <T> Result<T> success(String msg, T data){
        return new Result<T>(200, msg, data);
    }

    public static <T> Result<T> success(String msg){
        return new Result<T>(200, msg, null);
    }

    public static <T> Result<T> success(T data){
        return new Result<T>(200, "success", data);
    }

    public static <T> Result<T> success(){
        return new Result<T>(200, "success", null);
    }

    public static <T> Result<T> error(int code, String msg, T data){
        return new Result<T>(code, msg, data);
    }

    public static <T> Result<T> error(String msg, T data){
        return new Result<T>(500, msg, data);
    }

    public static <T> Result<T> error(String msg){
        return new Result<T>(500, msg, null);
    }

    public static <T> Result<T> error(T data){
        return new Result<T>(500, "fail", data);
    }

    public static <T> Result<T> error(){
        return new Result<T>(500, "fail", null);
    }
}
