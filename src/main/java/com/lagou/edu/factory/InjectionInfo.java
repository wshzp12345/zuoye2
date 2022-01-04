package com.lagou.edu.factory;

import com.lagou.edu.factory.annnotation.Autowired;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
@Data
@AllArgsConstructor
public class InjectionInfo {
    private String id;
    private Field field;
    private Annotation annotation;

}
