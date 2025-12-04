#!/usr/bin/env bash
set -e

# 简单日志函数
log()  { printf '[INFO] %s\n' "$*"; }
err()  { printf '[ERR ] %s\n' "$*" >&2; }
die()  { err "$*"; exit 1; }

# 确保在 git 仓库根
if ! git rev-parse --show-toplevel >/dev/null 2>&1; then
  die "当前目录不是 git 仓库，请先 cd 到仓库根再执行。"
fi

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT" || die "无法切换到仓库根：$REPO_ROOT"

GITMODULES="$REPO_ROOT/.gitmodules"
MODULE_YAML="$REPO_ROOT/geek-modules.yml"

[ -f "$MODULE_YAML" ] || die "配置文件 geek-modules.yml 不存在，请先创建（YAML 结构 modules: - name/url/path）。"

# 从 geek-modules.yml 解析出 name -> path/url
# 预期 YAML 结构：
# modules:
#   - name: Foo
#     url: https://...
#     path: some/path
parse_modules() {
  awk '
    BEGIN {
      inmods=0; initem=0; name=""; url=""; path="";
    }
    /^[[:space:]]*#/ { next }
    /^[[:space:]]*$/ { next }

    /^[[:space:]]*modules:/ { inmods=1; next }

    inmods {
      # 开始一个新条目
      if ($0 ~ /^[[:space:]]*-[[:space:]]*name:[[:space:]]*/) {
        if (name != "" && url != "" && path != "") {
          printf "%s\t%s\t%s\n", name, path, url;
        }
        initem=1; name=""; url=""; path="";
        sub(/^[^:]*:[[:space:]]*/, "");
        name=$0;
        gsub(/^[[:space:]]+|[[:space:]]+$/, "", name);
        next;
      }

      if (initem && $0 ~ /^[[:space:]]*url:[[:space:]]*/) {
        s=$0; sub(/^[^:]*:[[:space:]]*/, "", s);
        url=s;
        gsub(/^[[:space:]]+|[[:space:]]+$/, "", url);
        next;
      }

      if (initem && $0 ~ /^[[:space:]]*path:[[:space:]]*/) {
        s=$0; sub(/^[^:]*:[[:space:]]*/, "", s);
        path=s;
        gsub(/^[[:space:]]+|[[:space:]]+$/, "", path);
        next;
      }
    }

    END {
      if (name != "" && url != "" && path != "") {
        printf "%s\t%s\t%s\n", name, path, url;
      }
    }
  ' "$MODULE_YAML"
}

# 列出 .gitmodules 中所有记录
cmd_list() {
  log "从 .gitmodules 解析的子模块列表："
  parse_modules | while IFS=$'\t' read -r name path url; do
    [ -n "$name" ] || continue
    printf ' - name: %-25s path: %-40s url: %s\n' "$name" "$path" "$url"
  done
}

# 查看当前索引中已载入的子模块（gitlink, mode 160000）
cmd_status() {
  log "当前索引中的子模块（gitlink，mode=160000）："
  git ls-files -s | awk '$1 == 160000 {print $4}' | while read -r path; do
    printf ' - %s\n' "$path"
  done
}

# 根据 name 查找 path + url
get_module_info() {
  local name="$1"
  parse_modules | awk -v target="$name" -F '\t' '$1 == target {print $2 "\t" $3}'
}

