"""Task #3 サンプリング比較用スクリプト。

ExcelDump.java の出力（"[シート] R<行> C<列> = <値>"）と、converter が生成した YAML
（シート名.yaml）を突き合わせ、LIST_MAP / SETUP_TABLE / EXPECTED_TABLE / MESSAGE の
各ブロックについて Excel のセル値と YAML の値が一致するかを検証する。
converter の変換ロジックには依存せず、Excel 側は POI ダンプ、YAML 側は PyYAML で独立に読む。

比較規則（NTF の Excel 解釈と YAML の対応）:
  - 空セル ↔ "" / テーブルの "null" ↔ YAML null（NullInterpreter 相当）
  - 前後ダブルクォート付きセル "x" ↔ x（QuotationTrimmer 相当）
  - "[...]" で始まるヘッダ列（マーカー列）と "//" で始まる列外コメントは比較対象外

使い方: python3 -I compare_excel_yaml.py <dump.txt> <yamlディレクトリ> [サンプル件数]
"""
import os
import re
import sys
from collections import defaultdict

import yaml

LINE = re.compile(r"^\[(?P<sheet>[^\]]+)\] R(?P<row>\d+) C(?P<col>\d+) = <(?P<val>.*)$")


class StrKeyLoader(yaml.SafeLoader):
    """YAML 1.1 の bool 解釈（no/yes/on/off 等）を無効化した SafeLoader。

    PyYAML は `no:` のようなキーを False と解釈するため、NTF の列名 `no` を文字列として読む。
    """


StrKeyLoader.yaml_implicit_resolvers = {
    k: [(tag, rx) for tag, rx in v if tag != "tag:yaml.org,2002:bool"]
    for k, v in yaml.SafeLoader.yaml_implicit_resolvers.items()
}
MARKER = re.compile(r"^(LIST_MAP|SETUP_TABLE|EXPECTED_TABLE|EXPECTED_COMPLETED|MESSAGE)(\[(\d+)\])?=(.+)$")


def load_dump(path):
    """ダンプを {sheet: {row: {col: value}}} に読み込む（複数行セルを連結する）。"""
    sheets = defaultdict(lambda: defaultdict(dict))
    cur = None
    with open(path, encoding="utf-8") as f:
        for raw in f:
            raw = raw.rstrip("\n")
            m = LINE.match(raw)
            if m:
                cur = (m["sheet"], int(m["row"]), int(m["col"]))
                sheets[cur[0]][cur[1]][cur[2]] = m["val"]
            elif cur:  # 改行を含むセルの継続行
                s, r, c = cur
                sheets[s][r][c] = sheets[s][r][c] + "\n" + raw
    # 各セル値の末尾にはダンプの閉じ ">" が1つ付くため除去する
    for s in sheets.values():
        for row in s.values():
            for c, v in row.items():
                row[c] = v[:-1] if v.endswith(">") else v
    return sheets


def excel_value(v, is_table):
    if v is None:
        return ""
    if len(v) >= 2 and v.startswith('"') and v.endswith('"'):
        return v[1:-1]
    if is_table and v == "null":
        return None
    return v


def blocks(sheet):
    """シートからブロック（kind, group, name, header, data_rows）を抽出する。"""
    rows = sorted(sheet)
    result = []
    i = 0
    while i < len(rows):
        r = rows[i]
        m = MARKER.match(sheet[r].get(1, ""))
        if not m:
            i += 1
            continue
        kind, group, name = m.group(1), m.group(3), m.group(4)
        # 後続の連続行をブロックとして取得
        body = []
        j = i + 1
        prev = r
        while j < len(rows) and rows[j] == prev + 1 and not MARKER.match(sheet[rows[j]].get(1, "")):
            body.append(sheet[rows[j]])
            prev = rows[j]
            j += 1
        result.append((kind, group, name, body))
        i = j
    return result


def table_like(body, is_table):
    header = body[0]
    cols = {c: h for c, h in header.items() if not h.startswith("[") and not h.startswith("//")}
    data = []
    for row in body[1:]:
        data.append({h: excel_value(row.get(c), is_table) for c, h in cols.items()})
    return data


