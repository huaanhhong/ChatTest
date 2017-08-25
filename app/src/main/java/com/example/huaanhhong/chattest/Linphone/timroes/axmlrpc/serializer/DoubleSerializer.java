package com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.serializer;


import com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.XMLRPCException;
import com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.XMLUtil;
import com.example.huaanhhong.chattest.Linphone.timroes.axmlrpc.xmlcreator.XmlElement;

import org.w3c.dom.Element;

import java.text.DecimalFormat;


/**
 *
 * @author Tim Roes
 */
public class DoubleSerializer implements Serializer {

	public Object deserialize(Element content) throws XMLRPCException {
		return Double.parseDouble(XMLUtil.getOnlyTextContent(content.getChildNodes()));
	}

	public XmlElement serialize(Object object) {
		return XMLUtil.makeXmlTag(SerializerHandler.TYPE_DOUBLE,
				new DecimalFormat("#0.0#").format(((Double)object).doubleValue()));
	}

}
