$ErrorActionPreference = 'Stop'
$health = Invoke-WebRequest -UseBasicParsing 'http://127.0.0.1:8081/actuator/health' -TimeoutSec 15
$sources = Invoke-RestMethod -Uri 'http://127.0.0.1:8081/api/sources' -TimeoutSec 20
$scholarships = Invoke-RestMethod -Uri 'http://127.0.0.1:8081/api/scholarships' -TimeoutSec 20
if ($health.StatusCode -ne 200) { throw 'Backend health failed' }
if ($sources.Count -lt 1 -or $scholarships.Count -lt 1) { throw 'Backend returned no scraper data' }
foreach ($path in @('/api/scraper/nsp/run', '/api/scraper/run-all', '/api/ml/recommendations/me')) {
  $method = if ($path -like '/api/scraper/*') { 'Post' } else { 'Get' }
  try { Invoke-WebRequest -UseBasicParsing -Method $method -Uri ("http://127.0.0.1:8081" + $path) -TimeoutSec 15 | Out-Null; throw "Expected protected endpoint $path to reject anonymous access" }
  catch { if (-not $_.Exception.Response -or $_.Exception.Response.StatusCode.value__ -ne 401) { throw } }
}
Write-Output "PASS: backend boundaries; active_sources=$(($sources | Where-Object {$_.active}).Count); scholarships=$($scholarships.Count); protected scraper/ML routes reject anonymous access"
