# Goal

NTF（Nablarch Testing Framework）のAI対応として、AIが読み書きできないバイナリ形式のExcelテストデータを `nablarch-example-http-messaging` でYAML形式に移行する。
`nablarch-testing-converter` を使ってExcelテストデータをYAMLに変換し、`nablarch-testing-yaml` を用いた設定変更のみで全テストがパスすることを確認する。
参照PR: https://github.com/Fintan-contents/nablarch-system-development-guide/pull/211

# Acceptance criteria

- `develop` ブランチから作成した `ntf-yaml-support` ブランチで全作業が行われている
- 作業前に既存の全テストがパスすることを確認済みである
- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が test スコープの依存として追加されている
- `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` がビルドプラグイン（plugin直下 `<configuration>` に変換パラメータを設定し、CLIで `convert` ゴールを実行）として追加されている（test依存ではない）
- `src/test/resources/unit-test.xml` に `YamlTestDataParser` コンポーネントと `yamlInterpreters` リストが追加されている
- 変換対象の全てのテスト用ExcelファイルがそれぞれYAMLファイルに変換されている（サンプリングでExcelとYAMLの内容が一致することを確認済み）
- 変換後、変換対象だったExcelファイルが全て削除されている
- YAMLテストデータを使った状態で全テストがパスしている
- テストクラス本体（`.java`）の変更はなく、設定ファイルと依存関係・テストデータの変更のみで移行が完結している

# Assumptions

- `nablarch-testing-yaml:1.0.0-SNAPSHOT` と `nablarch-testing-converter:1.0.0-SNAPSHOT` はローカルMavenリポジトリ（`~/.m2`）に存在する
- テスト実行にはH2インメモリDBを使用し、`generate-sources` フェーズでDBセットアップとエンティティ生成が行われる
- YAMLファイルの出力先は `src/test/java` 配下（Excelと同じ場所）とし、`nablarch.test.resource-root` の設定変更は不要
- 変換ツール（`nablarch-testing-converter`）の実行手順は `/home/k10949/workspace/nablarch-testing-converter/README.md` に従う

# Environment / Build & Test

- JDK 17、Maven 3.9.9以降、H2 組み込み（別途DBサーバ不要）
- 本プロジェクトには `gsp` という専用プロファイルは存在しない。`gsp-dba-maven-plugin` の実行（generate-ddl / execute-ddl / generate-entity / load-data）は `generate-sources` フェーズに無条件バインドされているため、`mvn test`（`generate-sources` を内包）だけでDBセットアップ〜テストまで一括実行できる。
- **ビルド・テストコマンド**: `mvn clean test`
  - エンティティ生成のみ個別実行する場合: `mvn generate-resources`
  - 依存解決確認: `mvn dependency:resolve`
- **ロケール**: NTFのメッセージ解決のため `LANG=ja_JP.UTF-8`（および `LC_ALL=ja_JP.UTF-8`）必須。`C.UTF-8` だとメッセージ解決に失敗しテストが落ちる。
- README（`/home/k10949/workspace/nablarch-example-http-messaging/README.md`）では `mvn jetty:run`（起動）/ `mvn generate-resources`（DB・エンティティ生成）/ `mvn compile`（ビルド）を案内。テストは上記 `mvn clean test` を使用する。

# Rules

- commit every change; one completion marker per task
- push は行わない（コミットのみ。リモートへの push は最後にユーザーが行う）
- テストクラス（`.java`）は変更しない
- `develop` から新規作成した `ntf-yaml-support` ブランチで作業する
- steering.md は各タスクで更新し、当該タスクのコミットに含める
- テストはExcelファイルが存在しない状態で最終パスを確認する
- YAML変換後はサンプリングでExcelとYAMLの内容を比較してから削除する
- サンプリング比較で差異が見つかった場合は修正せずユーザーに報告して停止する
- QA / language / software-engineering の各レビューは Agent（`subagent_type: general-purpose` / `model: Opus 4.8`）でエキスパートペルソナを付与して実施する。software-engineering レビューの重複指摘はMD記載不要。

# 対象Excelテストデータ

本プロジェクトのテストクラスは3件、うちExcelテストデータを持つのは2件：
- `src/test/java/com/nablarch/example/form/ProjectFormTest.java`（`@EntityTest`） → `ProjectFormTest.xls`
- `src/test/java/com/nablarch/example/action/ProjectSaveActionRequestTest.java`（`@MessagingRequestTest`、テスト3件） → `ProjectSaveActionRequestTest.xlsx`

