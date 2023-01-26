package me.cprox.practice.util.external.callback;

import java.io.Serializable;

public interface TypeCallback<T> extends Serializable {

    /**
     * A callback for running a task on a set of data.
     *
     * @param data the data needed to run the task.
     */
    void callback(T data);

}