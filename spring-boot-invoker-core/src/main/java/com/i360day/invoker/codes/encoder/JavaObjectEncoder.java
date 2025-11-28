package com.i360day.invoker.codes.encoder;

import com.i360day.invoker.support.RemoteInvocationResult;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public class JavaObjectEncoder implements Encoder {

    @Override
    public void encoder(OutputStream outputStream, Type responseType, RemoteInvocationResult result) throws IOException {
        FlushGuardedOutputStream flushGuardedOutputStream = new FlushGuardedOutputStream(outputStream);
        ObjectOutputStream oos = new ObjectOutputStream(flushGuardedOutputStream);
        try {
            oos.writeObject(result);
        } finally {
            oos.close();
        }
    }
    /**
     * Decorate an {@code OutputStream} to guard against {@code flush()} calls,
     * which are turned into no-ops.
     * <p>Because {@link ObjectOutputStream#close()} will in fact flush/drain
     * the underlying stream twice, this {@link FilterOutputStream} will
     * guard against individual flush calls. Multiple flush calls can lead
     * to performance issues, since writes aren't gathered as they should be.
     *
     * @see <a href="https://jira.spring.io/browse/SPR-14040">SPR-14040</a>
     */
    private static class FlushGuardedOutputStream extends FilterOutputStream {

        public FlushGuardedOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void flush() throws IOException {
            // Do nothing on flush
        }
    }
}
