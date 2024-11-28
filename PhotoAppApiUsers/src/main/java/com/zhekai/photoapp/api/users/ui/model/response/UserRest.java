package com.zhekai.photoapp.api.users.ui.model.response;

import java.util.UUID;

import com.zhekai.photoapp.api.users.ui.model.UserBase;

public class UserRest extends UserBase {

	private UUID userId;

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

}
