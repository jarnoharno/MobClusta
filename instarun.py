#!/usr/bin/env python3
import subprocess
import sys

out = subprocess.check_output(['adb','devices'],universal_newlines=True).strip()
dev = [s.split()[0] for s in out.split('\n')[1:]]

subprocess.call(['./gradlew','--daemon','installDebug'])

for d in dev:
    subprocess.call(['adb','-s',d,'shell','am','start','-n',
    'fi.hiit.mobclusta/.MainActivity'])
