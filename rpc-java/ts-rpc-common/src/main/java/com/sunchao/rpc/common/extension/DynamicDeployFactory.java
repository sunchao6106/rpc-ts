package com.sunchao.rpc.common.extension;

@Component
public interface DynamicDeployFactory {

	/**
	 * Get extension.
	 * @param type
	 *           extension point type.
	 * @param name
	 *           extension name.
	 * @return
	 *           the extension point instance.
	 */
	<T>  T getExtension(Class<T> type, String name);
}