対象Excelファイル（2件、ベース名が異なるため同名 `.xls`/`.xlsx` 併存の問題はない）：
- `src/test/java/com/nablarch/example/form/ProjectFormTest.xls`
- `src/test/java/com/nablarch/example/action/ProjectSaveActionRequestTest.xlsx`

**リスク（Task #5 で検証）**: `ProjectSaveActionRequestTest` はHTTPメッセージング受信側のリクエスト単体テストで、構造化電文（`StructuredFwHeaderDefinition`、JSONフォーマット電文の可能性）を扱う。姉妹プロジェクト `nablarch-example-http-messaging-send`（送信側）は JSONフォーマット電文が nablarch-testing-yaml 未サポートのため Task #5 で BUILD FAILURE となった（詳細は当該プロジェクトの `reports/task-5_json-yaml-incompatibility.md`）。本プロジェクトでも同種のブロッカーが発生する可能性があり、Task #5 で検証・判断する。

# Scripts

（必要になった場合に `.rn/ntf-yaml-support/scripts/` 配下へ格納し、用途をここと該当 task-N.md に記載する。）

# Tasks

### #1: 現状のビルド・テストで全PASSを確認する

**Purpose**: `develop` から `ntf-yaml-support` ブランチを作成し、変更前の状態で全テストがパスすることを確認する。

**Prerequisites**: none

**Steps**:

- [x] `develop` ブランチの存在を確認し、そこから `ntf-yaml-support` ブランチを新規作成する
- [x] 対象プロジェクトの README を確認し、ビルド・テストの実行手順を把握する（本プロジェクトは `gsp` プロファイル無し、`generate-sources` に gsp 実行がバインド。`mvn clean test` で一括実行可）
- [x] 隣接プロジェクトの `.rn/` を参照し steering.md と scripts の初版を作成する（ビルド・テスト手順を明記）
- [x] README で確認した手順でエンティティ生成〜テストを実行し、全テストがパスすることを確認する（`mvn clean test` → Tests run: 19 / BUILD SUCCESS）
- [x] 結果を `checks/task-1.md` に記録する（self-check列のみ）
- [x] 変更ファイル（check ファイルおよび更新した steering.md）をコミットする
- [x] self-check (OK/NG per completion criterion, record in checks/task-1.md)
- [x] QA expert review (Agent: general-purpose / Opus 4.8) — OK
- [ ] user review

**Completion criteria**:

- `mvn clean test` が全テストパスで終了している（BUILD SUCCESS）

### #2: 設定変更 — pom.xml と unit-test.xml にYAML対応を追加

**Purpose**: `pom.xml` に `nablarch-testing-yaml`（test 依存）を追加し、`nablarch-testing-converter` を `<build><plugins>` のビルドプラグイン（plugin直下 `<configuration>` に変換パラメータを設定し、CLIで `convert` ゴールを実行）として追加し、`unit-test.xml` に `yamlInterpreters` リストと `YamlTestDataParser` コンポーネントの設定を追加する。設定内容は隣接プロジェクト（nablarch-example-web / nablarch-example-batch / nablarch-example-http-messaging-send）を参考にする。

**Prerequisites**: #1

**Steps**:

