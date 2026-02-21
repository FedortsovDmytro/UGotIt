package com.example.demo.uploading.service;

public class ClientAlreadyExistsException extends Throwable {
    public ClientAlreadyExistsException(String clientId) {
        super(clientId);

    }
}
