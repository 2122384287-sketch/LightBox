#!/usr/bin/env python3
import json,re,sys
from pathlib import Path
PATTERNS=[
(re.compile(r"e:\s+file://(.+?):(\d+):(\d+)\s+(.*)"),"KOTLIN_COMPILE_ERROR"),
(re.compile(r"Unresolved reference:\s(\S+)"),"KOTLIN_UNRESOLVED"),
(re.compile(r"SDK location not found"),"SDK_NOT_FOUND"),
(re.compile(r"Minimum supported Gradle version is\s*([0-9.]+)"),"GRADLE_VERSION_INCOMPATIBLE"),
(re.compile(r"Could not resolve all files"),"DEPENDENCY_RESOLVE_FAIL"),
(re.compile(r"Execution failed for task '(:[^']+)'"),"TASK_FAILED"),
(re.compile(r"Caused by:\s*(.*)"),"CAUSED_BY"),
(re.compile(r"FAILURE:\sBuild failed"),"BUILD_FAILED")
]
def parse(log_text:str):
    errors=[]
    lines=log_text.splitlines()
    for i,line in enumerate(lines):
        for pat,typ in PATTERNS:
            m=pat.search(line)
            if not m:continue
            if typ=="KOTLIN_COMPILE_ERROR":
                errors.append({"type":typ,"file":m.group(1),"line":int(m.group(2)),"col":int(m.group(3)),"msg":m.group(4).strip()})
            elif typ=="KOTLIN_UNRESOLVED":
                errors.append({"type":typ,"sym":m.group(1),"msg":line.strip()})
            elif typ=="GRADLE_VERSION_INCOMPATIBLE":
                errors.append({"type":typ,"req_ver":m.group(1),"msg":line.strip()})
            else:
                errors.append({"type":typ,"msg":line.strip()})
    seen,uniq=set(),[]
    for e in errors:
        key=json.dumps(e,sort_keys=True,ensure_ascii=False)
        if key not in seen:seen.add(key);uniq.append(e)
    return {"errors":uniq}
def main():
    txt=Path(sys.argv[1]).read_text(encoding="utf-8",errors="replace") if len(sys.argv)>1 else sys.stdin.read()
    print(json.dumps(parse(txt),indent=2,ensure_ascii=False))
if __name__=="__main__":main()
