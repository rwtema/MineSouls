package com.rwtema.minesouls.config;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ConfigHandler {
	public static Configuration configuration;
	static ArrayList<Field> configFields = new ArrayList<>();
	static Map<Class, Property.Type> types = ImmutableMap.<Class, Property.Type>builder()
			.put(int.class, Property.Type.INTEGER)
			.put(boolean.class, Property.Type.BOOLEAN)
			.put(float.class, Property.Type.DOUBLE)
			.put(double.class, Property.Type.DOUBLE)
			.build();
	static HashMap<Field, Property> properties = new HashMap<>();
	static HashSet<String> configurableCategories = new HashSet<>();

	static {
		MinecraftForge.EVENT_BUS.register(ConfigHandler.class);
	}

	public static void addClass(Class clazz, boolean configurable) {
		if (configurable)
			configurableCategories.add(getSimpleName(clazz));

		for (Field field : clazz.getFields()) {
			int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
				continue;
			}

			field.setAccessible(true);
			configFields.add(field);
		}

		configFields.sort((o1, o2) -> o1.toString().compareTo(o2.toString()));
	}

	public static void buildConfig(File file) {
		try {
			TObjectIntHashMap<String> categoryHashes = new TObjectIntHashMap<>();
			for (Field field : configFields) {
				Class<?> declaringClass = field.getDeclaringClass();
				String simpleName = getSimpleName(declaringClass);
				categoryHashes.putIfAbsent(simpleName, 0);
				int hash = categoryHashes.get(simpleName);
				hash = hash * 31 + field.getName().hashCode();
				hash = hash * 31 + field.get(null).hashCode();
				hash = hash * 31 + field.getType().getName().hashCode();
				categoryHashes.put(simpleName, hash);
			}

			configuration = new Configuration(file);
			configuration.load();


			for (Field configField : configFields) {
				Class<?> type = configField.getType();
				if (!types.containsKey(type)) continue;
				String defaultValue = configField.get(null).toString();
				Class<?> declaringClass = configField.getDeclaringClass();
				String simpleName = getSimpleName(declaringClass);
				Property.Type expectedType = types.get(type);

				Property prop = configuration.get(simpleName, configField.getName(), defaultValue, null, expectedType);

				String currentDeclaredValue = prop.getString();

				if(prop.getType() != expectedType){
					configuration.getCategory(simpleName).remove(configField.getName());
					prop = configuration.get(simpleName, configField.getName(), defaultValue, null, expectedType);
				}

				properties.put(configField, prop);

				int versionHash = categoryHashes.get(simpleName);
				int foundHash = configuration.get(simpleName, "VersionHash", versionHash,"Hash Value to allow values to be updated when version changes. Set to 0 to prevent values being changed.").getInt();
				if (foundHash != versionHash && foundHash != 0) {


					if (!Objects.equals(currentDeclaredValue, defaultValue)) {

						String oldValuesCategory = simpleName + "_old_values";
						configuration.get(oldValuesCategory, configField.getName(), defaultValue, null, expectedType);
						configuration.getCategory(oldValuesCategory).setComment("Contains older values that were overwritten when the MineSouls version changed");
					}

					prop.setValue(defaultValue);
				}
			}

			reloadPropertiesFromConfig();
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	private static String getSimpleName(Class<?> declaringClass) {
		String simpleName = declaringClass.getName();
		simpleName = simpleName.substring(simpleName.lastIndexOf(".") + 1); // strip the package name
		return simpleName;
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent event) throws IllegalAccessException {
		reloadPropertiesFromConfig();
	}

	public static void reloadPropertiesFromConfig() throws IllegalAccessException {

		for (Map.Entry<Field, Property> entry : properties.entrySet()) {
			Field configField = entry.getKey();
			Class<?> type = configField.getType();
			Property prop = entry.getValue();
			Object o;
			if (type == float.class) {
				o = (float) prop.getDouble();
			} else if (type == int.class) {
				o = prop.getInt();
			} else if (type == boolean.class) {
				o = prop.getBoolean();
			} else if (type == double.class) {
				o = prop.getDouble();
			} else {
				throw new IllegalStateException();
			}

			configField.set(null, o);
		}
		configuration.save();
	}

}
