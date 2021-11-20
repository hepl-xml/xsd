import javax.xml.parsers.SAXParserFactory;
import javax.xml.XMLConstants;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

class MyErrorHandler implements ErrorHandler {

    @Override
    public void warning(SAXParseException exception) throws SAXException {
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        throw new SAXException(exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        throw new SAXException(exception);
    }
}

class MyContentHandler extends DefaultHandler {
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String s = new String(ch, start, length);
        System.out.printf("[%s]\n",s);
    }
}

public class MyValidator {

    public static final Path BASEPATH = Paths.get("/Users/ludo/Documents/cours/xml/slides/src/xsd/ex03");

    public static void main(String args[]) throws Exception {
        if (args.length > 2) {
            System.out.printf("Usage: java MyValidator [<doc.xml> [<schema.xsd>]]\n");
            System.exit(1);
        }

        URI fileURI = null;
        URI schemaURI = null;
        switch (args.length) {
            case 0:
                fileURI = BASEPATH.resolve("book1.xml").toUri();
                schemaURI = BASEPATH.resolve("book1.xsd").toUri();
                break;
            case 1:
                fileURI = URI.create(args[0]);
                schemaURI = BASEPATH.resolve("books1.xsd").toUri();
                break;
            case 2:
                fileURI = URI.create(args[0]);
                schemaURI = URI.create(args[1]);
                break;
        }

        System.out.printf("Document URI: %s\n", fileURI);
        System.out.printf("Schema URI: %s\n", schemaURI);

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        //SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
        System.out.printf("schemaFactory: %s\n", schemaFactory.getClass().getName());
        Schema schema = schemaFactory.newSchema(schemaURI.toURL());
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setValidating(false);
        saxFactory.setNamespaceAware(true);
        saxFactory.setSchema(schema);

        XMLReader parser = saxFactory.newSAXParser().getXMLReader();
        //parser.setFeature("http://xml.org/sax/features/namespaces", true);
        //parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);

        parser.setErrorHandler(new MyErrorHandler());
        parser.setContentHandler(new MyContentHandler());
        parser.parse(fileURI.toString());
        System.out.println("Document is valid. Parsed with SaX.");

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
        Document document = domBuilder.parse(fileURI.toString());
        Validator validator = schema.newValidator();
        validator.setErrorHandler(new MyErrorHandler());
        validator.validate(new DOMSource(document));
        System.out.println("Document is valid. Parsed with DOM (no DOMResult).");

//        DOMResult domResult = new DOMResult();
//        validator.validate(new DOMSource(document), domResult);
//        System.out.println("Document is valid. Parsed with DOM (DOMResult).");
//
//        TransformerFactory transFactory = TransformerFactory.newInstance();
//        Transformer idTransform = transFactory.newTransformer();
//        idTransform.setOutputProperty(OutputKeys.METHOD, "xml");
//        idTransform.setOutputProperty(OutputKeys.INDENT, "yes");
//        idTransform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//        Source input = new DOMSource(domResult.getNode());
//        Result output = new StreamResult(System.out);
//        idTransform.transform(input, output);
    }
}
