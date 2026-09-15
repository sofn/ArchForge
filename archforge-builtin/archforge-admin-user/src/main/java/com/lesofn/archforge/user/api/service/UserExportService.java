package com.lesofn.archforge.user.api.service;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Exports admin users as a FastExcel-generated xlsx workbook.
 *
 * @author sofn
 */
public interface UserExportService {

    /** Writes a single-sheet workbook of users to the given output stream. */
    void exportTo(OutputStream out) throws IOException;
}
