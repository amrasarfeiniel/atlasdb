/**
 * Copyright 2015 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.common.concurrent;

import java.util.concurrent.Callable;

/**
 * Whenever this callable is called, for the duration of the call we will have a new thread name.
 * <p>
 * This is useful for debugging because when you get thread dumps the name will reflect what callable
 * is running.
 * @author carrino
 */
public class ThreadNamingCallable<T> implements Callable<T> {
    final Callable<T> delegate;
    final String name;
    final Type type;

    public enum Type {
        PREPEND, REPLACE, APPEND;
    }

    public static <T> Callable<T> wrapWithThreadName(Callable<T> delegate, String threadName) {
        return wrapWithThreadName(delegate, threadName, Type.REPLACE);
    }

    public static <T> Callable<T> wrapWithThreadName(Callable<T> delegate, String threadName, Type type) {
        return new ThreadNamingCallable<T>(delegate, threadName, type);
    }

    private ThreadNamingCallable(Callable<T> delegate, String name, Type type) {
        this.delegate = delegate;
        this.name = name;
        this.type = type;
    }

    @Override
    public T call() throws Exception {
        final String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(getNewName(oldName));
        try {
            return delegate.call();
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }

    private String getNewName(String oldName) {
        switch (type) {
        case PREPEND:
            return name + ' ' + oldName;
        case REPLACE:
            return name;
        case APPEND:
            return oldName + ' ' + name;
        default:
            throw new IllegalArgumentException("type not found: " + type);
        }
    }

}
