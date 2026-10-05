import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;

/**
 * Task #3 サンプリング比較用の独立 Excel ダンプツール。
 *
 * nablarch-testing-converter の変換ロジックに依存せず、Apache POI (3.8) で
 * Excel の全セルを「シート名 / 行 / 列 / 生値」の形式で標準出力へダンプする。
 * 変換後 YAML と原本 Excel の内容一致をサンプリング確認するために使用する。
 *
 * 使い方: java -cp "<poi classpath>:." ExcelDump <excelファイルパス>
 */
public class ExcelDump {
    public static void main(String[] args) throws Exception {
        try (FileInputStream in = new FileInputStream(args[0])) {
            Workbook wb = WorkbookFactory.create(in);
            DataFormatter fmt = new DataFormatter();
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                String sheetName = sheet.getSheetName();
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        String v = fmt.formatCellValue(cell);
                        if (v == null || v.isEmpty()) {
                            continue;
                        }
                        System.out.println("[" + sheetName + "] R" + (row.getRowNum() + 1)
                                + " C" + (cell.getColumnIndex() + 1) + " = <" + v + ">");
                    }
                }
            }
        }
    }
}
