import os, re, sys
from pathlib import Path
from datetime import datetime

LOG_FILE = "output_report/fixer_log.txt"

def log(msg):
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    line = f"[{timestamp}] [INFO] {msg}"
    print(line)
    # اس بات کو یقینی بنانا کہ لاگ ڈائریکٹری موجود ہو
    Path(LOG_FILE).parent.mkdir(parents=True, exist_ok=True)
    with open(LOG_FILE, "a") as f:
        f.write(line + "\n")

TARGET_PACKAGE = os.environ.get("TARGET_PACKAGE", "com.system.titan.pro")
JAVA_SRC_DIR = f"app/src/main/java/{TARGET_PACKAGE.replace('.', '/')}"
GRADLE_BUILD = "app/build.gradle"

def read_file(p): return open(p, 'r', encoding='utf-8').read() if os.path.exists(p) else None
def write_file(p, c): Path(p).parent.mkdir(parents=True, exist_ok=True); open(p, 'w', encoding='utf-8').write(c); log(f"Written: {p}")

def fix_java(fpath):
    c = read_file(fpath)
    if not c: return False
    orig = c

    # 1. صرف پیکج کو درست کریں (اگر موجود ہو تو تبدیل کریں، ورنہ شروع میں شامل کریں)
    if re.search(r'^\s*package\s+[\w.]+;', c, re.M):
        c = re.sub(r'^\s*package\s+[\w.]+;', f'package {TARGET_PACKAGE};', c, flags=re.M)
    else:
        c = f"package {TARGET_PACKAGE};\n" + c
    
    # 2. امپورٹ R کو درست کرنا (محفوظ طریقہ)
    import_r = f"import {TARGET_PACKAGE}.R;"
    if import_r not in c:
        c = re.sub(r'import\s+[\w.]+\.R;', import_r, c)
        if import_r not in c:
            # اگر امپورٹ R موجود ہی نہیں تو اسے پیکج کے فوراً بعد لگائیں
            c = c.replace(f"package {TARGET_PACKAGE};", f"package {TARGET_PACKAGE};\n{import_r}")

    # 3. سیمی کولن فکسنگ (صرف ان لائنز پر جو کسی بلاک کا حصہ نہ ہوں)
    lines = c.split('\n')
    new_lines = []
    for line in lines:
        s = line.strip()
        # صرف کوڈ والی لائنز کو چیک کریں
        if s and not s.endswith(('{', '}', ';', ':', '/', '*')) and not s.startswith(('@', 'import', 'package', 'public', 'private', 'protected', 'class', 'if', 'else', 'for', 'while')):
            line = line.rstrip() + ';'
        new_lines.append(line)
    c = '\n'.join(new_lines)

    if c != orig:
        write_file(fpath, c)
        log(f"Safe fixed: {fpath}")
        return True
    return False

def fix_gradle():
    # گریڈل فائل کو اوور رائٹ کرنے کے بجائے چیک کریں
    g = f"namespace '{TARGET_PACKAGE}'"
    if os.path.exists(GRADLE_BUILD):
        content = read_file(GRADLE_BUILD)
        if TARGET_PACKAGE not in content:
            content = re.sub(r"namespace\s+['\"].*?['\"]", g, content)
            write_file(GRADLE_BUILD, content)
            log("Updated namespace in build.gradle")
    return True

def main():
    log("=== Static Fixer Started (Safe Mode) ===")
    fixes = 0
    if os.path.exists(JAVA_SRC_DIR):
        for r,_,fs in os.walk(JAVA_SRC_DIR):
            for f in fs:
                if f.endswith('.java') and fix_java(os.path.join(r,f)):
                    fixes += 1
    fix_gradle()
    log(f"Total fixes applied: {fixes}")
    log("=== Static Fixer Finished ===")

if __name__ == "__main__":
    main()
