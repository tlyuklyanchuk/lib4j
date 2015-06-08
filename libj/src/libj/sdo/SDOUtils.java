package libj.sdo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import libj.debug.Log;
import libj.dom.DataDoc;
import libj.dom.DataNode;
import libj.dom.ListDataNode;
import libj.dom.MapDataNode;
import libj.utils.Text;
import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;

public class SDOUtils {

	public static Map<String, Type> getTypeMap(Type type) {

		Map<String, Type> typeMap = new LinkedHashMap<String, Type>();

		@SuppressWarnings("unchecked")
		List<Property> boProps = type.getProperties();

		for (Property p : boProps) {
			typeMap.put(p.getName(), p.getType());
		}

		return typeMap;
	}

	public static Map<String, Property> getTypePropMap(Type type) {

		Map<String, Property> typeMap = new LinkedHashMap<String, Property>();

		@SuppressWarnings("unchecked")
		List<Property> boProps = type.getProperties();

		for (Property p : boProps) {
			typeMap.put(p.getName(), p);
		}

		return typeMap;
	}

	public static void printTypeMap(Type type) {

		Log.info("### Describe data type '%s': ###", type.getName());

		Map<String, Property> propMap = getTypePropMap(type);

		for (String propName : propMap.keySet()) {

			Property prop = propMap.get(propName);
			Type propType = prop.getType();

			Log.info("%s (%s)", propName, propType.getName().concat(prop.isContainment() ? "[]" : Text.EMPTY_STRING));

			if (propType.getProperties().size() != 0) {
				printTypeMap(propType);
			}
		}
	}

	public static void printDataObjectMap(DataObject dataObject) {

		printTypeMap(dataObject.getType());
	}

	public static DataObject createChildObject(DataObject dataObject, String childName) {

		return dataObject.createDataObject(childName);
	}

	public static DataObject createChildObject(DataObject dataObject, Property property) {

		return dataObject.createDataObject(property);
	}

	private static void convertToDataDoc(DataObject bo, DataNode node) {

		Log.trace("### Node: %s ###", node.getName());

		Type type = bo.getType();
		Map<String, Property> propMap = SDOUtils.getTypePropMap(type);

		for (String propName : propMap.keySet()) {

			Property prop = propMap.get(propName);
			// Type propType = prop.getType();
			boolean isList = prop.isMany();
			boolean isContainer = prop.isContainment();

			Log.trace("propName=%s, isContainer=%b, isList=%b", propName, isContainer, isList);

			if (isList) {

				@SuppressWarnings("unchecked")
				List<DataObject> list = bo.getList(propName);
				ListDataNode listNode = node.createList(propName);

				for (DataObject o : list) {

					DataNode itemNode = new MapDataNode(propName);
					listNode.add(itemNode);
					convertToDataDoc(o, itemNode);
				}

			} else if (isContainer) {

				DataNode childNode = node.create(propName);
				convertToDataDoc(bo.getDataObject(propName), childNode);

			} else {

				node.set(propName, bo.get(propName));
			}
		}
	}

	public static DataDoc convertToDataDoc(DataObject dataObject) {

		DataDoc dataDoc = new DataDoc(dataObject.getType().getName());

		convertToDataDoc(dataObject, dataDoc.getRoot());

		return dataDoc;
	}

}
