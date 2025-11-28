 /*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.i360day.invoker.exception;

public class InvokerTimeoutException extends InvokerException {
    public InvokerTimeoutException(Throwable cause) {
        super(cause);
    }

    public InvokerTimeoutException(Throwable cause, String message) {
        super(cause, message);
    }

    public InvokerTimeoutException(InvokerErrorCode errorCode) {
        super(errorCode);
    }

    public InvokerTimeoutException(String message) {
        super(message);
    }

    public InvokerTimeoutException(InvokerErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
