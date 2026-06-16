<#
  demo.ps1 - Ejecucion autonoma del Proyecto Final (Registraduria)
  --------------------------------------------------------------------
  Un solo comando para demostrar TODO el ciclo de calidad del curso:
    1. Compila + ejecuta las 34 pruebas (unit/integracion/sistema) + cobertura JaCoCo
    2. Levanta la API REST
    3. Hace un smoke de los endpoints
    4. Ejecuta pruebas de rendimiento con JMeter (opcional)
    5. Abre los reportes (cobertura, JMeter, dashboard de calidad)

  Uso:
    pwsh ./demo.ps1                 # demo completa
    pwsh ./demo.ps1 -SkipJMeter     # sin pruebas de carga (mas rapido)
    pwsh ./demo.ps1 -SkipTests      # solo levantar la app y abrir dashboards
#>
param(
  [switch]$SkipJMeter,
  [switch]$SkipTests,
  [int]$Port = 8080
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Set-Location $root

function Section($t) { Write-Host "`n========== $t ==========" -ForegroundColor Cyan }

# --- Resolver herramientas (PATH o C:\tools) ---
function Resolve-Tool($cmd, $fallback) {
  $c = Get-Command $cmd -ErrorAction SilentlyContinue
  if ($c) { return $c.Source }
  if (Test-Path $fallback) { return $fallback }
  throw "No se encontro '$cmd' (ni en PATH ni en $fallback)."
}
$MVN = Resolve-Tool "mvn" "C:\tools\apache-maven-3.9.9\bin\mvn.cmd"
Write-Host "Maven: $MVN"

# --- 1) Pruebas + cobertura ---
if (-not $SkipTests) {
  Section "1/5  Compilacion + 34 pruebas + cobertura JaCoCo (mvn clean verify)"
  & $MVN -B clean verify
  if ($LASTEXITCODE -ne 0) { throw "El build/pruebas fallaron." }
  Write-Host "OK: pruebas verdes y cobertura >= 80%." -ForegroundColor Green
} else {
  Section "1/5  Pruebas OMITIDAS (-SkipTests)"
  & $MVN -B -q -DskipTests package
}

# --- 2) Levantar la API ---
Section "2/5  Levantando la API en http://localhost:$Port"
$out = Join-Path $env:TEMP "pf-app.out.log"; $err = Join-Path $env:TEMP "pf-app.err.log"
$app = Start-Process -FilePath $MVN -ArgumentList "spring-boot:run" `
        -RedirectStandardOutput $out -RedirectStandardError $err -NoNewWindow -PassThru
$up = $false
foreach ($i in 1..45) {
  try { Invoke-WebRequest -Uri "http://localhost:$Port/voters" -UseBasicParsing -TimeoutSec 2 | Out-Null; $up = $true; break }
  catch { Start-Sleep -Seconds 2 }
}
if (-not $up) { Write-Host "La app no respondio a tiempo. Revisa $out" -ForegroundColor Red; throw "App no disponible" }
Write-Host "OK: API arriba (PID $($app.Id))." -ForegroundColor Green

# --- 3) Smoke de endpoints ---
Section "3/5  Smoke de los endpoints"
$body = '{"name":"Demo","id":777,"age":35,"gender":"FEMALE","alive":true}'
Write-Host ("POST /register   -> " + (Invoke-RestMethod -Uri "http://localhost:$Port/register" -Method Post -Body $body -ContentType "application/json"))
Write-Host ("GET  /voters/777 -> " + (Invoke-RestMethod -Uri "http://localhost:$Port/voters/777"))
Write-Host ("GET  /stats      -> " + (Invoke-RestMethod -Uri "http://localhost:$Port/stats" | ConvertTo-Json -Compress))

# --- 4) JMeter ---
if (-not $SkipJMeter) {
  Section "4/5  Pruebas de rendimiento (JMeter)"
  $JM = Resolve-Tool "jmeter" "C:\tools\apache-jmeter-5.6.3\bin\jmeter.bat"
  $scenarios = @(
    @{name="01-smoke"; threads=1;  rampup=1;  duration=20},
    @{name="02-carga"; threads=50; rampup=10; duration=40}
  )
  foreach ($s in $scenarios) {
    $jtl = "results\$($s.name).jtl"; $rep = "results\$($s.name)-report"
    Remove-Item -Force $jtl -ErrorAction SilentlyContinue
    Remove-Item -Recurse -Force $rep -ErrorAction SilentlyContinue
    Write-Host "-> escenario $($s.name) (threads=$($s.threads), $($s.duration)s)"
    $jArgs = @("-n","-t","jmeter\proyecto-final-loadtest.jmx","-l",$jtl,"-e","-o",$rep,
               "-Jthreads=$($s.threads)","-Jrampup=$($s.rampup)","-Jduration=$($s.duration)")
    & $JM @jArgs | Select-String "summary =" | Select-Object -Last 1
  }
} else {
  Section "4/5  JMeter OMITIDO (-SkipJMeter)"
}

# --- 5) Abrir reportes ---
Section "5/5  Abriendo reportes"
$reports = @(
  "target\site\jacoco\index.html",
  "results\02-carga-report\index.html",
  "dashboard\dashboard.html"
)
foreach ($r in $reports) { if (Test-Path $r) { Start-Process (Resolve-Path $r); Write-Host "abierto: $r" } }

Write-Host "`n=================================================================" -ForegroundColor Green
Write-Host " DEMO COMPLETA. La API sigue corriendo en http://localhost:$Port" -ForegroundColor Green
Write-Host " Para detenerla:  Stop-Process -Id $($app.Id)" -ForegroundColor Yellow
Write-Host "=================================================================" -ForegroundColor Green
