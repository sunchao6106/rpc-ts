package com.sunchao.rpc.common.extension.factory;
import com.sunchao.rpc.common.extension.DynamicDeployFactory;
import com.sunchao.rpc.common.extension.HotSwapLoader;
import com.sunchao.rpc.common.extension.Component;

/*
 * SpiExtensionFactory.
 * a single factory and more products type.
 * get adaptive extension
 */
public class ComponentExtensionFactory implements DynamicDeployFactory {

	public <T> T getExtension(Class<T> type, String name) {
		if (type.isInterface() && type.isAnnotationPresent(Component.class)) {
			HotSwapLoader<T> loader = HotSwapLoader.getExtensionLoader(type);
			if (loader.getSupportedExtensions().size() > 0) {
				return loader.getAdaptiveExtension();
			}
		}
		return null;
	}

}
