@echo off
cd /d "D:\BOULMANE\SMAHAN BOULMANE\TESTS-APP-OKAN-TREANSFERT\v1\front"
start /B npx ng serve --port 4200 --proxy-config proxy.conf.json > ng-serve.log 2>&1
