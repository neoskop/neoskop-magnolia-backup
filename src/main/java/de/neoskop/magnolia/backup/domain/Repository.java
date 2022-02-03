package de.neoskop.magnolia.backup.domain;

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

    @Override
    public String toString() {
        return "{" + " workspace='" + getWorkspace() + "'" + ", path='" + getPath() + "'" + "}";
    }

}
