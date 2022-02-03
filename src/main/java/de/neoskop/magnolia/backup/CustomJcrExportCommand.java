package de.neoskop.magnolia.backup;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.command.JcrExportCommand;
import info.magnolia.importexport.contenthandler.XmlContentHandlerFactory;
import info.magnolia.importexport.contenthandler.YamlContentHandler;
import info.magnolia.importexport.exporter.YamlExporter;
import info.magnolia.importexport.filters.NamespaceFilter;
import info.magnolia.jcr.decoration.ContentDecorator;
import info.magnolia.objectfactory.Classes;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.xml.Exporter;
import org.apache.jackrabbit.commons.xml.SystemViewExporter;
import org.xml.sax.ContentHandler;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

public class CustomJcrExportCommand extends JcrExportCommand {

    private Format format = Format.XML;

    private Class<? extends Exporter> exporterClass;

    private Map<String, ContentDecorator> filters = new HashMap<>();

    public boolean isPrettyPrint() {
        return false; // prettyPrint
    }

    public ContentHandler getContentHandler(OutputStream out, boolean prettyPrint) {
        // only namespaces allowed in JCR bootstraps are 'sv' and 'xsi'
        NamespaceFilter filter = new NamespaceFilter("sv", "xsi");
        filter.setContentHandler(XmlContentHandlerFactory.newXmlContentHandler(out));
        return filter;
    }

    public Class<? extends Exporter> getExporterClass() {
        return exporterClass == null ? SystemViewExporter.class : exporterClass;
    }

    protected Node getJCRNode(Context ctx, String repository, String path)
            throws RepositoryException {
        // TODO: check why this need to use the system context and why we can't use
        // given CTX
        final Session session = MgnlContext.getSystemContext().getJCRSession(repository);
        final Node node = session.getNode(path);
        return node;
    }

    public void backupChildren(OutputStream outputStream, String repository, String path)
            throws Exception {

        try {
            final Session session = MgnlContext.getJCRSession(repository); // don't get the session
                                                                           // from
                                                                           // getJCRNode(context),
                                                                           // that's a system
                                                                           // context with superuser
                                                                           // access!

            final ContentHandler contentHandler =
                    this.format.getContentHandler(outputStream, isPrettyPrint());

            final Exporter exporter = Classes.getClassFactory().newInstance(getExporterClass(),
                    session, contentHandler, true, true);
            final ContentDecorator contentDecorator =
                    filters.containsKey(repository) ? filters.get(repository) : new DefaultFilter();
            final Node node = contentDecorator
                    .wrapNode(getJCRNode(MgnlContext.getInstance(), repository, path));

            exporter.export(node);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // finish the stream properly if zip stream, this is not done by the IOUtils
            if (outputStream instanceof DeflaterOutputStream) {
                ((DeflaterOutputStream) outputStream).finish();
            }
            outputStream.flush();
            outputStream.close();

            // TODO: IOUtils.closeQuietly(compressionOutputStream);
        }

    }

    /**
     * Export format.
     */
    public enum Format {
        XML(SystemViewExporter.class) {
            @Override
            public ContentHandler getContentHandler(OutputStream out, boolean prettyPrint) {
                // only namespaces allowed in JCR bootstraps are 'sv' and 'xsi'
                NamespaceFilter filter = new NamespaceFilter("sv", "xsi");
                filter.setContentHandler(XmlContentHandlerFactory.newXmlContentHandler(out));
                return filter;
            }
        },

        YAML(YamlExporter.class) {
            @Override
            public ContentHandler getContentHandler(OutputStream out, boolean prettyPrint) {
                // No xml namespaces allowed in YAML export
                NamespaceFilter filter = new NamespaceFilter();
                filter.setContentHandler(new YamlContentHandler(out));
                return filter;
            }
        };

        private final Class<? extends Exporter> defaultExporter;

        Format(Class<? extends Exporter> defaultExporter) {
            this.defaultExporter = defaultExporter;
        }

        public Class<? extends Exporter> getDefaultExporterClass() {
            return defaultExporter;
        }

        protected ContentHandler getContentHandler(OutputStream out) {
            return getContentHandler(out, true);
        }

        protected abstract ContentHandler getContentHandler(OutputStream out, boolean prettyPrint);

        public static boolean isSupportedExtension(String extension) {
            return Arrays.stream(values())
                    .anyMatch(f -> StringUtils.equals(f.name(), StringUtils.upperCase(extension)));
        }
    }
}
