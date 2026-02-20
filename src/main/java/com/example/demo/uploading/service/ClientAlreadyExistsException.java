package com.example.demo.base.uploading.service;

public class ClientAlreadyExistsException extends Throwable {
    public ClientAlreadyExistsException(String clientId) {
        super(clientId);

    }
}
