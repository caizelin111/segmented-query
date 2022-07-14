package com.czl.utils;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author CaiZelin
 * @date 2022/7/5 20:04
 */
public class ZlOptional {
    public static <T> T of(T result, Predicate<? super T> predicate, T defaultValue){
        if (predicate.test(result)) {
            return defaultValue;
        }

        return result;
    }

    public static <T> T ofList(T result){
        return of(result, Objects::isNull, (T) Collections.EMPTY_LIST);
    }
}
