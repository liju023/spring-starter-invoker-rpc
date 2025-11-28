package com.i360day.invoker;

import com.i360day.invoker.security.InvokerSecurityAdapter;
import com.i360day.invoker.support.RemoteInvocation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author liju.z
 */
class NullInvokerSecurityAdapter implements InvokerSecurityAdapter {

    @Override
    public void check(HttpServletRequest request, HttpServletResponse response, RemoteInvocation remoteInvocation) {

    }

    /**
     * 验证签名
     *
     * @param request
     * @param response
     * @param invocation
     */
//    private void signVerify(HttpServletRequest request, HttpServletResponse response, RemoteInvocation invocation) throws NoSuchMethodException {
////        Method method = invocation.getMethod(getService());
////        String signValue = HttpInvokerRemoteConstant.sign(invocation.getMethodName(), method.getReturnType(), invocation.getArguments());
//        String signValue = HttpInvokerRemoteConstant.sign(method, invocation.getArguments());
//        if (!StringUtils.isEquals(signValue, request.getHeader(HttpInvokerRemoteConstant.HTTP_INVOKER_AUTHORIZATION_KEY))) {
//            response.setStatus(HttpStatus.FORBIDDEN.value());
//            throw new InvalidSignatureException("Invalid signature " + signValue);
//        }
//    }
}
