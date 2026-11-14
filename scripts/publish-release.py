#!/usr/bin/env python3
"""Publish an already signed APK. Uses your local gh login; stores no credentials."""
import argparse, hashlib, json, pathlib, re, subprocess, tempfile
p=argparse.ArgumentParser();p.add_argument('--repository',required=True);p.add_argument('--apk',required=True);p.add_argument('--version',required=True);p.add_argument('--version-code',type=int,required=True);p.add_argument('--draft',action='store_true');a=p.parse_args()
if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9-]{0,38}/[A-Za-z0-9_.-]{1,100}',a.repository):p.error('Use owner/repository')
apk=pathlib.Path(a.apk).resolve();subprocess.run(['apksigner','verify','--verbose',str(apk)],check=True)
metadata={'packageName':'com.sakan.iptv','versionCode':a.version_code,'versionName':a.version,'apkFileName':apk.name,'sizeBytes':apk.stat().st_size,'sha256':hashlib.sha256(apk.read_bytes()).hexdigest()}
with tempfile.TemporaryDirectory() as work:
 manifest=pathlib.Path(work)/'update.json';manifest.write_text(json.dumps(metadata,indent=2))
 command=['gh','release','create','v'+a.version,'--repo',a.repository,'--title','Sakan IPTV '+a.version,'--notes','Signed Android update. Install over the existing app to keep accounts.',str(apk),str(manifest)]
 if a.draft:command.append('--draft')
 subprocess.run(command,check=True)
