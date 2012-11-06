/*
 * Copyright 2012, Gary Piercey, All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.vaadin.data.hbnutil;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationLogger
{
	private final Logger logger;

	public ApplicationLogger(Class<?> classType)
	{
		logger = LoggerFactory.getLogger(classType);
	}

	public void error(String message)
	{
		logger.error(message);
	}

	public void error(Throwable exception)
	{
		logger.error(unwindStack(exception));
	}

	public void error(String message, Throwable exception)
	{
		logger.error(message + ": " + unwindStack(exception));
	}

	public void info(String message)
	{
		logger.info(message);
	}

	public void info(Throwable exception)
	{
		logger.info(unwindStack(exception));
	}

	public void info(String message, Throwable exception)
	{
		logger.info(message + ": " + unwindStack(exception));
	}

	public void warn(String message)
	{
		logger.warn(message);
	}

	public void warn(Throwable exception)
	{
		logger.warn(unwindStack(exception));
	}

	public void warn(String message, Throwable exception)
	{
		logger.warn(message + ": " + unwindStack(exception));
	}

	public void debug(String message)
	{
		logger.debug(message);
	}

	public void debug(Throwable exception)
	{
		logger.debug(unwindStack(exception));
	}

	public void debug(String message, Throwable exception)
	{
		logger.debug(message + ": " + unwindStack(exception));
	}

	public void trace(String message)
	{
		logger.trace(message);
	}

	public void trace(Throwable exception)
	{
		logger.trace(unwindStack(exception));
	}

	public void trace(String message, Throwable exception)
	{
		logger.trace(message + ": " + unwindStack(exception));
	}

	public void executionTrace()
	{
		if (logger.isTraceEnabled()) // speed-up when not tracing
		{
			final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			logger.trace(formatTraceMessage(null, stackTrace[2]));
		}
	}

	public void executionTrace(String message)
	{
		if (logger.isTraceEnabled()) // speed-up when not tracing
		{
			final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			logger.trace(formatTraceMessage(message, stackTrace[2]));
		}
	}

	private String formatTraceMessage(String message, StackTraceElement stackElement)
	{
		try
		{
			final StringBuilder traceMessage = new StringBuilder();

			traceMessage.append(String.format("Execution Trace: [%s:%d] %s()",
				stackElement.getFileName(), stackElement.getLineNumber(), stackElement.getMethodName()));

			if (message != null && message.length() > 0)
				traceMessage.append(": " + traceMessage);

			return traceMessage.toString();
		}
		catch (Exception e)
		{
			this.error(e);
			return "Trace unavailable due to previous errors...";
		}
	}

	public static String unwindStack(Throwable exception)
	{
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);

		exception.printStackTrace(printWriter);
		final String stackTrace = stringWriter.toString();

		return stackTrace;
	}
}
