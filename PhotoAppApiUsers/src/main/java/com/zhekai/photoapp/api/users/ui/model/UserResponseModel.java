package com.zhekai.photoapp.api.users.ui.model;

import java.util.List;

public class UserResponseModel extends UserBase {
    private String userId;
    private List<AlbumResponseModel> albums;

    public String getUserId() {
	return userId;
    }

    public void setUserId(String userId) {
	this.userId = userId;
    }

    public List<AlbumResponseModel> getAlbums() {
	return albums;
    }

    public void setAlbums(List<AlbumResponseModel> albums) {
	this.albums = albums;
    }
}
