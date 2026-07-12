#!/usr/bin/env python3
import re
import os
from pathlib import Path

WORKTREE = Path("/home/sofn/code/sofn/ArchForge/.worktrees/admin-user-api-split")


def extract_methods(content: str) -> list[str]:
    """Extract one-line public method signatures from a Java class."""
    methods = []
    for line in content.splitlines():
        m = re.match(r"^(\s*)public\s+([A-Za-z0-9_<>,?\s]+)\s+([a-zA-Z_]\w*)\s*\(([^)]*)\)\s*\{", line)
        if m:
            indent, return_type, name, params = m.groups()
            name = name.strip()
            return_type = " ".join(return_type.split())
            if name == return_type.split()[-1].replace("<", "").replace(">", "").replace(",", ""):
                # constructor, skip
                continue
            if return_type == "static" or name == "static":
                continue
            methods.append(f"{return_type} {name}({params.strip()})")
    return methods


def class_name_for_interface(class_name: str) -> str:
    if class_name.endswith("Service"):
        return class_name
    return class_name


def process_internal_service(impl_path: Path, interface_package: str, interface_dir: Path):
    content = impl_path.read_text()
    # find class name
    class_match = re.search(r"public\s+class\s+(\w+Service)\s*\{", content)
    if not class_match:
        raise RuntimeError(f"Cannot find public class in {impl_path}")
    class_name = class_match.group(1)
    interface_name = class_name_for_interface(class_name)

    # extract imports
    import_lines = re.findall(r"^import .*?;\s*$", content, re.MULTILINE)

    # extract methods
    methods = extract_methods(content)
    if not methods:
        raise RuntimeError(f"No public methods found in {impl_path}")

    # generate interface
    interface_imports = [line for line in import_lines if not line.strip().startswith("import lombok")]
    # add import for the interface types used by method signatures from the same package
    # keep existing imports; compiler will ignore unused ones, spotless will clean up later
    interface_content = [
        f"package {interface_package};",
        "",
    ]
    interface_content.extend(interface_imports)
    if interface_imports:
        interface_content.append("")
    interface_content.append(f"public interface {interface_name} {{")
    for method in methods:
        interface_content.append(f"    {method};")
    interface_content.append("}")
    interface_content.append("")

    interface_dir.mkdir(parents=True, exist_ok=True)
    interface_path = interface_dir / f"{interface_name}.java"
    interface_path.write_text("\n".join(interface_content))

    # modify impl: rename class and add implements, import interface
    interface_import = f"import {interface_package}.{interface_name};"
    if interface_import not in content:
        # add import right after package line
        pkg_match = re.search(r"^(package .*?;)\s*$", content, re.MULTILINE)
        if pkg_match:
            insert_pos = pkg_match.end()
            content = content[:insert_pos] + "\n" + interface_import + content[insert_pos:]

    # rename class and add implements
    content = re.sub(
        r"public\s+class\s+(\w+Service)\s*\{",
        r"public class \1Impl implements \1 {",
        content,
    )

    impl_path.write_text(content)


def main():
    service_dir = WORKTREE / "domain/admin-user/src/main/java/com/lesofn/archforge/user/internal/service"
    interface_dir = WORKTREE / "domain/admin-user-api/src/main/java/com/lesofn/archforge/user/api/service"

    for impl_path in sorted(service_dir.glob("*Impl.java")):
        if impl_path.name == "UserProviderImpl.java":
            continue
        process_internal_service(impl_path, "com.lesofn.archforge.user.api.service", interface_dir)

    # SysMenuService
    menu_impl = WORKTREE / "domain/admin-user/src/main/java/com/lesofn/archforge/user/internal/menu/SysMenuServiceImpl.java"
    menu_interface_dir = WORKTREE / "domain/admin-user-api/src/main/java/com/lesofn/archforge/user/api/menu"
    process_internal_service(menu_impl, "com.lesofn.archforge.user.api.menu", menu_interface_dir)


if __name__ == "__main__":
    main()
