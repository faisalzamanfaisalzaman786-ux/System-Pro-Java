import os, re, sys
from pathlib import Path
from datetime import datetime

LOG_FILE = "output_report/fixer_log.txt"
def log(msg):
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    line = f"[{timestamp}] [INFO] {msg}"
    print(line)
    with open(LOG_FILE, "a") as f:
        f.write(line + "\n")

TARGET_PACKAGE = os.environ.get("TARGET_PACKAGE", "com.system.titan.pro")
JAVA_SRC_DIR = f"app/src/main/java/{TARGET_PACKAGE.replace('.', '/')}"
GRADLE_BUILD = "app/build.gradle"

def read_file(p): return open(p).read() if os.path.exists(p) else None
def write_file(p, c): Path(p).parent.mkdir(parents=True, exist_ok=True); open(p, 'w').write(c); log(f"Written: {p}")

def fix_java(fpath):
    c = read_file(fpath)
    if not c: return False
    orig = c
    # Fix package
    c = re.sub(r'^package\s+[\w.]+;', f'package {TARGET_PACKAGE};', c, flags=re.M)
    if not re.search(r'^package\s+', c, re.M):
        c = f"package {TARGET_PACKAGE};\n" + c
        log(f"Added package to {fpath}")
    # Fix import R
    c = re.sub(r'import\s+[\w.]+\.R;', f'import {TARGET_PACKAGE}.R;', c)
    if 'R;' not in c:
        lines = c.split('\n')
        for i, line in enumerate(lines):
            if line.startswith('package '):
                lines.insert(i+1, f'import {TARGET_PACKAGE}.R;')
                break
        c = '\n'.join(lines)
        log(f"Added import R to {fpath}")
    # Common imports
    imports = []
    if 'Toast.makeText' in c and 'android.widget.Toast' not in c:
        imports.append('import android.widget.Toast;')
    if 'AlertDialog' in c and 'android.app.AlertDialog' not in c and 'androidx.appcompat.app.AlertDialog' not in c:
        imports.append('import android.app.AlertDialog;')
    if 'ActivityCompat' in c and 'androidx.core.app.ActivityCompat' not in c:
        imports.append('import androidx.core.app.ActivityCompat;')
    if 'ContextCompat' in c and 'androidx.core.content.ContextCompat' not in c:
        imports.append('import androidx.core.content.ContextCompat;')
    if 'Build' in c and 'android.os.Build' not in c:
        imports.append('import android.os.Build;')
    if 'CameraManager' in c and 'android.hardware.camera2.CameraManager' not in c:
        imports.append('import android.hardware.camera2.CameraManager;')
    if 'CameraCharacteristics' in c and 'android.hardware.camera2.CameraCharacteristics' not in c:
        imports.append('import android.hardware.camera2.CameraCharacteristics;')
    if imports:
        lines = c.split('\n')
        pos = 0
        for i, line in enumerate(lines):
            if line.startswith('package ') or line.startswith('import '):
                pos = i+1
        for imp in reversed(imports):
            lines.insert(pos, imp)
        c = '\n'.join(lines)
        log(f"Added imports {imports} to {fpath}")
    # Fix braces
    open_b = c.count('{'); close_b = c.count('}')
    if open_b > close_b:
        c += '\n' + '}' * (open_b - close_b)
        log(f"Fixed braces in {fpath}")
    # Fix semicolons (skip annotations)
    lines = c.split('\n')
    new = []
    for line in lines:
        stripped = line.strip()
        if (stripped and 
            not stripped.startswith('@') and 
            not stripped.startswith('//') and 
            not stripped.startswith('/*') and 
            not stripped.startswith('*') and
            not stripped.endswith('{') and 
            not stripped.endswith('}') and 
            not stripped.endswith(';')):
            if not re.match(r'^@\w+(\(.*\))?$', stripped):
                if not re.match(r'^(if|for|while|switch|try|catch|finally)\s*\(', stripped):
                    line = line.rstrip() + ';'
        new.append(line)
    c = '\n'.join(new)
    if c != orig:
        write_file(fpath, c)
        return True
    return False

def fix_gradle():
    g = f'''plugins {{
    id 'com.android.application'
}}

android {{
    namespace '{TARGET_PACKAGE}'
    compileSdk 34

    defaultConfig {{
        applicationId '{TARGET_PACKAGE}'
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }}

    compileOptions {{
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }}
}}

dependencies {{
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
}}'''
    if os.path.exists(GRADLE_BUILD):
        os.rename(GRADLE_BUILD, GRADLE_BUILD + ".old")
    write_file(GRADLE_BUILD, g)
    log("Created fresh build.gradle")
    return True

def main():
    log("=== Static Fixer Started ===")
    fixes = 0
    if os.path.exists(JAVA_SRC_DIR):
        for r,_,fs in os.walk(JAVA_SRC_DIR):
            for f in fs:
                if f.endswith('.java') and fix_java(os.path.join(r,f)):
                    fixes += 1
    if fix_gradle():
        fixes += 1
    log(f"Total fixes applied: {fixes}")
    log("=== Static Fixer Finished ===")

if __name__ == "__main__":
    main()
