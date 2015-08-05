package com.sunchao.rpc.common.extension.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sunchao.rpc.common.extension.HotSwap;
import com.sunchao.rpc.common.extension.DynamicDeployFactory;
import com.sunchao.rpc.common.extension.HotSwapLoader;

/**
 * AdaptiveExtensionFactory.
 * more factories type and more products type. 
 * @author sunchao
 *
 */
@HotSwap
public class AdaptiveExtensionFactory implements DynamicDeployFactory {

	private final List<DynamicDeployFactory> factories;
	
	public AdaptiveExtensionFactory() {
		HotSwapLoader<DynamicDeployFactory> loader =  HotSwapLoader.getExtensionLoader(DynamicDeployFactory.class);
		List<DynamicDeployFactory> list = new ArrayList<DynamicDeployFactory>();// get extension factory instance.
		for (String name : loader.getSupportedExtensions()) { // get the extension point names all.
			list.add(loader.getExtension(name));  // load the all extension factory instances.
		}
		factories = Collections.unmodifiableList(list);
	}
	
	public <T> T getExtension(Class<T> type, String name) {
		for (DynamicDeployFactory factory : factories) {
			T extension  = factory.getExtension(type, name); // delegate all the factories to get the specified tuple(type, name) extension instance.
			if (extension !=  null) {
				return extension;
			}
		}
		return null;
	}

}