def main():
    dump, ydir = sys.argv[1], sys.argv[2]
    sample = int(sys.argv[3]) if len(sys.argv) > 3 else 3
    sheets = load_dump(dump)
    total = mismatch = 0
    samples = []
    for sheet_name, sheet in sheets.items():
        ypath = os.path.join(ydir, sheet_name + ".yaml")
        if not os.path.exists(ypath):
            print(f"MISSING YAML: {ypath}")
            mismatch += 1
            continue
        with open(ypath, encoding="utf-8") as f:
            y = yaml.load(f, Loader=StrKeyLoader)
        for kind, group, name, body in blocks(sheet):
            if kind == "LIST_MAP":
                ex = table_like(body, False)
                yrows = next(b["rows"] for b in y["list_maps"] if b["id"] == name)
            elif kind in ("SETUP_TABLE", "EXPECTED_TABLE", "EXPECTED_COMPLETED"):
                key = "setup_tables" if kind == "SETUP_TABLE" else "expected_tables"
                ex = table_like(body, True)
                yb = next(b for b in y[key]
                          if b["table"] == name and (str(b.get("group_id")) == group if group else "group_id" not in b))
                yrows = yb["rows"]
            elif kind == "MESSAGE":
                yb = next(b for b in y["messages"] if b["id"] == name)
                directives = {}
                k = 0
                while k < len(body) and body[k].get(1) not in ("no",):
                    directives[body[k][1]] = body[k].get(2, "")
                    k += 1
                names, types, lengths = body[k], body[k + 1], body[k + 2]
                rec = yb["records"][0]
                for c in sorted(c for c in names if c > 1):
                    fld = next(fd for fd in rec["fields"] if fd["name"] == names[c])
                    for label, a, b in (("type", types.get(c, ""), fld.get("type")),
                                        ("length", lengths.get(c, ""), fld.get("length"))):
                        total += 1
                        if a != b:
                            mismatch += 1
                            print(f"NG {sheet_name}/{name} field {names[c]}.{label}: excel={a!r} yaml={b!r}")
                for dk, dv in directives.items():
                    total += 1
                    if yb["directives"].get(dk) != dv:
                        mismatch += 1
                        print(f"NG {sheet_name}/{name} directive {dk}: excel={dv!r} yaml={yb['directives'].get(dk)!r}")
                datarows = body[k + 3:]
                total += 1
                if len(datarows) != len(rec["rows"]):
                    mismatch += 1
                    print(f"NG {sheet_name}/{name} row count excel={len(datarows)} yaml={len(rec['rows'])}")
                cols = sorted(c for c in names if c > 1)
                for idx, (er, yr) in enumerate(zip(datarows, rec["rows"])):
                    for ci, c in enumerate(cols):
                        a, b = excel_value(er.get(c), False), yr[ci]
                        total += 1
                        ok = a == b
                        mismatch += 0 if ok else 1
                        if not ok:
                            print(f"NG {sheet_name}/{name} row{idx + 1} {names[c]}: excel={a!r} yaml={b!r}")
                        samples.append((sheet_name, f"MESSAGE={name} row{idx + 1} {names[c]}", a, b, ok))
                continue
            else:
                continue
            total += 1
            if len(ex) != len(yrows):
                mismatch += 1
                print(f"NG {sheet_name}/{kind}={name} row count excel={len(ex)} yaml={len(yrows)}")
            if not ex:
                samples.append((sheet_name, f"{kind}={name}", "(データ行なし)", "rows: []", len(yrows) == 0))
            for idx, (er, yr) in enumerate(zip(ex, yrows)):
                for h, a in er.items():
                    b = yr.get(h, "<<absent>>")
                    total += 1
                    ok = a == b
                    if not ok:
                        mismatch += 1
                        print(f"NG {sheet_name}/{kind}={name} row{idx + 1} {h}: excel={a!r} yaml={b!r}")
                    samples.append((sheet_name, f"{kind}={name} row{idx + 1} {h}", a, b, ok))
                extra = set(yr) - set(er)
                if extra:
                    mismatch += 1
                    print(f"NG {sheet_name}/{kind}={name} row{idx + 1} extra yaml keys {extra}")
    # シートごとに先頭から指定件数をサンプルとして出力
    shown = defaultdict(int)
    for s in samples:
        if shown[s[0]] < sample:
            shown[s[0]] += 1
            print("SAMPLE\t" + "\t".join(str(x) for x in s))
    print(f"compared={total} mismatch={mismatch}")
    sys.exit(1 if mismatch else 0)


if __name__ == "__main__":
    main()
