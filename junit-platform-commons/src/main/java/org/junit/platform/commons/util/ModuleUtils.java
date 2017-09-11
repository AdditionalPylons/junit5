/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * Collection of utilities for working with modules.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.1
 */
@API(status = INTERNAL, since = "1.1")
public final class ModuleUtils {

	/**
	 * Class finder service providing interface.
	 */
	public interface ClassFinder {

		/**
		 * Return list of classes of the passed-in module that contains potential testable methods.
		 *
		 * @param moduleName name of the module to inspect
		 * @param classNameFilter filter to apply to the fully qualified class name
		 * @param classTester filter to apply to each class instance
		 * @return list of classes
		 */
		List<Class<?>> findAllClassesInModule(String moduleName, Predicate<Class<?>> classTester,
				Predicate<String> classNameFilter);
	}

	private static final Logger logger = LoggerFactory.getLogger(ModuleUtils.class);

	///CLOVER:OFF
	private ModuleUtils() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * TODO Add Javadoc to ModuleUtils.findAllClassesInModule
	 */
	public static List<Class<?>> findAllClassesInModule(String moduleName, Predicate<Class<?>> classTester,
			Predicate<String> classNameFilter) {
		Preconditions.notBlank(moduleName, "module name must not be null or blank");
		Preconditions.notNull(classTester, "class tester must not be null");
		Preconditions.notNull(classNameFilter, "class name filter must not be null");

		ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
		List<Class<?>> classes = new ArrayList<>();

		logger.config(() -> "Loading auto-detected class finders...");
		int count = 0;
		for (ClassFinder classFinder : ServiceLoader.load(ClassFinder.class, classLoader)) {
			classes.addAll(classFinder.findAllClassesInModule(moduleName, classTester, classNameFilter));
			count++;
		}
		if (count == 0) {
			logger.warn(() -> "No module class finder service registered! No test classes found.");
		}
		return Collections.unmodifiableList(classes);
	}

}
