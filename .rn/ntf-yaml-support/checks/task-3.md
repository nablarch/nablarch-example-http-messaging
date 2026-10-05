# task-3 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 変換対象の全ExcelファイルにYAMLが生成 | OK | `ProjectFormTest.xls` → 3件（testBeanValidation / testCharsetAndLength / testSingleValidation）、`ProjectSaveActionRequestTest.xlsx` → 4件（setUpDb / testAbNormalEnd / testNormalEndExistAllFields / testNormalEndOnlyRequireFields）。計7ファイル。converter: `Converted 2 file(s)` / BUILD SUCCESS、WARN（重複カラム）なし | OK | QAが7YAMLをPyYAMLでパース、シート名=YAMLファイル名の全7対応を独立確認 |
| サンプリング比較でExcelとYAMLの内容が一致 | OK | Apache POI(3.8) による独立ダンプ（`scripts/ExcelDump.java`）と全7YAMLを突合。各ファイル3件以上サンプリング、差異なし（詳細は下記） | OK | QAがExcel生値を repr/バイト長付きで再ダンプ＋YAML実デコードで電文JSONを全文バイト照合（366/334/344字 完全一致）。差異は `"?"`→`?` の1件のみで QuotationTrimmer 仕様通り（下記 QuotationTrimmer 注記参照、欠陥なし） |
| 変換後YAMLがコミットされている | OK | 7 YAMLファイル・steering.md・task-3.md・scripts/ExcelDump.java をコミット（Excel/xlsx は未削除） | OK | commit `74cdf49` に7YAMLがコミット済み、Excel 2件は git 追跡下で未削除（Task #4 で削除）を確認 |

### 変換ツール実行

- コマンド: `mvn com.nablarch.framework:nablarch-testing-converter:1.0.0-SNAPSHOT:convert`（pom の plugin configuration: from=xls / to=yaml / input=output=`src/test/java` / overwrite=true）
- 結果: `[INFO] Converted 2 file(s). / BUILD SUCCESS`。重複カラム WARN なし。
- 同名 `.xls`/`.xlsx` 併存チェック: ベース名が異なる（ProjectFormTest.xls / ProjectSaveActionRequestTest.xlsx）ため併存なし。converter の "same-name Excel books coexist" エラーは発生せず。

### サンプリング比較用スクリプト

- `scripts/ExcelDump.java`: converter の変換ロジックに依存せず Apache POI で Excel 全セルを「シート/行/列/生値」でダンプする独立ツール。POI 3.8（converter と同一）+ 推移依存のクラスパスで実行。原本 Excel と変換後 YAML のサンプリング一致確認に使用。

### サンプリング比較詳細（各ファイル最低3件、差異なし）

**ProjectSaveActionRequestTest.xlsx（4シート）**

- `setUpDb` → `setUpDb.yaml`: SETUP_TABLE PROJECT / INDUSTRY / CLIENT の3テーブルがいずれもデータ行なし → YAML `rows: []` ×3 一致 ✓
- `testNormalEndExistAllFields` → 同名YAML:
  - testShots: no=1, description=`プロジェクトが正しくデータベースに登録されること。`, expectedStatusCode=空, setUpTable=1, expectedTable=1, diConfig=`classpath:http-messaging-test-component-configuration.xml`, requestPath=`/ProjectSaveAction`, userId=100 → 一致 ✓
  - setUpMessages: field `{name:"JSON", type:"全半角", length:"403"}`、本文JSON（projectName=プロジェクト９９９ 他、`\n`・インデント・多バイト含め完全一致）→ 一致 ✓
  - expectedMessages: field length=20、本文=`{"statusCode":"201"}` → 一致 ✓
  - setup_tables INDUSTRY(01/農業) / CLIENT(1/テスト顧客名/01)、expected_tables PROJECT 全15列（USER_ID=null 含む）→ 一致 ✓
