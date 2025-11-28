package com.i360day.invoker.filter;

import com.i360day.invoker.support.RemoteInvocation;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;

public interface RemoteFilterChain {

	boolean matches(HttpServletRequest request);

	List<Filter> getFilters();

	void doFilter(HttpServletRequest request, HttpServletResponse response, RemoteInvocation remoteInvocation);
}