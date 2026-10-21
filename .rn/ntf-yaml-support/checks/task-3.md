# task-3 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 変換対象の全 Excel に対応する YAML が生成されている | OK | 対象 Excel 2件（`form/ProjectFormTest.xls` 3シート、`action/ProjectSaveActionRequestTest.xlsx` 4シート）。`mvn com.nablarch.framework:nablarch-testing-converter:1.0.0-SNAPSHOT:convert` → `Converted 2 file(s).` / BUILD SUCCESS、WARN（重複カラム）なし。シート名＝YAML ファイル名で計7 YAML（`<テストクラス名>/` ディレクトリ配下）。シート数と YAML 数が一致し、欠落なし | | |
| サンプリング比較で Excel と YAML の内容が一致している | OK | POI 独立ダンプ（`scripts/ExcelDump.java`）と PyYAML 読込（`scripts/compare_excel_yaml.py`）で LIST_MAP / SETUP_TABLE / EXPECTED_TABLE / MESSAGE の全ブロックを全件照合: ProjectSaveActionRequestTest 120項目・ProjectFormTest 1168項目、mismatch 0。各ファイル3件以上のサンプルは下記 Sampling Comparison | | |
| YAML がコミットされている | OK | 7 YAML・scripts・steering.md・task-3.md を Task #3 コミットに含む。Excel 2件は未削除（Task #4 で削除） | | |

### 使用スクリプト

- `scripts/ExcelDump.java`: converter の変換ロジックに依存せず、Apache POI 3.8 で Excel の全セルを `[シート] R<行> C<列> = <値>` 形式でダンプする（過去の移行作業で作成したものを履歴 `f46e98e` から復元）。
- `scripts/compare_excel_yaml.py`: ダンプと YAML（PyYAML、YAML 1.1 の bool 解釈を無効化したローダ）を突き合わせ、全ブロックの全セル・メッセージの directives / field 定義（type・length）/ 本文を照合し、各シートのサンプルを出力する。比較規則は、空セル↔`""`、テーブルの `null`↔YAML null、前後ダブルクォート付きセル↔クォート除去値（QuotationTrimmer 相当）、`[...]` マーカー列・`//` コメントは対象外。
  - 実行: `python3 -I compare_excel_yaml.py <dump.txt> <YAMLディレクトリ> [サンプル件数]`
  - 初回実行で出た NG 9件は、いずれもスクリプト側の不備だったため修正した。(1) PyYAML（YAML 1.1）がキー `no` を bool `False` と解釈していた（実行時に使う nablarch-testing-yaml は snakeyaml-engine＝YAML 1.2 で文字列 `no` として読む）。(2) 複数行セルのダンプ行解析に誤りがあった。修正後は mismatch 0。

## Sampling Comparison（task-3 のみ）
| YAML ファイル | 比較件数 | レコードキー/行 | Excel 値 | YAML 値 | 一致 |
| --- | --- | --- | --- | --- | --- |
| action/ProjectSaveActionRequestTest/setUpDb.yaml | 3（全件） | SETUP_TABLE=PROJECT / INDUSTRY / CLIENT | ヘッダのみ（データ行なし）×3 | `rows: []` ×3 | OK |
| action/ProjectSaveActionRequestTest/testNormalEndExistAllFields.yaml | 3 | MESSAGE=setUpMessages row1 JSON（type/length） | 全半角 / 403、JSON本文（改行・インデント・全角含む14行） | `{name: "JSON", type: "全半角", length: "403"}`、本文完全一致 | OK |
| 〃 | | MESSAGE=expectedMessages row1 JSON | `{"statusCode":"201"}` | `{"statusCode":"201"}` | OK |
| 〃 | | EXPECTED_TABLE[1]=PROJECT row1 PROJECT_NAME / USER_ID | プロジェクト９９９ / `null` | プロジェクト９９９ / null | OK |
| action/ProjectSaveActionRequestTest/testNormalEndOnlyRequireFields.yaml | 3 | MESSAGE=setUpMessages row1 JSON | 任意項目 null の JSON 本文 | 本文完全一致 | OK |
| 〃 | | SETUP_TABLE[1]=CLIENT row1 CLIENT_NAME | テスト顧客名 | テスト顧客名 | OK |
| 〃 | | EXPECTED_TABLE[1]=PROJECT row1 PROJECT_MANAGER | `null` | null | OK |
| action/ProjectSaveActionRequestTest/testAbNormalEnd.yaml | 3 | LIST_MAP=testShots row1 description | バリデーションエラーが発生し、DBにプロジェクトが登録されないこと。 | 同左 | OK |
| 〃 | | MESSAGE=setUpMessages row1 JSON | projectName/projectType/projectClass が空の JSON 本文 | 本文完全一致 | OK |
| 〃 | | MESSAGE=expectedMessages row1 JSON | `{"statusCode":"400"}` | `{"statusCode":"400"}` | OK |
| form/ProjectFormTest/testCharsetAndLength.yaml | 3 | LIST_MAP=charsetAndLength row1 max | 64 | "64" | OK |
| 〃 | | row1 半角英字 | x | "x" | OK |
| 〃 | | row2 allowEmpty | o | "o" | OK |
| form/ProjectFormTest/testSingleValidation.yaml | 3 | LIST_MAP=singleValidation row4 input1 / messageId | hoge / {nablarch.core.validation.ee.EnumElement.message} | 同左 | OK |
| 〃 | | row49 input1（Excel R52C2） | `"?"`（前後ダブルクォート付き） | "?"（デコード値 `?`） | OK（※） |
| 〃 | | row67 input1（Excel R70C2） | `"?"`（前後ダブルクォート付き） | "?"（デコード値 `?`） | OK（※） |
| form/ProjectFormTest/testBeanValidation.yaml | 3 | LIST_MAP=testShots row1 title | 正常系-1 | 正常系-1 | OK |
| 〃 | | testShots row5 expectedMessageId1 | プロジェクト終了日はプロジェクト開始日より後の日付を入力して下さい。 | 同左 | OK |
| 〃 | | params row1 clientId | `${半角数字,9}` | `${半角数字,9}` | OK |

（※）Excel 経路では QuotationTrimmer が実行時に前後のダブルクォートを除去して `?` として扱う。YAML は `?` で、YAML 用 interpreters（`dateTimeInterpreter` / `compositeInterpreter`）にはクォート除去がないため、そのまま `?` として読まれる。両経路とも実行時の値は `?` で意味的に等価であり、差異ではない（全 Excel のうち前後クォート付きセルはこの2件のみ）。

## .xls/.xlsx 併存チェック（該当時のみ）
- 併存の有無: なし（`ProjectFormTest.xls` と `ProjectSaveActionRequestTest.xlsx` はベース名が異なる。拡張子を除いたパスに重複なし）
- 差分確認結果の保存先: 該当なし
- 未使用側（通常 `.xlsx`）の削除: 該当なし

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Meaningful tests/verification | | |
| Edge case coverage | | |

## Expert Reviews (code changes only)

N/A — no code changes in this task.

## Overall Verdict

- Self-check: OK
- QA:
- Language expert: N/A
- Software-engineering expert: N/A
- Ready for user review:
