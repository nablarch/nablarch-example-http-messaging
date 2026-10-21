# nablarch-example-http-messaging NTF YAML 対応 steering

## Overview
- 目的・背景: NTF（Nablarch Testing Framework）のAI対応として、AIが読み書きできないバイナリ形式のExcelテストデータをYAML形式に移行する（背景資料: https://github.com/Fintan-contents/nablarch-system-development-guide/pull/211）。
- Acceptance criteria の要約:
  - 既存 `ntf-yaml-support` ブランチ上で、過去の移行コミット履歴を維持したまま（rebase / amend / reset --hard 禁止）、develop 一致ツリーのベースラインコミット → task-1〜5 を新規コミットとして積み上げて**再実施**する（完了済みでもスキップしない）
  - ベースライン（develop 同一）で既存の全テストがパス
  - `pom.xml`: `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test scope）、`nablarch-testing-converter:1.0.0-SNAPSHOT`（`<build><plugins>` のビルドプラグイン）
  - `YamlTestDataParser`（`testDataParser`）を `testDataParser` が参照される全フェーズで有効になる設定ファイルに追加（`interpreters` は testdata.html の設定例どおり `dateTimeInterpreter` → `compositeInterpreter` の2つのみ）
  - 全対象 Excel を YAML 変換（サンプリング一致確認）→ Excel 削除 → YAML で全テストパス
- 対象プロジェクト名: nablarch-example-http-messaging
- 指示書: `.rn/ntf-yaml-support/ntf-yaml-support-instructions_http-messaging_V1.6.md`（原本: `/home/k10949/instructions/ntf-yaml-support-instructions_http-messaging_V1.6.md`）

## Build / Test
- 環境: JDK 17（README 前提。Maven 実行 JDK は Temurin 17.0.20。※PATH 上の `java` は 21.0.2 だが Maven は 17 で動作）、Maven 3.9.16、H2 組み込み（DBサーバ不要）
- README（`mvn jetty:run` / `mvn generate-resources` / `mvn compile` を案内）を確認。`gsp` 専用プロファイルは無く、`gsp-dba-maven-plugin`（DDL生成/実行・エンティティ生成・データロード）は `generate-sources` フェーズに無条件バインドされている。そのため `generate-resources` を個別に実行する必要はなく、`mvn clean test` だけで DB セットアップ・エンティティ生成からテストまで一括実行できる。
- **テストコマンド**: `LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8 mvn clean test`
  - ロケール指定は必須（`C.UTF-8` 等だと NTF のメッセージ解決に失敗する）
  - エンティティ生成のみ: `mvn generate-resources`
  - 依存解決確認: `mvn dependency:resolve`、converter 解決確認: `mvn help:describe -Dplugin=com.nablarch.framework:nablarch-testing-converter:1.0.0-SNAPSHOT`
  - XLS→YAML 変換: `mvn com.nablarch.framework:nablarch-testing-converter:1.0.0-SNAPSHOT:convert`（パラメータは pom の plugin `<configuration>`）

## 対象・構成メモ
- テストクラス（Excel を持つもの）:
  - `src/test/java/com/nablarch/example/form/ProjectFormTest.java`（`@EntityTest`）→ `ProjectFormTest.xls`
  - `src/test/java/com/nablarch/example/action/ProjectSaveActionRequestTest.java`（`@MessagingRequestTest`、HTTPメッセージング受信側のリクエスト単体テスト）→ `ProjectSaveActionRequestTest.xlsx`
- テスト設定: `src/test/resources/unit-test.xml` が `http-messaging-test-component-configuration.xml`（アクション起動 diConfig、testShots の `diConfig` で指定）と `nablarch/test/test-data.xml` を import している。
- **testDataParser 配置判定（Task #2）**: 受信側テストで、アクション `ProjectSaveAction` は `MessageSender` / `sendSync` を使用しない（`src/main/java` に該当なし）。diConfig 内の `MockMessagingClient` は呼ばれず、Excel にも `response_*`（同期送信の応答電文）は無い。さらにフレームワーク側でも、`MessagingRequestTestSupport` は setUp/expected メッセージを Main 起動前（unit-test.xml 有効時）に読み、diConfig による再初期化（`recreateRepository`）の後は finally の `revertDefaultRepository` で戻す。`MockMessagingClient` がテストデータを読むのは sendSync 時だけ（Task #2 QA/SE で確認）。よってアクション実行中に `testDataParser` 経由でテストデータを読む処理は無く、Rules の判定では「非該当」。配置先は `unit-test.xml` のみとする（本番用 `src/main/resources` には入れない）。

## Scripts
- `scripts/ExcelDump.java`（Task #3）: converter の変換ロジックに依存せず、Apache POI 3.8 で Excel 全セルを「シート/行/列/値」でダンプする独立ツール。変換後 YAML と原本 Excel のサンプリング比較に使用する（過去の移行作業 `f46e98e` から復元）。
- `scripts/compare_excel_yaml.py`（Task #3）: ExcelDump の出力と YAML（PyYAML）をブロック単位で突き合わせ、全セルの一致判定とシートごとのサンプル出力を行う比較スクリプト。実行: `python3 -I compare_excel_yaml.py <dump.txt> <YAMLディレクトリ> [サンプル件数]`

## Tasks

### #1: ベースライン化（developと同一状態）し、全PASSを確認する
**Purpose**: 既存 `ntf-yaml-support` ブランチ（過去の移行コミット `8f14d70`〜`f46e98e` を含む）の先端の上に、ワーキングツリーを `develop` と同一内容へ揃える新規コミットを積み、その状態で全テストがパスすることを確認する（完了済みでも再実施）。
**Prerequisites**: none
**Steps**:
- [x] 既存の `ntf-yaml-support` ブランチ上で作業（新規作成なし、先端 `f46e98e`）
- [x] 巻き戻さず現在の先端の上に処理を積む（rebase / reset --hard / amend 不使用）
- [x] `git read-tree -u --reset develop` → `git commit` でベースラインコミット `3b4a150` を作成（`git diff develop HEAD` が空、`f46e98e` は祖先として残存）
- [x] README を確認しビルド・テスト手順を把握（上記 Build / Test）
- [x] steering.md 初版を作成（scripts は Task #3 で作成予定）
- [x] 本指示書を `.rn/ntf-yaml-support/` へ同名コピーし `cmp` で同一を確認
- [x] エンティティ生成（`mvn clean test` の `generate-sources` フェーズで実行）
- [x] `mvn clean test` → Tests run: 19（ProjectSaveActionRequestTest 3 / ProjectFormTest 16）, Failures 0, Errors 0 / BUILD SUCCESS
- [x] 結果を `checks/task-1.md` に記録
- [x] 変更ファイルをコミット
- [x] self-check
- [x] QA expert review (Agent: general-purpose / Opus) — OK
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- 既存の `ntf-yaml-support` ブランチ上で作業しており、コミット履歴が維持されている
- ブランチの既存先端の上に、ワーキングツリー全体が `develop` と同一内容になる新規コミットが1つ積まれている
- `mvn clean test` が全テストパスで終了している（BUILD SUCCESS）
- 本指示書のコピーが `.rn/ntf-yaml-support/` 配下に原本と同一内容で配置され、コミットされている

### #2: 設定変更 — pom.xml と unit-test.xml にYAML対応を追加
**Purpose**: `pom.xml` に `nablarch-testing-yaml`（test 依存）と `nablarch-testing-converter`（ビルドプラグイン）を追加し、`unit-test.xml` に `YamlTestDataParser`（testdata.html の設定例どおり）を追加する。
**Prerequisites**: #1
**Steps**:
- [x] `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test scope）を追加
- [x] `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` を追加（`<configuration>` に from/to/input/output/overwrite）
- [x] testDataParser 配置判定（上記「対象・構成メモ」：非該当 → `unit-test.xml`）
- [x] `unit-test.xml` に `YamlTestDataParser` を追加（`interpreters` 内 `<list>` に `dateTimeInterpreter` → `compositeInterpreter` の2つのみ、`dbInfo` 定義より後）
- [x] `mvn validate` / `mvn dependency:resolve` / `mvn help:describe`（converter）で解決確認（いずれも BUILD SUCCESS、`convert` ゴール解決）
- [x] 変更したファイルをコミット
- [x] self-check — OK
- [x] QA / language / software-engineering expert review — いずれも OK
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が test スコープで追加されている
- `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` がビルドプラグインとして追加されている
- `YamlTestDataParser` が全フェーズで有効になる設定ファイルに追加され、`interpreters` は `dateTimeInterpreter` → `compositeInterpreter` の2つのみ
- `mvn dependency:resolve` が成功し、converter の `convert` ゴールが `mvn help:describe` で解決できる

