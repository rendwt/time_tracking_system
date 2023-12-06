package com.tproject.handlers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class HttpHandler {
    //store method of the class
    private final Method method;
    //store class
    private final Class<?> clazz;
    //crete object for reflection
    private final Object handlerObject;

}
