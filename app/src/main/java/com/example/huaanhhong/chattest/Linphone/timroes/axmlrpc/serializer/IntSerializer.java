package com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.serializer;



import com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.XMLRPCException;
import com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.XMLUtil;
import com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.xmlcreator.XmlElement;

import org.w3c.dom.Element;


/**
 *
 * @author timroes
 */
public class IntSerializer implements Serializer {

	public Object deserialize(Element content) throws XMLRPCException {
		return Integer.parseInt(XMLUtil.getOnlyTextContent(content.getChildNodes()));
	}

	public XmlElement serialize(Object object) {
		return XMLUtil.makeXmlTag(SerializerHandler.TYPE_INT,
				object.toString());
	}

}
