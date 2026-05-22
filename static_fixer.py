import os
import re
import sys

target_pkg = os.environ.get('TARGET_PACKAGE', 'com.system.titan.pro')
java_dir = f'app/src/main/java/{target_pkg.replace(".", "/")}'

if not os.path.exists(java_dir):
    print(f"Java directory not found: {java_dir}")
    sys.exit(0)

for root, dirs, files in os.walk(java_dir):
    for f in files:
        if not f.endswith('.java'):
            continue
        path = os.path.join(root, f)
        with open(path, 'r', encoding='utf-8') as file:
            content = file.read()
        original = content

        # 1. Fix package line
        content = re.sub(r'^package\s+[\w.]+;', f'package {target_pkg};', content, flags=re.M)
        if not re.search(r'^package\s+', content, re.M):
            content = f'package {target_pkg};\n' + content

        # 2. Fix import R
        content = re.sub(r'import\s+[\w.]+\.R;', f'import {target_pkg}.R;', content)

        # 3. Add missing imports for CameraManager, CameraCharacteristics, Toast
        imports_to_add = []
        if 'CameraManager' in content and 'android.hardware.camera2.CameraManager' not in content:
            imports_to_add.append('import android.hardware.camera2.CameraManager;')
        if 'CameraCharacteristics' in content and 'android.hardware.camera2.CameraCharacteristics' not in content:
            imports_to_add.append('import android.hardware.camera2.CameraCharacteristics;')
        if 'Toast' in content and 'android.widget.Toast' not in content:
            imports_to_add.append('import android.widget.Toast;')
        if imports_to_add:
            lines = content.split('\n')
            insert_pos = 0
            for i, line in enumerate(lines):
                if line.strip().startswith('package '):
                    insert_pos = i + 1
                    break
            for imp in reversed(imports_to_add):
                lines.insert(insert_pos, imp)
            content = '\n'.join(lines)

        # 4. Fix missing semicolons
        lines = content.split('\n')
        new_lines = []
        for line in lines:
            stripped = line.rstrip()
            if stripped and not stripped.endswith(';') and not stripped.endswith('{') and not stripped.endswith('}') and not stripped.startswith('import ') and not stripped.startswith('package ') and not stripped.strip().startswith('@'):
                if not re.match(r'^\s*(if|for|while|switch|try|catch|finally|else)\s*\(', stripped):
                    stripped += ';'
            new_lines.append(stripped)
        content = '\n'.join(new_lines)

        # 5. Special fix for `})`
        content = re.sub(r'\}\)$', r'});', content, flags=re.M)
        content = re.sub(r'\}\)\;$', r'});', content, flags=re.M)

        if content != original:
            with open(path, 'w', encoding='utf-8') as file:
                file.write(content)
            print(f'Fixed: {path}')
