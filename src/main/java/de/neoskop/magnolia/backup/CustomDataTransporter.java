package de.neoskop.magnolia.backup;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.importexport.filters.AccesscontrolNodeFilter;
import info.magnolia.importexport.filters.ImportXmlRootFilter;
import info.magnolia.importexport.filters.MagnoliaV2Filter;
import info.magnolia.importexport.filters.RemoveMixversionableFilter;
import info.magnolia.importexport.filters.VersionFilter;
import info.magnolia.importexport.postprocessors.ActivationStatusImportPostProcessor;
import info.magnolia.importexport.postprocessors.MetaDataImportPostProcessor;
import info.magnolia.importexport.postprocessors.UpdateVersionMixinPostProcessor;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class CustomDataTransporter extends DataTransporter {

    private static Logger log = LoggerFactory.getLogger(CustomDataTransporter.class.getName());

    /**
     * Created CustomDataTransporter from Magnolia DataTransporter Class, because the method
     * closeQuietly is not allowed to execute.
     * 
     * Imports XML stream into repository. XML is filtered by <code>MagnoliaV2Filter</code>,
     * <code>VersionFilter</code> and <code>ImportXmlRootFilter</code> if
     * <code>keepVersionHistory</code> is set to <code>false</code>
     *
     * @param xmlStream XML stream to import
     * @param repositoryName selected repository
     * @param basepath base path in repository
     * @param name (absolute path of <code>File</code>)
     * @param keepVersionHistory if <code>false</code> version info will be stripped before
     *        importing the document
     * @param forceUnpublishState if <code>true</code> then activation state of node will be change
     *        to unpublished.
     * @param importMode a valid value for ImportUUIDBehavior
     * @see ImportUUIDBehavior
     * @see ImportXmlRootFilter
     * @see VersionFilter
     * @see MagnoliaV2Filter
     */
    public static synchronized void importXmlStream(InputStream xmlStream, String repositoryName,
            String basepath, String name, boolean keepVersionHistory, boolean forceUnpublishState,
            int importMode, boolean saveAfterImport, boolean createBasepathIfNotExist)
            throws IOException {

        // TODO hopefully this will be fixed with a more useful message with the
        // Bootstrapper refactoring
        if (xmlStream == null) {
            throw new IOException("Can't import a null stream into repository: " + repositoryName
                    + ", basepath: " + basepath + ", name: " + name);
        }
        try {
            Session session = MgnlContext.getJCRSession(repositoryName);

            log.debug("Importing content into repository: [{}] from: [{}] into path: [{}]",
                    repositoryName, name, basepath);

            if (session.nodeExists(basepath) && "/".equals(name) && session.nodeExists(name)) {
                NodeIterator subNodes = session.getNode(basepath).getNodes();
                try {
                    while (subNodes.hasNext()) {
                        Node nextNode = subNodes.nextNode();
                        if (!nextNode.getPath().contains("jcr:system")) {
                            nextNode.remove();
                            session.save();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (session.nodeExists(basepath) && !"/".equals(name)
                    && session.nodeExists(name)) {
                session.getNode(name).remove();
                session.save();
            }

            if (!session.nodeExists(basepath) && createBasepathIfNotExist) {
                try {
                    NodeUtil.createPath(session.getRootNode(), basepath, NodeTypes.Content.NAME);
                } catch (RepositoryException e) {
                    log.error("can't create path [{}]", basepath);
                }
            }

            // Collects a list with all nodes at the basepath before import so we can see
            // exactly which nodes were imported afterwards
            List<Node> nodesBeforeImport =
                    NodeUtil.asList(NodeUtil.asIterable(session.getNode(basepath).getNodes()));

            if (keepVersionHistory) {
                // do not manipulate
                session.importXML(basepath, xmlStream, importMode);
            } else {
                // create readers/filters and chain
                SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                saxParserFactory.setNamespaceAware(true);
                SAXParser saxParser = saxParserFactory.newSAXParser();
                XMLReader initialReader = saxParser.getXMLReader();
                try {
                    initialReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl",
                            true);
                } catch (SAXException e) {
                    log.error("could not set parser feature");
                }

                XMLFilter magnoliaV2Filter = null;

                // if stream is from regular file, test for belonging XSL file to apply XSL
                // transformation to XML
                if (new File(name).isFile()) {
                    InputStream xslStream = DataTransporter.getXslStreamForXmlFile(new File(name));
                    if (xslStream != null) {
                        Source xslSource = new StreamSource(xslStream);
                        SAXTransformerFactory saxTransformerFactory =
                                (SAXTransformerFactory) SAXTransformerFactory.newInstance();
                        XMLFilter xslFilter = saxTransformerFactory.newXMLFilter(xslSource);
                        magnoliaV2Filter = new MagnoliaV2Filter(xslFilter);
                    }
                }

                if (magnoliaV2Filter == null) {
                    magnoliaV2Filter = new MagnoliaV2Filter(initialReader);
                }

                XMLFilter versionFilter = new VersionFilter(magnoliaV2Filter);

                // enable this to strip useless "name" properties from dialogs
                // versionFilter = new UselessNameFilter(versionFilter);

                // enable this to strip mix:versionable from pre 3.6 xml files
                versionFilter = new RemoveMixversionableFilter(versionFilter);

                // strip rep:accesscontrol node
                versionFilter = new AccesscontrolNodeFilter(versionFilter);

                XMLReader finalReader = new ImportXmlRootFilter(versionFilter);

                ContentHandler handler = session.getImportContentHandler(basepath, importMode);
                finalReader.setContentHandler(handler);

                // parse XML, import is done by handler from session
                try {
                    finalReader.parse(new InputSource(xmlStream));
                } finally {
                    // IOUtils.closeQuietly(xmlStream);
                }

                if (((ImportXmlRootFilter) finalReader).rootNodeFound) {
                    String path = basepath;
                    if (!path.endsWith(DataTransporter.SLASH)) {
                        path += DataTransporter.SLASH;
                    }

                    Node dummyRoot = (Node) session.getItem(path + DataTransporter.JCR_ROOT);
                    for (Iterator iter = dummyRoot.getNodes(); iter.hasNext();) {
                        Node child = (Node) iter.next();
                        // move childs to real root

                        if (session.itemExists(path + child.getName())) {
                            session.getItem(path + child.getName()).remove();
                        }

                        session.move(child.getPath(), path + child.getName());
                    }
                    // delete the dummy node
                    dummyRoot.remove();
                }

                // Post process all nodes that were imported
                VersionManager versionManager = Components.getComponent(VersionManager.class);
                NodeIterator nodesAfterImport = session.getNode(basepath).getNodes();
                while (nodesAfterImport.hasNext()) {
                    Node nodeAfterImport = nodesAfterImport.nextNode();
                    boolean existedBeforeImport = false;
                    for (Node nodeBeforeImport : nodesBeforeImport) {
                        if (NodeUtil.isSame(nodeAfterImport, nodeBeforeImport)) {
                            existedBeforeImport = true;
                            break;
                        }
                    }
                    if (!existedBeforeImport) {
                        postProcessAfterImport(nodeAfterImport, forceUnpublishState, importMode,
                                versionManager);
                    }
                }
            }
            if (saveAfterImport) {
                session.save();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error importing " + name + ": " + e.getMessage(), e);
        } finally {
            // IOUtils.closeQuietly(xmlStream);
        }
    }

    private static void postProcessAfterImport(Node node, boolean forceUnpublishState,
            int importMode, VersionManager versionManager) throws RepositoryException {
        try {
            new MetaDataImportPostProcessor().postProcessNode(node);
            if (forceUnpublishState) {
                new ActivationStatusImportPostProcessor().postProcessNode(node);
            }
            new UpdateVersionMixinPostProcessor(importMode, versionManager).postProcessNode(node);
        } catch (RepositoryException e) {
            throw new RepositoryException("Failed to post process imported nodes at path "
                    + NodeUtil.getNodePathIfPossible(node) + ": " + e.getMessage(), e);
        }
    }
}
