package com.composum.sling.nodes.update;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

@Component(
        label = "Composum Source Update Service",
        description = "service to update content trees from XML"
)
@Service(SourceUpdateService.class)
public class SourceUpdateServiceImpl implements SourceUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(SourceUpdateServiceImpl.class);

    private SAXParserFactory saxParserFactory;

    @Activate
    private void activate(final BundleContext bundleContext) {
        saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
    }

    /**
     * Make node equivalent to an XML document. General strategy: we update the attributes of all nodes according to the XML document,
     * creating nonexistent nodes along the way, and make node which nodes were present, and which were changed.
     * In a second pass, we recurse through the JCR tree again, delete nodes that were not present and update the lastModified
     * properties of nodes, below which there were changes.
     */
    @Override
    public void updateFromXml(Resource resource, InputStream inputStream) throws RepositoryException, IOException, ParserConfigurationException, SAXException {
        // FIXME remove logging output and use stream directly.
        String content = IOUtils.toString(inputStream, "UTF-8");
        LOG.info("Trying to update {} to \n{}", resource.getPath(), content);
        Node node = resource.adaptTo(Node.class);
        Session session = node.getSession();
        ByteArrayOutputStream sout = new ByteArrayOutputStream();
        session.exportDocumentView(resource.getPath(), sout, false, false);
        LOG.info("Document View:\n{}", sout.toString("UTF-8"));
        try {
            saxParserFactory.newSAXParser().parse(new ByteArrayInputStream(content.getBytes("UTF-8")), new UpdateHandler(node));
            SAXParser saxParser = saxParserFactory.newSAXParser();
            LOG.info("Now importing");
            XMLReader xmlreader = saxParser.getXMLReader();
            xmlreader.setContentHandler(session.getImportContentHandler(resource.getPath(), ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING));
            xmlreader.parse(new InputSource(new ByteArrayInputStream(content.getBytes("UTF-8"))));
            LOG.info("Have changes: {}", session.hasPendingChanges());
            session.save();
        } finally {
            LOG.info("Have changes (2): {}", session.hasPendingChanges());
            session.refresh(false); // XXX or save.
        }
    }

    protected class UpdateHandler extends DefaultHandler {

        protected Stack<Node> nodeStack = new Stack<>();

        public UpdateHandler(@Nonnull Node top) {
            nodeStack.push(top);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < attributes.getLength(); ++i) {
                if (!"CDATA".equals(attributes.getType(i))) {
                    throw new SAXException("Unknown attribute type " + attributes.getType(i) + " of " + attributes.getQName(i) + "=" + attributes.getValue(i));
                }
                buf.append(attributes.getQName(i)).append("=").append(attributes.getValue(i)).append(" ");
            }
            LOG.info("startElement({},{}", qName, buf);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            LOG.info("endElement({})", qName);
        }

        @Override
        public void startDocument() throws SAXException {
            LOG.info("startDocument");
        }

        @Override
        public void endDocument() throws SAXException {
            LOG.info("endDocument");
        }

        // error handling stuff

        @Override
        public void warning(SAXParseException e) throws SAXException {
            // better abort on any warnings - we don't expect any.
            throw e;
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            // better abort on any errors - we don't expect any.
            throw e;
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            String chars = new String(ch, start, length);
            // LOG.info("ignorableWhitespace: '{}'", chars);
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
            // That's not normal - don't know how to deal with that.
            throw new SAXException("Skipped entity " + name);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String chars = new String(ch, start, length);
            // LOG.info("characters: '{}'", chars);
            if (StringUtils.isNotBlank(chars)) {
                // That's not normal - don't know how to deal with that.
                throw new SAXException("Unexpected characters: " + chars);
            }
        }

    }
}
