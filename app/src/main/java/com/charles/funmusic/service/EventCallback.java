package com.charles.funmusic.service;

public interface EventCallback<T> {
    void onEvent(T t);
}