- `testNormalEndOnlyRequireFields` → 同名YAML: 任意項目が null の必須のみ本文（projectManager:null 他）、expected PROJECT の任意列 null → 一致 ✓
- `testAbNormalEnd` → 同名YAML: description=`バリデーションエラーが発生し、DBにプロジェクトが登録されないこと。`, 本文JSON（projectName/projectType/projectClass が空）, expectedMessages=`{"statusCode":"400"}`, expected_tables PROJECT=`rows: []` → 一致 ✓

**ProjectFormTest.xls（3シート）**

- `testCharsetAndLength` → `charsetAndLength`: row1(projectName) allowEmpty=x, max=64, messageIdWhenInvalidLength=`{...Length.max.message}`, interpolateKey_1=max/Value_1=64, messageIdWhenNotApplicable=`{...SystemChar.message}`, interpolateKey_2=charsetDef/Value_2=全角文字, 文字種列（半角英字=x … 全角英字=o …）全20列 → 一致 ✓。row2(projectManager) allowEmpty=o も一致 ✓
- `testSingleValidation` → `singleValidation`: row1(projectType, 空, Required.message), row2(development, messageId空), row3(maintenance), row4(hoge, EnumElement.message) → 一致 ✓。コメントセル `// emptyNG` はデータ列ではないため正しく除外 ✓
- `testBeanValidation` → `testShots`+`params` の2 list_map: testShots 5行（正常系-1〜4 は expectedMessageId1 空、異常系-1 は `プロジェクト終了日はプロジェクト開始日より後の日付を入力して下さい。`、propertyName1=validProjectPeriod）、`[No]` マーカー列は正しく除外。params の各行（projectClass=ss/s/a …、clientId=`${半角数字,9}` 他）→ 一致 ✓

### QuotationTrimmer に関する注記（QA検出、欠陥なし）

- `testSingleValidation` の projectStartDate/projectEndDate の `?` ケース（Excel R52C2/R70C2）で、Excel 生値は3文字 `"?"`（0x22 0x3F 0x22）だが YAML は `input1: "?"`（デコード値1文字 `?`）。
- これは Nablarch `QuotationTrimmer`（実行時に前後ダブルクォートを除去する仕様）に基づく。Excel 経路では実行時に `"?"` → `?` とトリムされるため、converter が除去後の `?` を YAML 化したのは意味的に等価で正しい。
- 本PJの `unit-test.xml` の `yamlInterpreters` は `quotationTrimmer` を**除外**しているため、YAML 値 `?` はそのまま `?` として読まれ、Excel 実行時値と一致する。両経路のテスト値は `?` で同一。前後クォート付きセルは全Excel走査でこの2件のみ。

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Meaningful tests/verification | OK | self-check の POI依存ダンプに加え、QAが独立に Excel 生値を repr/バイト長付きで再ダンプ＋全7YAMLを PyYAML 実デコードし、電文JSONを全文バイト照合（366/334/344字 完全一致）。self-check 未言及の `"?"`（QuotationTrimmer）差異まで追跡し仕様確認済み |
| Edge case coverage | OK | JSON電文の改行/多バイト/`null`/空文字、`rows: []`、コメント行・`[No]`マーカー・列外コメント（`// emptyNG`）の除外、group_id、USER_ID=null、ダブルクォート規約を網羅確認。停止すべき差異なし |

## Expert Reviews (code changes only)

N/A — no code changes in this task（テストデータYAMLの生成のみ、プロダクション/テストコードの変更なし）。

## Overall Verdict

- Self-check: OK
- QA: OK
- Language expert: N/A
- Software-engineering expert: N/A
- Ready for user review: Yes

## 備考（Task #5 への申し送り）

本プロジェクトのメッセージング電文は `file-type: "Fixed"` + 数値 length（403 / 20）でモデル化されており、1フィールド `JSON`（type=全半角）に JSON 本文全体を格納する形式。姉妹PJ `nablarch-example-http-messaging-send` を Task #5 でブロックした `length: "JSON"`（非数値）とは異なり、YAMLスキーマ（`length` は `^([0-9]+|-)$`）に適合する見込み。Task #5 で `mvn clean test` の実機確認を行う。
