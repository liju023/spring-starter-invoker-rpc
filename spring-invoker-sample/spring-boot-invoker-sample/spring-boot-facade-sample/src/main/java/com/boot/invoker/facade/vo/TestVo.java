package com.boot.invoker.facade.vo;

import java.io.Serializable;
import java.util.List;

/**
 * @program: spring-cloud-invoker-parent
 * @description: test
 * @author: liju.z
 * @create: 2022-08-14 11:32
 **/
public class TestVo implements Cloneable, Serializable {
    private static final long serialVersionUID = 4507779485095471381L;

    private String userName;

    private String password;

    private List<TestVo> testVoList;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<TestVo> getTestVoList() {
        return testVoList;
    }

    public void setTestVoList(List<TestVo> testVoList) {
        this.testVoList = testVoList;
    }

    @Override
    public TestVo clone() {
        TestVo testVo = new TestVo();
        testVo.setPassword(password);
        testVo.setUserName(userName);
        return testVo;
    }
}