# 添加一个模块：按 name 从 .gitmodules 取 path/url，然后 git submodule add
cmd_add() {
  local name="$1"
  [ -n "$name" ] || die "用法：$0 add <module-name>（module-name 为 .gitmodules 中的名字）"

  local info
  info="$(get_module_info "$name")" || true
  [ -n "$info" ] || die "在 .gitmodules 中找不到名为 '$name' 的子模块条目。"

  local path url
  path="$(printf '%s\n' "$info" | cut -f1)"
  url="$(printf '%s\n' "$info" | cut -f2)"

  [ -n "$path" ] || die "子模块 '$name' 的 path 为空，请检查 .gitmodules。"
  [ -n "$url" ]  || die "子模块 '$name' 的 url 为空，请检查 .gitmodules。"

  # 检查是否已经是 gitlink
  if git ls-files -s -- "$path" 2>/dev/null | awk '$1 == 160000 {exit 0} END{exit 1}'; then
    log "路径 '$path' 已经是子模块（gitlink），跳过 add。"
    return 0
  fi

  # 如果目录已存在且不是子模块，提示用户
  if [ -d "$path" ] && [ ! -d "$path/.git" ]; then
    err "目录 '$path' 已存在且不是子模块目录，可能包含本地文件。"
    err "请先手动备份/删除该目录，然后再执行 add。"
    exit 1
  fi

  # 先用 git config --file .gitmodules 写入/覆盖配置，让 .gitmodules 与配置文件同步
  if [ -f "$GITMODULES" ]; then
    log "同步 .gitmodules：submodule.$name.path=$path, submodule.$name.url=$url"
    git config -f "$GITMODULES" "submodule.$name.path" "$path"
    git config -f "$GITMODULES" "submodule.$name.url" "$url"
  else
    log ".gitmodules 不存在，将在 git submodule add 时自动创建。"
  fi

  log "执行：git submodule add '$url' '$path'"
  git submodule add "$url" "$path"

  log "已添加子模块 '$name' -> path='$path'"
  log "可执行：git submodule update --init --recursive"
}

# 删除一个模块：按 name 从 .gitmodules 取 path，然后 git rm + 清理 .git/modules
cmd_remove() {
  local name="$1"
  [ -n "$name" ] || die "用法：$0 remove <module-name>"

  local info
  info="$(get_module_info "$name")" || true
  [ -n "$info" ] || die "在 .gitmodules 中找不到名为 '$name' 的子模块条目。"

  local path
  path="$(printf '%s\n' "$info" | cut -f1)"
  [ -n "$path" ] || die "子模块 '$name' 的 path 为空，请检查 .gitmodules。"

  log "将删除子模块 '$name'，路径 '$path'。"

  # 停用
  git submodule deinit -f -- "$path" 2>/dev/null || true

  # 从索引移除 gitlink
  if git ls-files -s -- "$path" 2>/dev/null | awk '$1 == 160000 {exit 0} END{exit 1}'; then
    log "执行：git rm -f '$path'"
    git rm -f "$path"
  else
    log "路径 '$path' 当前不是子模块 gitlink，仅尝试从索引移除（如果存在）。"
    git rm --cached -f "$path" 2>/dev/null || true
  fi

  # 删除 .git/modules 下的元数据
  local moddir=".git/modules/$path"
  if [ -d "$moddir" ]; then
    log "删除元数据目录：$moddir"
    rm -rf "$moddir"
  fi

  # 最后删除工作区中的实际目录壳，避免后续 add 时报 "already exists"
  if [ -d "$path" ]; then
    echo "[INFO] Removing working tree directory '$path'"
    rm -rf -- "$path"
  fi

  log "注意：本脚本不自动修改 .gitmodules 中的该条目，如需彻底删除请手动编辑 .gitmodules。"
}

usage() {
  cat <<EOF
用法：$0 <command> [args...]

command:
  list                 列出 .gitmodules 中的所有子模块 (name / path / url)
  status               查看当前索引中的子模块（gitlink，已载入模块）
  add <name>           根据 .gitmodules 中的 name 添加一个子模块（git submodule add）
  remove <name>        根据 .gitmodules 中的 name 删除一个子模块（git rm + 清理 .git/modules）

示例：
  $0 list
  $0 status
  $0 add Geek-Plugin-Ehcache
  $0 remove Geek-Plugin-Ehcache
EOF
}

case "$1" in
  list)    shift; cmd_list "$@";;
  status)  shift; cmd_status "$@";;
  add)     shift; cmd_add "$@";;
  remove)  shift; cmd_remove "$@";;
  ""|-h|--help) usage;;
  *) err "未知命令：$1"; usage; exit 1;;
esac
