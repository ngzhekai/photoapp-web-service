package com.zhekai.photoapp.api.users.ui.model;

public class CreateUserRequestModel extends UserBase {
    private String password;

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }
}
