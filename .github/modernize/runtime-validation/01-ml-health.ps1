$ErrorActionPreference = 'Stop'
$response = Invoke-RestMethod -Uri 'http://127.0.0.1:8000/health' -TimeoutSec 15
if ($response.status -ne 'ok' -or $response.service -ne 'scholarmate-ml') { throw "Unexpected ML health response" }
Write-Output 'PASS: ML health endpoint'
