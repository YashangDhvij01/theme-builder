package com.themebuilder.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkAssetImportResult {

    private int total;
    private int inserted;
    private int updated;
    private int failed;
    private List<String> errors = new ArrayList<>();

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getInserted() {
        return inserted;
    }

    public void setInserted(int inserted) {
        this.inserted = inserted;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
