package de.neoskop.magnolia.backup.domain;

import org.apache.commons.lang3.StringUtils;

import info.magnolia.importexport.DataTransporter;

public class Repository {
    private String workspace;
    private String path;

    public Repository(String workspace, String path) {
        this.workspace = workspace;
        this.path = path;
    }

    public String getWorkspace() {
        return this.workspace;
    }

    public String getPath() {
        return this.path;
    }

    public String getFilename() {
        String pathName = DataTransporter.createExportPath(path);
        pathName = DataTransporter.encodePath(pathName, DataTransporter.DOT, DataTransporter.UTF8);
        if (DataTransporter.DOT.equals(pathName)) {
            pathName = StringUtils.EMPTY; // root node
        }
        return workspace + pathName;
    }

    @Override
    public String toString() {
        return "{" + " workspace='" + getWorkspace() + "'" + ", path='" + getPath() + "'" + "}";
    }

}
