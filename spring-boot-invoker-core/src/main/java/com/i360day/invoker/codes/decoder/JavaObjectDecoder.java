package com.i360day.invoker.codes.decoder;

import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.exception.InvalidContentTypeException;
import com.i360day.invoker.http.Response;
import com.i360day.invoker.support.CodebaseAwareObjectInputStream;
import com.i360day.invoker.support.RemoteInvocationResult;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.rmi.RemoteException;

/**
 * 使用jdk ObjectInputStream 解码
 */
public class JavaObjectDecoder implements Decoder {
    /**
     * 只接受的MediaType
     */
    private final MediaType acceptContentType = MediaType.parseMediaType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER);

    /**
     * jdk 解码
     * @param response
     * @param type
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public RemoteInvocationResult decode(Response response, Type type) throws IOException, ClassNotFoundException {

        if (!acceptContentType.toString().equals(response.getContentType())) {
            throw new InvalidContentTypeException(String.format("The response [%s] does not match the context [%s]", MediaType.APPLICATION_OCTET_STREAM_VALUE, response.getContentType()));
        }

        return readRemoteInvocationResult(response.getBody(), null);
    }

    /**
     * Deserialize a RemoteInvocationResult object from the given InputStream.
     * <p>Gives {@code decorateInputStream} a chance to decorate the stream
     * first (for example, for custom encryption or compression). Creates an
     * {@code ObjectInputStream} via {@code createObjectInputStream} and
     * calls {@code doReadRemoteInvocationResult} to actually read the object.
     * <p>Can be overridden for custom serialization of the invocation.
     *
     * @param body        the InputStream to read from
     * @param codebaseUrl the codebase URL to load classes from if not found locally
     * @return the RemoteInvocationResult object
     * @throws IOException            if thrown by I/O methods
     * @throws ClassNotFoundException if thrown during deserialization
     * @see #createObjectInputStream
     * @see #doReadRemoteInvocationResult
     */
    protected RemoteInvocationResult readRemoteInvocationResult(Response.Body body, @Nullable String codebaseUrl)
            throws IOException, ClassNotFoundException {

        ObjectInputStream ois = createObjectInputStream(body.asInputStream(), codebaseUrl);
        try {
            return doReadRemoteInvocationResult(ois);
        } finally {
            ois.close();
        }
    }

    /**
     * Create an ObjectInputStream for the given InputStream and codebase.
     * The default implementation creates a CodebaseAwareObjectInputStream.
     *
     * @param is          the InputStream to read from
     * @param codebaseUrl the codebase URL to load classes from if not found locally
     *                    (can be {@code null})
     * @return the new ObjectInputStream instance to use
     * @throws IOException if creation of the ObjectInputStream failed
     * @see CodebaseAwareObjectInputStream
     */
    protected ObjectInputStream createObjectInputStream(InputStream is, @Nullable String codebaseUrl) throws IOException {
        return new CodebaseAwareObjectInputStream(is, null, codebaseUrl);
    }

    /**
     * Perform the actual reading of an invocation object from the
     * given ObjectInputStream.
     * <p>The default implementation simply calls {@code readObject}.
     * Can be overridden for deserialization of a custom wrapper object rather
     * than the plain invocation, for example an encryption-aware holder.
     *
     * @param ois the ObjectInputStream to read from
     * @return the RemoteInvocationResult object
     * @throws IOException            if thrown by I/O methods
     * @throws ClassNotFoundException if the class name of a serialized object
     *                                couldn't get resolved
     * @see ObjectOutputStream#writeObject
     */
    protected RemoteInvocationResult doReadRemoteInvocationResult(ObjectInputStream ois) throws IOException, ClassNotFoundException {

        Object obj = ois.readObject();
        if (!(obj instanceof RemoteInvocationResult)) {
            throw new RemoteException("Deserialized object needs to be assignable to type [" + RemoteInvocationResult.class.getName() + "]: " + ClassUtils.getDescriptiveType(obj));
        }
        return (RemoteInvocationResult) obj;
    }
}