### #3: ExcelテストデータをYAMLに変換
**Purpose**: converter で Excel を YAML に変換し、テストクラスと同じディレクトリ配下に配置して、サンプリングで内容一致を確認する。
**Prerequisites**: #2
**Steps**:
- [x] 対象 Excel の所在確認（`form/ProjectFormTest.xls`、`action/ProjectSaveActionRequestTest.xlsx` の2件）
- [x] 同名 `.xls` / `.xlsx` 併存確認（併存なし）
- [x] converter README の手順で全対象 Excel を YAML 変換（`Converted 2 file(s).`、WARN なし、7 YAML）
- [x] 各 YAML を Excel とサンプリング比較（`scripts/ExcelDump.java` + `scripts/compare_excel_yaml.py` で全ブロック全件照合: 120 + 1168項目 mismatch 0。各ファイル3件以上をサンプル記録）
- [x] 結果を `checks/task-3.md` の Sampling Comparison に記録（`"?"`→`?` の2件は QuotationTrimmer 相当で意味的に等価）
- [x] YAML をコミット（Excel は削除しない）
- [x] self-check — OK
- [x] QA expert review — OK（別手段で全1537項目照合、差異0）
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- 変換対象の全 Excel に対応する YAML が生成されている
- サンプリング比較で Excel と YAML の内容が一致している
- YAML がコミットされている

