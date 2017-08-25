package com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.serializer;

import com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.XMLRPCException;
import com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.XMLUtil;
import com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.xmlcreator.XmlElement;

import org.w3c.dom.Element;

/**
 *
 * @author Tim Roes
 */
public class BooleanSerializer implements Serializer {

	public Object deserialize(Element content) throws XMLRPCException {
		return (XMLUtil.getOnlyTextContent(content.getChildNodes()).equals("1"))
				? Boolean.TRUE : Boolean.FALSE;
	}

	public XmlElement serialize(Object object) {
		return XMLUtil.makeXmlTag(SerializerHandler.TYPE_BOOLEAN,
				((Boolean)object == true) ? "1" : "0");
	}

}