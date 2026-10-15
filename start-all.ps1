param(
    [string]$DbUsername = $env:SCHOLARMATE_DB_USERNAME,
    [string]$DbPassword = $env:SCHOLARMATE_DB_PASSWORD
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontend = Join-Path $root 'SchlorMateFrontend\SchlorMate'
$backend = Join-Path $root 'scolarmate-backend'
$ml = Join-Path $backend 'ml-service'

if ([string]::IsNullOrWhiteSpace($DbUsername)) {
    $DbUsername = 'postgres'
}
if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    $securePassword = Read-Host 'PostgreSQL password for scholarmate_db' -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try { $DbPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}

function Find-Maven {
    $mvn = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($mvn) { return $mvn.Source }
    $wrapperMaven = Get-ChildItem (Join-Path $env:USERPROFILE '.m2\wrapper\dists') -Filter mvn.cmd -Recurse -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
    if ($wrapperMaven) { return $wrapperMaven }
    throw 'Maven was not found. Install Maven or run the project from an IDE with Maven support.'
}

function Find-Python {
    $candidates = @()
    $python = Get-Command python.exe -ErrorAction SilentlyContinue
    if ($python) { $candidates += $python.Source }
    $py = Get-Command py.exe -ErrorAction SilentlyContinue
    if ($py) { $candidates += $py.Source }
    foreach ($candidate in $candidates | Select-Object -Unique) {
        try {
            & $candidate -c 'import uvicorn' 2>$null
            if ($LASTEXITCODE -eq 0) { return $candidate }
        } catch { }
    }
    throw 'Python was not found. Install Python 3.13 or activate the ML virtual environment.'
}

$maven = Find-Maven
$python = Find-Python
$env:SCHOLARMATE_DB_USERNAME = $DbUsername
$env:SCHOLARMATE_DB_PASSWORD = $DbPassword
$env:SCHOLARMATE_ML_BASE_URL = 'http://127.0.0.1:8000'
$env:SCHOLARMATE_SCRAPER_SCHEDULE_ENABLED = 'true'
$env:SCHOLARMATE_SCRAPER_INITIAL_DELAY_MS = '30000'

Start-Process powershell.exe -ArgumentList @('-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', "Set-Location '$ml'; & '$python' -m uvicorn app.main:app --host 127.0.0.1 --port 8000")
Start-Process powershell.exe -ArgumentList @('-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', "Set-Location '$backend'; & '$maven' spring-boot:run")
Start-Process powershell.exe -ArgumentList @('-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', "Set-Location '$frontend'; npm.cmd run dev -- --host 127.0.0.1 --port 5173")

Write-Host 'Started ScholarMate services:' -ForegroundColor Green
Write-Host '  ML       http://127.0.0.1:8000/health'
Write-Host '  Backend  http://127.0.0.1:8082/actuator/health'
Write-Host '  Frontend http://127.0.0.1:5173'