- [x] `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test scope）を追加する
- [x] `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` をビルドプラグインとして追加する（`<configuration>` に from=xls / to=yaml / input / output / overwrite を設定。web/batch/send 準拠）
- [x] `unit-test.xml` に `yamlInterpreters` リストと `YamlTestDataParser` コンポーネントを追加する（隣接プロジェクトを参考）
- [x] `mvn validate` / `mvn dependency:resolve` / `mvn help:describe`（converter）で解決確認（いずれも BUILD SUCCESS）
- [x] 変更したファイルをコミットする
- [x] self-check (OK/NG per completion criterion, record in checks/task-2.md)
- [x] QA expert review (Agent: general-purpose / Opus 4.8) — OK
- [x] language expert review (Agent: general-purpose / Opus 4.8) — OK
- [x] software-engineering expert review (Agent: general-purpose / Opus 4.8) — OK
- [ ] user review

**Completion criteria**:

- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が test スコープで追加されている
- `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` がビルドプラグイン（plugin直下 `<configuration>` に変換パラメータを設定し、CLIで `convert` ゴールを実行）として追加されている
- `unit-test.xml` に `yamlInterpreters` リストと `testDataParser` コンポーネント（`YamlTestDataParser`）が追加されている
- `mvn dependency:resolve` が成功し、converter プラグインの `convert` ゴールが `mvn help:describe` で解決できる

### #3: ExcelテストデータをYAMLに変換

**Purpose**: `nablarch-testing-converter` を使って Excel のテストデータをYAML形式に変換し、テストクラスと同じディレクトリに配置して、変換内容をサンプリングで確認する。

**Prerequisites**: #2

**Steps**:

- [x] 対象 Excel ファイルの所在を確認する（ProjectFormTest.xls / ProjectSaveActionRequestTest.xlsx）
- [x] 同名の `.xls` と `.xlsx` が併存していないか確認する（ベース名が異なるため併存なし）
- [x] `/home/k10949/workspace/nablarch-testing-converter/README.md` の手順に従い、全ての対象ExcelファイルをYAML変換する（`Converted 2 file(s)` / BUILD SUCCESS、WARN なし、計7 YAML）
- [x] 変換後、各YAMLファイルについてExcelの内容とサンプリングで比較し、一致を確認する（POI独立ダンプ `scripts/ExcelDump.java` と突合、各ファイル3件以上、差異なし）
- [x] サンプリング比較結果を `checks/task-3.md` に記録する（差異なし。`"?"`→`?` は QuotationTrimmer 仕様で欠陥なし）
- [x] 変換したYAMLファイルをコミットする（Excelはまだ削除しない）
- [x] self-check (OK/NG per completion criterion, record in checks/task-3.md)
- [x] QA expert review (Agent: general-purpose / Opus 4.8) — OK
- [ ] user review

**Completion criteria**:

- 変換対象の全てのExcelファイルに対応するYAMLファイルが生成されている
- サンプリング比較（各ファイル最低3件、元データが3件未満の場合は元データ件数分）でExcelとYAMLの内容が一致している（差異があれば原因を調査しユーザーに報告し、修正せずに止める）
- 変換後のYAMLファイルがコミットされている

### #4: Excelを削除する

**Purpose**: 変換対象だった Excel を削除してコミットする。

**Prerequisites**: #3

**Steps**:

- [x] 変換対象となった Excel ファイルを削除する（ProjectFormTest.xls / ProjectSaveActionRequestTest.xlsx の2件）
- [x] 削除結果（残存Excelが0件であること）を `checks/task-4.md` に記録する（Excelなしでの `mvn clean test` 実行とその結果の確認は Task #5 で行う）
- [x] Excelファイル削除をコミットする
- [x] self-check (OK/NG per completion criterion, record in checks/task-4.md)
- [x] QA expert review (Agent: general-purpose / Opus 4.8) — OK
- [ ] user review

**Completion criteria**:

- 変換対象だった全 Excel ファイルが削除されている
- 削除がコミットされている

### #5: YAMLテストデータで全テストをパスさせる

**Purpose**: Excelなしで全テストがパスすることを確認する。

**Prerequisites**: #4

**Steps**:

- [ ] `mvn clean test` が BUILD SUCCESS になることを確認する
- [ ] テストログで YamlTestDataParser が実際に使われていることを確認する
- [ ] self-check (OK/NG per completion criterion, record in checks/task-5.md)
- [ ] QA expert review (Agent: general-purpose / Opus 4.8)
- [ ] language expert review (Agent: general-purpose / Opus 4.8)
- [ ] software-engineering expert review (Agent: general-purpose / Opus 4.8)
- [ ] user review

**Completion criteria**:

- `mvn clean test` が BUILD SUCCESS で終了する（全テストパス、Excel ファイルなし）
- テストログで YamlTestDataParser が実際に使われていることが確認できる

# Decisions

- **[確定] nablarch-testing-converter の宣言方式**: ビルドプラグイン（`<build><plugins>`、`convert` ゴール設定付き）とする。test依存にはしない。理由: converter は Maven プラグインでテスト実行時には不要、XLS→YAML 変換時のみ使用。test依存だと POI/snakeyaml 等の推移依存がテストクラスパスに混入するリスクがある。web/batch/send テンプレートに準拠。

# State

- **Status**: in progress
- **Date**: 2026-10-06
- **Last completed**: Task #4（Excel 2件を削除、残存0件。self-check OK）。QA review・user review 待ち。
- **Next**: Task #5（Excelなしで `mvn clean test` 全パス確認）
- **重要**: 本PJのメッセージング電文は `file-type: Fixed` + 数値 length（403/20）でモデル化されており、姉妹PJ send の `length: "JSON"` ブロッカーとは異なる。Task #5 でスキーマ適合・全テストパスの見込み（実機確認は Task #5）。
- **Notes**: ブランチ `ntf-yaml-support`（develop 起点）。JSONフォーマット電文のYAMLスキーマ非互換が Task #5 で顕在化する可能性あり（姉妹PJ send で既知）。
