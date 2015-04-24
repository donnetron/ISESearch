package isespider;

/*
SAMPLE <doc> ELEMENT CHUNK
	<doc>
		<field name="UID">0000000</field>
		<field name="company">Titan Europe 2007-2</field>
		<field name="datetime">2012-10-22T14:14:00Z</field>
		<field name="title">Notice</field>
		<field name="url">http://www.ise.ie/app/announcementDetails.aspx?ID=11370839</field>
		<field name="content">Company name Irish Stock Exchange Headline MSM...............</field>
	</doc>
*/

import javax.xml.parsers.*;
import javax.xml.transform.*;  
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ISEDOMNotice {
	Logger logger = LogManager.getLogger(this.getClass().getName());

	Document doc;
	XPath xpath;

	public ISEDOMNotice() {
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            xpath = XPathFactory.newInstance().newXPath();

			// <doc>
			Element rootElement = doc.createElement("doc");
			doc.appendChild(rootElement);

				// <field name="UID">0000000</field>
				Element uid = doc.createElement("field");
				rootElement.appendChild(uid);
				uid.setAttribute("name", "UID");
				
				// <field name="company">Titan Europe 2007-2</field>
				Element company = doc.createElement("field");
				rootElement.appendChild(company);
				company.setAttribute("name", "company");
				
				// <field name="datetime">2012-10-22T14:14:00Z</field>
				Element datetime = doc.createElement("field");
				rootElement.appendChild(datetime);
				datetime.setAttribute("name", "datetime");

				// <field name="title">Notice</field>
				Element title = doc.createElement("field");
				rootElement.appendChild(title);
				title.setAttribute("name", "title");
				
				// <field name="url">http://www.ise.ie/app/announcementDetails.aspx?ID=11370839</field>
				Element url = doc.createElement("field");
				rootElement.appendChild(url);
				url.setAttribute("name", "url");
				
				// <field name="notice">
				Element content = doc.createElement("field");
				rootElement.appendChild(content);
				content.setAttribute("name", "notice");
				
				// <field name="cachefile">
				Element cacheFile = doc.createElement("field");
				rootElement.appendChild(cacheFile);
				cacheFile.setAttribute("name", "cachefile");
		}
		catch (ParserConfigurationException | DOMException e) { logger.catching(e); }
	}

	public ISEDOMNotice(DocumentFragment notice) {
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			xpath = XPathFactory.newInstance().newXPath();

			Node documentFragmentNode = doc.importNode(notice, true);
			doc.appendChild(documentFragmentNode);
		}
		catch (ParserConfigurationException | DOMException e) { logger.catching(e); }
	}
	
	// setter methods
	public void setUID(String uid) {
		setNodeValue("/doc/field[@name='UID']", uid);
	}

	public void setCompany(String company) {
		setNodeValue("/doc/field[@name='company']", company);
	}

	public void setDateTime(String dateTime) {
		setNodeValue("/doc/field[@name='datetime']", dateTime);
	}

	public void setTitle(String title) {
		setNodeValue("/doc/field[@name='title']", title);
	}

	public void setURL(String url) {
		setNodeValue("/doc/field[@name='url']", url);
	}

	public void setContent(String content) {
		setNodeValue("/doc/field[@name='notice']", content);
	}
	
	public void setCacheFile(String cachefile) {
		setNodeValue("/doc/field[@name='cachefile']", cachefile);
	}

	// getter methods
	public String getUID() {
		return getNodeValue("/doc/field[@name='UID']");
 	}
	
	public String getCompany() {
		return getNodeValue("/doc/field[@name='company']");
 	}

	public String getDateTime() {
		return getNodeValue("/doc/field[@name='datetime']");
 	}

	//special method to get year
	public String getDateTimeYear() {
		return getNodeValue("/doc/field[@name='datetime']").substring(0,4);
 	}

	public String getTitle() {
		return getNodeValue("/doc/field[@name='title']");
 	}

	public String getURL() {
		return getNodeValue("/doc/field[@name='url']");
 	}

	public String getContent() {
		return getNodeValue("/doc/field[@name='notice']");
 	}	
	
	public String getCacheFile() {
		return getNodeValue("/doc/field[@name='cachefile']");
 	}	

	// get entire document
	public Document getDocument() {
		return doc;
	}

	// get entire document as a DocumentFragment
	public DocumentFragment getDocumentFragment() {
		DocumentFragment df = doc.createDocumentFragment();
		df.appendChild(doc.getFirstChild());
		return df;
	}

	// get entire document as a Node
	public Node getNodeSet() {
		return doc.getFirstChild();
	}

	// private generic methods
	private void setNodeValue(String xPathExpression, String value) {
		try {
			Node companyNode = (Node)xpath.evaluate(xPathExpression, doc, XPathConstants.NODE);
			companyNode.setTextContent(value);
		}
		catch (XPathExpressionException e) { logger.catching(e); }
	}

	private String getNodeValue(String xPathExpression) {
		try {
			return xpath.compile(xPathExpression).evaluate(doc);
		}
		catch (XPathExpressionException e) { logger.catching(e); }
		return null;
	}

	//debug methods
	public void printDocument() {
		logger.info(getDocumentString());
	}

	//uses fully qualified class names to avoid importing everything for using just once
	public String getDocumentString() {
		try {
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			java.io.Writer out = new java.io.StringWriter();
			tf.transform(new DOMSource(doc), new StreamResult(out));
			return(out.toString());
		}
		catch (TransformerException e) { logger.catching(e); }
		return null;
	}

}