Set-Location -LiteralPath "D:\BOULMANE\SMAHAN BOULMANE\TESTS-APP-OKAN-TREANSFERT\v1\front"
npx ng serve --port 4200 --proxy-config proxy.conf.json 2>&1 | Out-File -LiteralPath "D:\BOULMANE\SMAHAN BOULMANE\TESTS-APP-OKAN-TREANSFERT\v1\front\ng-serve.log" -Encoding UTF8
