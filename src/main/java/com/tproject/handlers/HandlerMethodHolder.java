package com.tproject.handlers;

import com.tproject.annotations.Controller;
import com.tproject.annotations.RequestMapping;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;


public class HandlerMethodHolder {

    private static volatile HandlerMethodHolder instance;

    private HashMap<HttpMapping,HttpHandler> controllerMap = new HashMap<>();

    public static HandlerMethodHolder getInstance() {
        HandlerMethodHolder localInstance = instance;
        if (localInstance == null) {
            synchronized (HandlerMethodHolder.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new HandlerMethodHolder();
                }
            }
        }
        return localInstance;
    }

    public HashMap<HttpMapping,HttpHandler> getControllerMap() {
        if (controllerMap.isEmpty()) {
            try {
                updateControllerMap();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return controllerMap;
    }

    protected void updateControllerMap() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        //set package to explore
        Reflections reflections = new Reflections("com.tproject.controllers");

        //get every class, marked with @Controller annotation
        Set<Class<?>> annotatedAsControllerClasses = reflections.get(SubTypes.of(TypesAnnotated.with(Controller.class)).asClass());

        //walk through all classes in package
        for (Class dedclaredClass : annotatedAsControllerClasses) {

            //get all methods of the current class
            Method[] classMethods = dedclaredClass.getDeclaredMethods();

            //check all methos of the current class
            for (Method declaredClassMethod : classMethods) {

                //get all annotations of current method
                Annotation[] methodAnnotations = declaredClassMethod.getDeclaredAnnotations();

                for (Annotation annotation : methodAnnotations) {
                    //if method of current class marked with @RequestMapping

                    if (annotation instanceof RequestMapping) {
                        RequestMapping customAnnotation = (RequestMapping) annotation;

                        //create pair "URL - called method"
                        HttpMapping newMapping = HttpMapping.builder().path(customAnnotation.url()).method(customAnnotation.method()).build();

                        //if created pair is not already exist - insert into map of "URL - method"
                        if (!this.controllerMap.containsKey(newMapping)) {
                            this.controllerMap.put(newMapping, HttpHandler.builder()
                                    .method(declaredClassMethod)
                                    .clazz(dedclaredClass)
                                    .handlerObject(dedclaredClass.getDeclaredConstructor().newInstance())
                                    .build());
                        }
                        else {
                            throw new RuntimeException();
                        }

                    }
                }
            }
        }
    }
}
