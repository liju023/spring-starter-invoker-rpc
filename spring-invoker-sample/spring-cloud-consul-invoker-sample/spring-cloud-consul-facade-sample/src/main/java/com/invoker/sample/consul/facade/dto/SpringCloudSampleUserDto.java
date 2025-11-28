package com.invoker.sample.consul.facade.dto;

import java.util.List;

public class SpringCloudSampleUserDto {

    private List<String> list;

    private String success;

    private Integer count;

    public SpringCloudSampleUserDto() {
    }

    public SpringCloudSampleUserDto(List<String> list, String success) {
        this.list = list;
        this.success = success;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
