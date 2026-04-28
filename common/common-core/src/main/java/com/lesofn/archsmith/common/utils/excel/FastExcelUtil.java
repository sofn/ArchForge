package com.lesofn.archsmith.common.utils.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Thin convenience wrapper around {@code org.dhatim:fastexcel} for ArchSmith Excel I/O.
 *
 * <p>FastExcel is the project-standard Excel library; EasyExcel is forbidden by Gradle build guard.
 *
 * @author sofn
 */
@NullMarked
public final class FastExcelUtil {

    private FastExcelUtil() {}

    /** Writes a single sheet with a header row plus rows of arbitrary objects. */
    public static void write(
            OutputStream out, String sheetName, List<String> headers, List<? extends List<?>> rows)
            throws IOException {
        try (Workbook wb = new Workbook(out, "ArchSmith", "1.0")) {
            Worksheet ws = wb.newWorksheet(sheetName);
            for (int c = 0; c < headers.size(); c++) {
                ws.value(0, c, headers.get(c));
            }
            for (int r = 0; r < rows.size(); r++) {
                List<?> row = rows.get(r);
                for (int c = 0; c < row.size(); c++) {
                    @Nullable Object v = row.get(c);
                    if (v == null) continue;
                    switch (v) {
                        case Number n -> ws.value(r + 1, c, n);
                        case Boolean b -> ws.value(r + 1, c, b);
                        default -> ws.value(r + 1, c, v.toString());
                    }
                }
            }
            ws.finish();
        }
    }

    /** Reads the first sheet, skipping the header row, returning all cells as strings. */
    public static List<List<String>> readFirstSheet(InputStream in) throws IOException {
        List<List<String>> result = new ArrayList<>();
        try (ReadableWorkbook wb = new ReadableWorkbook(in)) {
            Sheet sheet = wb.getFirstSheet();
            if (sheet == null) {
                throw new IOException("workbook has no sheets");
            }
            try (java.util.stream.Stream<Row> stream = sheet.openStream()) {
                stream.skip(1) // header
                        .forEach(
                                (Row r) -> {
                                    List<String> cells =
                                            r.stream()
                                                    .map(
                                                            cell ->
                                                                    cell == null
                                                                            ? ""
                                                                            : Objects.toString(
                                                                                    cell
                                                                                            .getRawValue(),
                                                                                    ""))
                                                    .collect(Collectors.toList());
                                    result.add(cells);
                                });
            }
        }
        return result;
    }
}
