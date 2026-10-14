$ErrorActionPreference = 'Stop'
$student = @{ educationStage='UG'; course='Computer Science'; college='Runtime Test University'; cgpa=8.2; familyIncome=350000; category='General'; state='Maharashtra'; yearOfStudy=3; gender='Other' }
$scholarships = @(
  @{ id=101; name='Computer Science Merit Scholarship'; description='Undergraduate computer science support'; provider='Test Foundation'; course='Computer Science'; category='Merit'; state='Maharashtra'; minimumCgpa=7.0; maximumFamilyIncome=600000; amount=50000; applicationDeadline='2026-12-31' },
  @{ id=102; name='National STEM Grant'; description='STEM support'; provider='Government'; course='Engineering'; category='General'; state='All India'; minimumCgpa=6.5; maximumFamilyIncome=500000; amount=30000; applicationDeadline='2026-11-30' },
  @{ id=103; name='Arts Scholarship'; description='Arts support'; provider='Arts Trust'; course='Fine Arts'; category='Merit'; state='Kerala'; minimumCgpa=8.5; maximumFamilyIncome=200000; amount=20000; applicationDeadline='2026-10-31' }
)
$body = @{ student=$student; scholarships=$scholarships } | ConvertTo-Json -Depth 12
$times = @()
foreach ($attempt in 1..3) {
  $watch = [Diagnostics.Stopwatch]::StartNew()
  $response = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8000/api/v1/recommend' -ContentType 'application/json' -Body $body -TimeoutSec 120
  $watch.Stop(); $times += $watch.ElapsedMilliseconds
}
if ($response.recommendations.Count -ne 3) { throw 'ML did not rank all candidates' }
if ($response.pipeline.semanticStatus -ne 'available') { throw 'Semantic scoring is unavailable' }
Write-Output "PASS: ML recommendation flow; latencies_ms=$($times -join ','); warm_p95_ms=$($times[1..2] | Measure-Object -Maximum | Select-Object -ExpandProperty Maximum)"
