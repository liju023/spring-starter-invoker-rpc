package com.i360day.invoker.filter;

import com.i360day.invoker.support.RemoteInvocation;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface RemoteFilterChain {

	boolean matches(HttpServletRequest request);

	List<Filter> getFilters();

	void doFilter(HttpServletRequest request, HttpServletResponse response, RemoteInvocation remoteInvocation);
}