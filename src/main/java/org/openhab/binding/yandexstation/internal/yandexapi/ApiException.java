package org.openhab.binding.yandexstation.internal.yandexapi;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ApiException extends Exception {
    private static final long serialVersionUID = -1748312966538510299L;
    public ApiException(String message) {
        super(message);
    }
    public ApiException(String message, Throwable e) {
        super(message, e);
    }
}
