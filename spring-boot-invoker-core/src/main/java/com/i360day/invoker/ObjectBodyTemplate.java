package com.i360day.invoker;


import com.i360day.invoker.support.RemoteInvocation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @program: spring-cloud-invoker-parent
 * @description: java object
 * @author: liju.z
 * @create: 2022-08-14 11:59
 **/
public class ObjectBodyTemplate implements BodyTemplate{

    private static final int SERIALIZED_INVOCATION_BYTE_ARRAY_INITIAL_SIZE = 1024;

    private byte[] body;

    public ObjectBodyTemplate(RemoteInvocation remoteInvocation) throws IOException {
        try(ByteArrayOutputStream byteArrayOutputStream = getByteArrayOutputStream(remoteInvocation)) {
            this.body = byteArrayOutputStream.toByteArray();
        }catch (IOException ex){
            throw ex;
        }
    }

    public static ObjectBodyTemplate of(RemoteInvocation remoteInvocation) throws IOException {
        return new ObjectBodyTemplate(remoteInvocation);
    }

    /**
     * Serialize the given RemoteInvocation into a ByteArrayOutputStream.
     *
     * @param invocation the RemoteInvocation object
     * @return a ByteArrayOutputStream with the serialized RemoteInvocation
     * @throws IOException if thrown by I/O methods
     */
    private ByteArrayOutputStream getByteArrayOutputStream(RemoteInvocation invocation) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(SERIALIZED_INVOCATION_BYTE_ARRAY_INITIAL_SIZE);
        writeRemoteInvocation(invocation, baos);
        return baos;
    }

    /**
     * Serialize the given RemoteInvocation to the given OutputStream.
     * <p>The default implementation gives {@code decorateOutputStream} a chance
     * to decorate the stream first (for example, for custom encryption or compression).
     * Creates an {@code ObjectOutputStream} for the final stream and calls
     * {@code doWriteRemoteInvocation} to actually write the object.
     * <p>Can be overridden for custom serialization of the invocation.
     *
     * @param invocation the RemoteInvocation object
     * @param os         the OutputStream to write to
     * @throws IOException if thrown by I/O methods
     * @see #decorateOutputStream
     * @see #doWriteRemoteInvocation
     */
    private void writeRemoteInvocation(RemoteInvocation invocation, OutputStream os) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(decorateOutputStream(os));
        try {
            doWriteRemoteInvocation(invocation, oos);
        } finally {
            oos.close();
        }
    }

    /**
     * Return the OutputStream to use for writing remote invocations,
     * potentially decorating the given original OutputStream.
     * <p>The default implementation returns the given stream as-is.
     * Can be overridden, for example, for custom encryption or compression.
     *
     * @param os the original OutputStream
     * @return the potentially decorated OutputStream
     */
    private OutputStream decorateOutputStream(OutputStream os) throws IOException {
        return os;
    }

    /**
     * Perform the actual writing of the given invocation object to the
     * given ObjectOutputStream.
     * <p>The default implementation simply calls {@code writeObject}.
     * Can be overridden for serialization of a custom wrapper object rather
     * than the plain invocation, for example an encryption-aware holder.
     *
     * @param invocation the RemoteInvocation object
     * @param oos        the ObjectOutputStream to write to
     * @throws IOException if thrown by I/O methods
     * @see ObjectOutputStream#writeObject
     */
    private void doWriteRemoteInvocation(RemoteInvocation invocation, ObjectOutputStream oos) throws IOException {
        oos.writeObject(invocation);
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public int getSize() {
        return body.length;
    }
}
