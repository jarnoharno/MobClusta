#!/usr/bin/env python3
import subprocess
import sys
import os

out = subprocess.check_output(['adb','devices'],universal_newlines=True).strip()
dev = [s.split()[0] for s in out.split('\n')[1:]]

gradle = './gradlew'
if os.name == 'nt':
	gradle = 'gradlew.bat'

subprocess.call([gradle,'--daemon','uninstallAll'])
subprocess.call([gradle,'--daemon','installDebug'])

for d in dev:
    subprocess.call(['adb','-s',d,'shell','am','start','-n',
    'fi.hiit.mobclusta/.MainActivity'])