### #4: Excelを削除する
**Purpose**: 変換対象だった Excel を削除してコミットする。
**Prerequisites**: #3
**Steps**:
- [x] 変換対象 Excel を削除（`ProjectFormTest.xls`・`ProjectSaveActionRequestTest.xlsx` の2件、`git rm`）
- [x] 残存 Excel 0件を `checks/task-4.md` に記録（作業ツリー・`git ls-files` とも0件）
- [x] 削除をコミット
- [x] self-check — OK
- [x] QA expert review — OK
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- 変換対象だった全 Excel が削除されている
- 削除がコミットされている

### #5: YAMLテストデータで全テストをパスさせる
**Purpose**: Excel なしで全テストがパスすることを確認する。
**Prerequisites**: #4
**Steps**:
- [ ] `mvn clean test` が BUILD SUCCESS
- [ ] テストログで YamlTestDataParser の使用を確認
- [ ] self-check / QA / language / software-engineering expert review
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- `mvn clean test` が BUILD SUCCESS（全テストパス、Excel なし）
- テストログで YamlTestDataParser が実際に使われていることが確認できる

## State
- Status: Task #1 完了（self-check / QA OK）、Task #2 完了（self-check / QA / Language / SE すべて OK）、Task #3 完了（self-check / QA OK）、Task #4 完了（self-check / QA OK）、Task #5 実施中
- 注意: 未追跡 `.idea/` はコミットしない
- 過去の移行コミット（`8f14d70`〜`f46e98e`）は履歴に維持。再実施はベースライン `3b4a150` の上に積む。
