#!/usr/bin/env python3
"""E2E 日志断言：检查无头客户端运行产物日志中的 EnchantPeak 标记行。

用法：
  python3 scripts/assert_e2e_log.py --require 'marker regex' ... --forbid 'bad regex' ...
  （--require 至少匹配一次即通过；--forbid 出现即失败）

日志定位：依次尝试 $E2E_LOG、常见 latest.log 路径与工作区搜索。
"""
import argparse
import re
import sys
from pathlib import Path


def find_log():
    candidates = []
    explicit = Path(__import__("os").environ.get("E2E_LOG", "") or "/nonexistent")
    candidates.append(explicit)
    candidates.append(Path.home() / ".minecraft" / "logs" / "latest.log")
    candidates.append(Path(".minecraft/logs/latest.log"))
    candidates.append(Path("run/logs/latest.log"))
    for c in candidates:
        if c.is_file():
            return c
    # 兜底：工作区全量搜索（排除 build 产物目录）
    hits = sorted(
        (p for p in Path(".").rglob("latest.log")
         if "build" not in p.parts and p.is_file()),
        key=lambda p: p.stat().st_mtime, reverse=True)
    if hits:
        return hits[0]
    return None


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--require", nargs="*", default=[],
                        help="必须出现的正则（全部命中才通过）")
    parser.add_argument("--forbid", nargs="*", default=[],
                        help="禁止出现的正则（任一命中即失败）")
    parser.add_argument("--require-file", type=Path, default=None,
                        help="每行一个必须出现的正则（跳过空行与 # 注释）；"
                             "推荐在 CI 中用文件传参，避免 shell 转义破坏反斜杠")
    parser.add_argument("--forbid-file", type=Path, default=None,
                        help="每行一个禁止出现的正则（同上）")
    args = parser.parse_args()

    def read_patterns(path):
        out = []
        if path is not None and path.is_file():
            for line in path.read_text(encoding="utf-8").splitlines():
                line = line.strip()
                if line and not line.startswith("#"):
                    out.append(line)
        return out

    require = list(args.require) + read_patterns(args.require_file)
    forbid = list(args.forbid) + read_patterns(args.forbid_file)

    log = find_log()
    if log is None:
        print("✗ 未找到 latest.log", file=sys.stderr)
        return 2
    text = log.read_text(encoding="utf-8", errors="replace")
    print(f"检查日志: {log} ({len(text)} 字节)")

    failures = []
    for pattern in require:
        if not re.search(pattern, text):
            failures.append(f"缺少必需标记: {pattern}")
        else:
            print(f"  ✓ 必需标记命中: {pattern}")
    for pattern in forbid:
        m = re.search(pattern, text)
        if m:
            failures.append(f"出现禁止标记: {pattern}（上下文: …{text[max(0, m.start()-80):m.end()+80]}…）")
        else:
            print(f"  ✓ 禁止标记未出现: {pattern}")

    if failures:
        print("✗ E2E 断言失败：")
        for f in failures:
            print(f"  - {f}")
        return 1
    print("✅ E2E 日志断言全部通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())
